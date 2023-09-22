package it.fincons.osp.services.impl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.*;

import javax.management.*;
import javax.transaction.Transactional;

import it.fincons.osp.model.*;
import it.fincons.osp.repository.*;
import it.fincons.osp.security.jwt.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.ComunicazioneMailInsertDTO;
import it.fincons.osp.dto.RichiestaParereDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.RichiestaParereMapper;
import it.fincons.osp.payload.protocollazione.response.ProtocolloResponse;
import it.fincons.osp.payload.request.UlterioriPareriInsertRequest;
import it.fincons.osp.services.ComunicazioneMailService;
import it.fincons.osp.services.ProtocollazioneService;
import it.fincons.osp.services.ProtocolloService;
import it.fincons.osp.services.RichiestaParereService;
import it.fincons.osp.utils.Constants;
import it.fincons.osp.utils.EmailUtils;

@Service
public class RichiestaParereServiceImpl implements RichiestaParereService {
	@Value("${osp.app.protocollo.callNewEndpoint}")
	private boolean callNewEndpoint;

	private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

	@Autowired
	RichiestaParereRepository richiestaParereRepository;

	@Autowired
	PraticaRepository praticaRepository;

	@Autowired
	UtenteRepository utenteRepository;

	@Autowired
	GruppoRepository gruppoRepository;

	@Autowired
	StatoPraticaRepository statoPraticaRepository;

	@Autowired
	AllegatoRepository allegatoRepository;

	@Autowired
	ProtocollazioneService protocollazioneService;

	@Autowired
	ProtocolloService protocolloService;

	@Autowired
	RichiestaParereMapper richiestaParereMapper;

	@Autowired
	ComunicazioneMailService comunicazioneMailService;

	@Override
	@Transactional
	@LogEntryExit(showArgs = true)
	public RichiestaParereDTO insertRichiestaParereFromVerificaFormale(RichiestaParereDTO richiestaParereRequest) {

		Pratica pratica = praticaRepository.findById(richiestaParereRequest.getIdPratica())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						richiestaParereRequest.getIdPratica()));

		Utente utenteRichiedente = utenteRepository.findById(richiestaParereRequest.getIdUtenteRichiedente())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID utente richiedente non presente nel sistema",
						richiestaParereRequest.getIdUtenteRichiedente()));

		Gruppo gruppoDestinatarioParere = gruppoRepository.findById(Constants.ID_GRUPPO_POLIZIA_LOCALE)
				.orElseThrow(() -> new RuntimeException("Errore: ID gruppo polizia locale non presente nel sistema"));

		// inserimento richiesta parere
		RichiestaParere richiestaParere = this.performInsertRichiestaParere(
				richiestaParereRequest.getNotaRichiestaParere(), pratica, utenteRichiedente, gruppoDestinatarioParere);

		// aggiorno stato della pratica e inserisco flag verifica formale
		StatoPratica statoPraticaRichiestaPareri = statoPraticaRepository
				.findById(Constants.ID_STATO_PRATICA_RICHIESTA_PARERI)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica RICHIESTA PARERI non trovato"));

		//String infoPassaggioStato=PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_RICHIESTA_PARERI);
		//infoPassaggioStato=String.format(infoPassaggioStato, utenteRichiedente.getGruppo().getDescrizione());

		if(richiestaParereRequest.isRiabilitaEsenzioneMarcaDaBollo()) {
			pratica.getDatiRichiesta().setFlagEsenzioneMarcaDaBolloModificato(
				pratica.getDatiRichiesta().isFlagEsenzioneMarcaDaBolloModificato() ||
				!pratica.getDatiRichiesta().isFlagEsenzioneMarcaDaBollo()
			);
			pratica.getDatiRichiesta().setFlagEsenzioneMarcaDaBollo(true);
			if (
				pratica.getDatiRichiesta().isFlagEsenzioneMarcaDaBolloModificato() &&
				(
					pratica.getDatiRichiesta().getMotivazioneEsenzioneMarcaBollo() == null ||
					StringUtils.isBlank(pratica.getDatiRichiesta().getMotivazioneEsenzioneMarcaBollo())
				)
			) {
				pratica.getDatiRichiesta().setMotivazioneEsenzioneMarcaBollo("Approvata come esente");
			}
		}

		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_RICHIESTA_PARERI));
		pratica.setStatoPratica(statoPraticaRichiestaPareri);
		pratica.setFlagVerificaFormale(true);
		pratica.setUtenteModifica(utenteRichiedente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));
		praticaRepository.save(pratica);

		if (pratica.getRichiestePareri() == null) {
			List<RichiestaParere> richiestePareri = new ArrayList<>();
			pratica.setRichiestePareri(richiestePareri);
		}
		pratica.getRichiestePareri().add(richiestaParere);

		List<Utente> listaDestinatari = getDestinatariRichiestaParereToPoliziaLocale(pratica);

		if(listaDestinatari==null||listaDestinatari.size()==0){
			throw new BusinessException(ErrorCode.E33, "Errore: non sono presenti referenti del gruppo Polizia Locale per il "+pratica.getMunicipio().getDescrizione());
		}

		// richiesta numero protocollo
		ProtocolloResponse protocolloResponse = null;

		if(callNewEndpoint){
			protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(
					pratica,
					utenteRichiedente,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_PARERE,
					listaDestinatari,
					false,
					Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA,
					null,
					PassaggioStato.VERIFICA_FORMALE_TO_RICHIESTA_PARERI,
					null);

		}else{
			protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(pratica,
					utenteRichiedente, Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_PARERE, listaDestinatari, false);
		}

		try {
			// inserisco protocollo sulla richiesta parere
			richiestaParere
					.setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());
			richiestaParere.setDataProtocollo(protocolloResponse.getDataProtocollo());
			richiestaParereRepository.save(richiestaParere);

			// inserimento comunicazione mail
			Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);
			this.sendEmailNotificaRichiestaParereToPoliziaLocale(listaDestinatari, utenteRichiedente, pratica,
					richiestaParere, protocolloInserimento.getCodiceProtocollo(), richiestaParere.getCodiceProtocollo());
		} catch(Exception error) {
			logger.error(error.getMessage());
		}

		return richiestaParereMapper.entityToDto(richiestaParere);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public List<RichiestaParereDTO> insertUlterioriRichiestePareri(UlterioriPareriInsertRequest ulterioriPareriRequest,
			String usernameUtente) {

		Pratica pratica = praticaRepository.findById(ulterioriPareriRequest.getIdPratica())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						ulterioriPareriRequest.getIdPratica()));

		Utente utente = utenteRepository.findByUsernameAndFlagEliminatoFalse(usernameUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E3, "Errore: username non presente nel sistema", usernameUtente));

		List<RichiestaParereDTO> listaPareriInseriti = new ArrayList<>();

		if (pratica.getRichiestePareri() == null) {
			List<RichiestaParere> richiestePareri = new ArrayList<>();
			pratica.setRichiestePareri(richiestePareri);
		}

		// recupero protocollo inserimento pratica
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);

		for (Integer idGruppo : ulterioriPareriRequest.getIdGruppiDestinatariPareri()) {
			Gruppo gruppoDestinatarioParere = gruppoRepository.findById(idGruppo).orElseThrow(
					() -> new BusinessException(ErrorCode.E1, "Errore: ID gruppo non presente nel sistema", idGruppo));

			RichiestaParere richiestaParere = this.performInsertRichiestaParere(null, pratica, utente,
					gruppoDestinatarioParere);

			pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(3,560)); //id destinazione fittizio per evitare conflitto con RICHIESTA_PARERI_TO_ESPRIMI_PARERE
			pratica.setUtenteModifica(utente);
			pratica.setDataModifica(LocalDateTime.now().withNano(0));
			praticaRepository.save(pratica);

			pratica.getRichiestePareri().add(richiestaParere);

			List<Utente> listaDestinatari = getDestinatariRichiestaUlterioriPareri(pratica, gruppoDestinatarioParere);

			// richiesta numero protocollo
			ProtocolloResponse protocolloResponse = null;

			if(callNewEndpoint){

				protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(
						pratica,
						utente,
						Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_PARERE,
						listaDestinatari,
						false,
						Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA,
						null,
						PassaggioStato.RICHIESTA_PARERI_TO_RICHIESTA_PARERI,
						null);

			}else{
				protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(pratica, utente,
						Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_PARERE, listaDestinatari, false);
			}

			// inserisco protocollo sulla richiesta parere
			richiestaParere
					.setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());
			richiestaParere.setDataProtocollo(protocolloResponse.getDataProtocollo());
			richiestaParereRepository.save(richiestaParere);

			// inserimento comunicazione mail
			this.sendEmailNotificaRichiestaParereToAttori(utente, pratica, richiestaParere,
					protocolloInserimento.getCodiceProtocollo(), gruppoDestinatarioParere);

			listaPareriInseriti.add(richiestaParereMapper.entityToDto(richiestaParere));
		}

		return listaPareriInseriti;
	}

	@LogEntryExit
	@Transactional
	@Override
	public RichiestaParereDTO insertRichiestaVerificaRipristinoLuoghi(Pratica pratica, Utente utenteRichiedente/*,
																	  PassaggioStato passaggioStato*/) {
		Gruppo gruppoDestinatarioParere = gruppoRepository.findById(Constants.ID_GRUPPO_POLIZIA_LOCALE)
				.orElseThrow(() -> new RuntimeException("Errore: ID gruppo polizia locale non presente nel sistema"));

		// inserimento richiesta parere
		RichiestaParere richiestaParere = this.performInsertRichiestaParere(
				Constants.NOTA_RICHIESTA_PARERE_VERIFICA_RIPRISTINO_LUOGHI, pratica, utenteRichiedente,
				gruppoDestinatarioParere);

		if (pratica.getRichiestePareri() == null) {
			List<RichiestaParere> richiestePareri = new ArrayList<>();
			pratica.setRichiestePareri(richiestePareri);
		}
		pratica.getRichiestePareri().add(richiestaParere);

		List<Utente> listaDestinatari = getDestinatariRichiestaParereToPoliziaLocale(pratica);


		richiestaParereRepository.save(richiestaParere);

		// inserimento comunicazione mail
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);
		this.sendEmailNotificaRichiestaVerificaRipristinoLuoghiToPoliziaLocale(listaDestinatari, utenteRichiedente,
				pratica, richiestaParere, protocolloInserimento.getCodiceProtocollo(),
				richiestaParere.getCodiceProtocollo());

		return richiestaParereMapper.entityToDto(richiestaParere);
	}

	/**
	 * Effettua la creazione e il salvataggio di una nuova richiesta parere
	 * 
	 * @param notaRichiestaParere
	 * @param pratica
	 * @param utenteRichiedente
	 * @param gruppoDestinatarioParere
	 * @return l'oggetto richiesta parere salvato
	 */
	@LogEntryExit
	@Transactional
	private RichiestaParere performInsertRichiestaParere(String notaRichiestaParere, Pratica pratica,
			Utente utenteRichiedente, Gruppo gruppoDestinatarioParere) {

		RichiestaParere richiestaParere = new RichiestaParere();
		richiestaParere.setPratica(pratica);
		richiestaParere.setUtenteRichiedente(utenteRichiedente);
		richiestaParere.setStatoPratica(pratica.getStatoPratica());
		richiestaParere.setGruppoDestinatarioParere(gruppoDestinatarioParere);
		richiestaParere.setNotaRichiestaParere(notaRichiestaParere);
		richiestaParere.setDataInserimento(LocalDateTime.now().withNano(0));
		richiestaParere.setFlagInseritaRisposta(false);

		richiestaParereRepository.save(richiestaParere);

		return richiestaParere;
	}

	/**
	 * Costruisce la mail di notifica richiesta pareri per la Polizia Locale e
	 * inserisce la comunicazione email da inviare
	 * 
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param richiestaParere
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 */
	@LogEntryExit
	private void sendEmailNotificaRichiestaParereToPoliziaLocale(List<Utente> destinatari, Utente utente,
			Pratica pratica, RichiestaParere richiestaParere, String codiceProtocolloPratica,
			String codiceProtocolloOperazione) {

		// costruisco comunicazione email per la polizia locale
		ComunicazioneMailInsertDTO comunicazioneMail = new ComunicazioneMailInsertDTO();
		comunicazioneMail.setPratica(pratica);
		comunicazioneMail.setRichiestaParere(richiestaParere);
		comunicazioneMail.setOggetto(this.buildOggettoEmail(pratica, "Richiesta di parere"));
		comunicazioneMail.setTesto(EmailUtils.getContentEmail(EmailUtils.getMessageRichiestaPareriToAttori(pratica,
				codiceProtocolloPratica, codiceProtocolloOperazione), "Richiesta di parere"));

		Set<String> toUtente = new HashSet<>();
		destinatari.stream().forEach(u -> toUtente.add(u.getEmail()));

		if (!toUtente.isEmpty()) {
			comunicazioneMail.setDestinatari(String.join(",", toUtente));
			comunicazioneMail.setDestinatariCc(utente.getEmail());
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMail);
		}
	}

	/**
	 * Costruisce la mail di notifica richiesta pareri per gli attori coinvolti e
	 * inserisce la comunicazione email da inviare
	 * 
	 * @param pratica
	 * @param richiestaParere
	 * @param codiceProtocolloPratica
	 */
	@LogEntryExit
	private void sendEmailNotificaRichiestaParereToAttori(Utente utente, Pratica pratica,
			RichiestaParere richiestaParere, String codiceProtocolloPratica, Gruppo gruppoDestinatario) {

		// costruisco comunicazione email per il municipio
		ComunicazioneMailInsertDTO comunicazioneMail = new ComunicazioneMailInsertDTO();
		comunicazioneMail.setPratica(pratica);
		comunicazioneMail.setRichiestaParere(richiestaParere);
		comunicazioneMail.setOggetto(this.buildOggettoEmail(pratica, "Richiesta di parere"));
		comunicazioneMail.setTesto(EmailUtils.getContentEmail(EmailUtils.getMessageRichiestaPareriToAttori(pratica,
				codiceProtocolloPratica, richiestaParere.getCodiceProtocollo()), "Richiesta di parere"));

		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDestinatario);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		List<Utente> listaUtenti = utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);
		List<String> toUtente = new ArrayList<>();
		listaUtenti.stream().forEach(u -> toUtente.add(u.getEmail()));
		toUtente.add(pratica.getUtentePresaInCarico().getEmail());

		if (!toUtente.isEmpty()) {
			comunicazioneMail.setDestinatari(String.join(",", toUtente));
			comunicazioneMail.setDestinatariCc(utente.getEmail());
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMail);
		}
	}

	/**
	 * Ritorna gli utenti destinatari per l'invio delle mail e per la
	 * protocollazione per l'operazione di richiesta parere alla Polizia locale
	 * 
	 * @param pratica
	 * @return la lista di destinatari
	 */
	private List<Utente> getDestinatariRichiestaParereToPoliziaLocale(Pratica pratica) {
		Gruppo gruppoPoliziaLocale = gruppoRepository.findById(Constants.ID_GRUPPO_POLIZIA_LOCALE)
				.orElseThrow(() -> new RuntimeException("Gruppo polizia locale non trovato"));

		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoPoliziaLocale);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		return utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);
	}

	/**
	 * Ritorna gli utenti destinatari per l'invio delle mail e per la
	 * protocollazione per l'operazione di richiesta parere agli utenti di un gruppo
	 * specifico
	 * 
	 * @param pratica
	 * @param gruppoDestinatario
	 * @return la lista di destinatari
	 */
	private List<Utente> getDestinatariRichiestaUlterioriPareri(Pratica pratica, Gruppo gruppoDestinatario) {
		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDestinatario);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		List<Utente> listaUtenti = utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);

		listaUtenti.add(pratica.getUtentePresaInCarico());

		return listaUtenti;
	}

	/**
	 * Costruisce la mail di notifica richiesta verifica del ripristino dei luoghi
	 * per la Polizia Locale e inserisce la comunicazione email da inviare
	 * 
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param richiestaParere
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 */
	@LogEntryExit
	private void sendEmailNotificaRichiestaVerificaRipristinoLuoghiToPoliziaLocale(List<Utente> destinatari,
			Utente utente, Pratica pratica, RichiestaParere richiestaParere, String codiceProtocolloPratica,
			String codiceProtocolloOperazione) {

		// costruisco comunicazione email per la polizia locale
		ComunicazioneMailInsertDTO comunicazioneMail = new ComunicazioneMailInsertDTO();
		comunicazioneMail.setPratica(pratica);
		comunicazioneMail.setRichiestaParere(richiestaParere);
		comunicazioneMail
			.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione verifica del ripristino dei luoghi"));
		comunicazioneMail
			.setTesto(
				EmailUtils.getContentEmail(
					EmailUtils.getMessageRichiestaVerificaRipristinoLuoghiToAttori(pratica,
						codiceProtocolloPratica, codiceProtocolloOperazione),
				"Comunicazione verifica del ripristino dei luoghi"));

		Set<String> toUtente = new HashSet<>();
		destinatari.stream().forEach(u -> toUtente.add(u.getEmail()));

		if (!toUtente.isEmpty()) {
			comunicazioneMail.setDestinatari(String.join(",", toUtente));
			comunicazioneMail.setDestinatariCc(utente.getEmail());
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMail);
		}
	}

	public List<RichiestaParere> findAllByProtocollo(String protocollo) {
		return this.richiestaParereRepository.findAllByCodiceProtocolloLike("%" + protocollo + "%");
	}

	private String buildOggettoEmail(Pratica pratica, String subject){

		String numeroProtocollo=null;

		if(pratica.getProtocolli()!=null&&pratica.getProtocolli().size()>0){
			Comparator<Protocollo> comparator = Comparator.comparing( Protocollo::getNumeroProtocollo );

			Protocollo min = pratica.getProtocolli().stream().min(comparator).get();

			if(min!=null){
				numeroProtocollo=min.getCodiceProtocollo();
			}

		}

		StringBuilder oggetto=new StringBuilder();

		if(pratica.getDestinatario()!=null){
			String denominazione=null;

			if(pratica.getDestinatario().getCognome()!=null&&pratica.getDestinatario().getNome()!=null){
				denominazione=pratica.getDestinatario().getCognome()+" "+pratica.getDestinatario().getNome();
			}else{
				denominazione=pratica.getDestinatario().getDenominazione();
			}

			oggetto.append(String.format("Concessione di Occupazione Suolo Pubblico - %s - %s - %s - %s",
					pratica.getTipoProcesso().getDescrizione(),
					denominazione,
					numeroProtocollo,
					subject
			));
		}else{
			oggetto.append(String.format("Concessione di Occupazione Suolo Pubblico - %s - %s %s - %s - %s",
					pratica.getTipoProcesso().getDescrizione(),
					pratica.getFirmatario().getNome(),
					pratica.getFirmatario().getCognome(),
					numeroProtocollo,
					subject));
		}

		return oggetto.toString();
	}
}
