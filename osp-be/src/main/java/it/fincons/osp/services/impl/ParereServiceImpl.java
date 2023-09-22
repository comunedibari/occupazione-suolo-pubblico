package it.fincons.osp.services.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import it.fincons.osp.model.*;
import it.fincons.osp.repository.*;
import it.fincons.osp.services.*;
import it.fincons.osp.utils.Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.ComunicazioneMailInsertDTO;
import it.fincons.osp.dto.ParereDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.ParereMapper;
import it.fincons.osp.payload.protocollazione.response.ProtocolloResponse;
import it.fincons.osp.payload.request.ParereInsertRequest;
import it.fincons.osp.utils.Constants;
import it.fincons.osp.utils.DocUtils;
import it.fincons.osp.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ParereServiceImpl implements ParereService {
	@Value("${osp.app.protocollo.callNewEndpoint}")
	private boolean callNewEndpoint;

	@Autowired
	PraticaRepository praticaRepository;

	@Autowired
	ParereRepository parereRepository;

	@Autowired
	RichiestaParereRepository richiestaParereRepository;

	@Autowired
	UtenteRepository utenteRepository;

	@Autowired
	ProtocollazioneService protocollazioneService;

	@Autowired
	ProtocolloService protocolloService;

	@Autowired
	ParereMapper parereMapper;

	@Autowired
	GruppoRepository gruppoRepository;

	@Autowired
	AllegatoRepository allegatoRepository;

	@Autowired
	ComunicazioneMailService comunicazioneMailService;

	@Autowired
	AllegatoService allegatoService;

	@Autowired
	TipoAllegatoRepository tipoAllegatoRepository;

	@Autowired
	StatoPraticaRepository statoPraticaRepository;
	@Autowired
	private RichiestaParereService richiestaParereService;

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public ParereDTO insertParere(ParereInsertRequest parereInsertRequest, boolean isRipristinoLuoghi) {

		ParereDTO parereRequest = parereInsertRequest.getParere();

		RichiestaParere richiestaParere = richiestaParereRepository.findById(parereRequest.getIdRichiestaParere())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID richiesta parere non presente nel sistema", parereRequest.getIdRichiestaParere()));

		Utente utenteParere = utenteRepository.findById(parereRequest.getIdUtenteParere())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID utente parere non presente nel sistema", parereRequest.getIdUtenteParere()));

		Pratica pratica = richiestaParere.getPratica();

		Parere parere = new Parere();
		parere.setRichiestaParere(richiestaParere);
		parere.setUtenteParere(utenteParere);
		parere.setNota(parereRequest.getNota());
		parere.setEsito(parereRequest.getEsito());
		parere.setDataInserimento(LocalDateTime.now().withNano(0));
		parere.setFlagCompetenza(parereRequest.getFlagCompetenza());

		parereRepository.save(parere);

		// aggiorno flag risposta in richiesta parere
		richiestaParere.setFlagInseritaRisposta(true);
		richiestaParereRepository.save(richiestaParere);

		richiestaParere.setParere(parere);

		//recupero allegati parere
		List<Allegato> allegatiParere =allegatoRepository.findByIdRichiestaParere(richiestaParere.getId());

		pratica.setDataModifica(LocalDateTime.now().withNano(0)); // Per creare riga nello storico
		pratica.setUtenteModifica(utenteParere);

		String infoPassaggioStato = null;
		// aggiorno stato pratica per il processo di rinuncia oppure per gli stati della pratica Revocata, Decaduta ed Annullata
		if (pratica.getTipoProcesso().getId().equals(Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA)
				|| pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_REVOCATA)
				|| pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_DECADUTA)
				|| pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_ANNULLATA)
				|| pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_TERMINATA)) {
			StatoPratica statoPraticaArchiviata = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_ARCHIVIATA)
					.orElseThrow(() -> new RuntimeException("Errore: stato pratica ARCHIVIATA non trovato"));

			infoPassaggioStato = PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_ARCHIVIATA);
			pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_ARCHIVIATA));

			pratica.setStatoPratica(statoPraticaArchiviata);
			richiestaParere.setStatoPratica(statoPraticaArchiviata);
		}else{
			infoPassaggioStato = PassaggioStato.RICHIESTA_PARERI_TO_ESPRIMI_PARERE.getDescStatoPratica();
			infoPassaggioStato=String.format(infoPassaggioStato, utenteParere.getGruppo().getDescrizione());

			pratica.setInfoPassaggioStato(infoPassaggioStato);
		}

		praticaRepository.save(pratica);
		pratica.getRichiestePareri().removeIf(r -> r.getId().equals(richiestaParere.getId()));
		pratica.getRichiestePareri().add(richiestaParere);

		List<Utente> listaDestinatari = getDestinatariParere(pratica, isRipristinoLuoghi);

		// richiesta numero protocollo
		ProtocolloResponse protocolloResponse = null;

		if(callNewEndpoint){
			List<Allegato> allegati=new ArrayList<>();
			byte[] docPrincipale=null;
			String docPrincipaleName = null;

			if(allegatiParere!=null&&allegatiParere.size()>0){
				for(int i=0;i<allegatiParere.size();i++){
					Allegato allegato=allegatiParere.get(i);

					if(allegato.getTipoAllegato().getId()==Constants.ID_TIPO_ALLEGATO_RELAZIONE_SERVIZIO||allegato.getTipoAllegato().getId()==Constants.ID_TIPO_ALLEGATO_ISTRUTTORIA_TECNICA){
						docPrincipale=new String(Base64.getEncoder().encode(allegato.getFileAllegato())).getBytes();
						docPrincipaleName = allegato.getNomeFile();
					}else if(allegato.getTipoAllegato().getId()==Constants.ID_TIPO_ALLEGATO_ORDINANZA){
						allegati.add(allegato);
					}
				}

				protocolloResponse = protocollazioneService.getNumeroProtocolloUscitaV2(
					pratica,
					utenteParere,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_PARERE,
					listaDestinatari,
					false,
					docPrincipale,
					Utils.getFileExtension(docPrincipaleName),
					allegati
				);

			} else {
				protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(
					pratica,
					utenteParere,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_PARERE,
					listaDestinatari,
					false,
					Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA,
					allegati,
					null,
					//in this situation a string for passaggio stato is needed
					infoPassaggioStato);

			}

			parere.setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());
			parere.setDataProtocollo(protocolloResponse.getDataProtocollo());
		} else {
			protocolloResponse =  protocollazioneService.getNumeroProtocolloUscita(pratica,
					utenteParere,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_PARERE, listaDestinatari, false);
		}

		parereRepository.save(parere);

		// aggiorno allegati parere
		allegatoService.updateAllegatiParere(parere);

		// inserimento comunicazione mail
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);
		this.sendEmailNotificaParereToIstruttoreMunicipio(
			listaDestinatari,
			utenteParere,
			pratica,
			richiestaParere,
			parere,
			protocolloInserimento.getCodiceProtocollo(),
			parere.getCodiceProtocollo(),
			isRipristinoLuoghi
		);

		// gestione email ordinanza
		if (CollectionUtils.isNotEmpty(parereInsertRequest.getListaIdUtentiEmail())) {
			this.sendEmailOrdinanza(parere, parereInsertRequest.getListaIdUtentiEmail(),
					parereInsertRequest.isFlagPec(), pratica, protocolloInserimento.getCodiceProtocollo(),
					parere.getCodiceProtocollo() );
		}

		return parereMapper.entityToDto(parere);
	}

	@Override
	@LogEntryExit(showArgs = true)
	public ParereDTO getParere(Long id) {
		Parere parere = parereRepository.findById(id).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: id parere non presente nel sistema", id));

		return parereMapper.entityToDto(parere);
	}

	/**
	 * Costruisce la mail di notifica parere espresso per gli istruttori di
	 * municipio e inserisce la comunicazione email da inviare
	 * 
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param richiestaParere
	 * @param parere
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 */
	@LogEntryExit
	private void sendEmailNotificaParereToIstruttoreMunicipio(List<Utente> destinatari, Utente utente, Pratica pratica,
			RichiestaParere richiestaParere, Parere parere, String codiceProtocolloPratica,
			String codiceProtocolloOperazione, boolean isRipristinoLuoghi) {

		// costruisco comunicazione email
		ComunicazioneMailInsertDTO comunicazioneMail = new ComunicazioneMailInsertDTO();
		comunicazioneMail.setPratica(pratica);
		comunicazioneMail.setRichiestaParere(richiestaParere);
		if (isRipristinoLuoghi) {
			comunicazioneMail.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione risposta della verifica ripristino dei luoghi"));
			comunicazioneMail.setTesto(EmailUtils.getContentEmail(
				EmailUtils.getMessageParereRipristinoToAttori(pratica, codiceProtocolloPratica, codiceProtocolloOperazione,
					parere.getUtenteParere().getGruppo().getDescrizione(), parere.getNota()),
			"Comunicazione risposta della verifica ripristino dei luoghi"));
		} else {
			comunicazioneMail.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione risposta alla richiesta pareri"));
			comunicazioneMail.setTesto(EmailUtils.getContentEmail(
					EmailUtils.getMessageParereToAttori(pratica, codiceProtocolloPratica, codiceProtocolloOperazione,
							parere.getUtenteParere().getGruppo().getDescrizione(), parere.getNota()),
					"Comunicazione risposta alla richiesta pareri"));
		}

		List<String> toUtente = new ArrayList<>();
		destinatari.stream().forEach(u -> toUtente.add(u.getEmail()));

		if (!toUtente.isEmpty()) {
			comunicazioneMail.setDestinatari(String.join(",", toUtente));
			comunicazioneMail.setDestinatariCc(utente.getEmail());
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMail);
		}
	}

	/**
	 * Ritorna gli utenti destinatari per l'invio delle mail e per la
	 * protocollazione per l'operazione di inserimento parere
	 * 
	 * @param pratica
	 * @return la lista di destinatari
	 */
	private List<Utente> getDestinatariParere(Pratica pratica, boolean isRipristinoLuoghi) {
		Gruppo gruppoIstruttoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_ISTRUTTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo istruttore municipio non trovato"));

		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoIstruttoreMunicipio);
		if (isRipristinoLuoghi) {
			pratica.getRichiestePareri().stream().forEach(
				rp -> {
					List<Gruppo> gruppi = listaGruppi.stream().filter(
						g -> g.getId().equals(rp.getGruppoDestinatarioParere().getId())
					).collect(Collectors.toList());
					if (gruppi.isEmpty()) {
						listaGruppi.add(rp.getGruppoDestinatarioParere());
					}
				}
			);
		}

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		return utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);
	}

	/**
	 * Costruisce la mail di notifica inserimento ordinanza e inserisce la
	 * comunicazione email da inviare
	 * 
	 * @param parere
	 * @param listaIdUtentiEmail
	 * @param flagPec
	 */
	@LogEntryExit
	private void sendEmailOrdinanza(Parere parere, List<Long> listaIdUtentiEmail, boolean flagPec, Pratica pratica,
			String codiceProtocolloPratica, String codiceProtocolloOperazione) {

		// recupero file ordinanza
		TipoAllegato tipoAllegatoOrdinanza = tipoAllegatoRepository.findById(Constants.ID_TIPO_ALLEGATO_ORDINANZA)
				.orElseThrow(() -> new RuntimeException("Tipo allegato ordinanza non trovato"));
		Allegato allegatoOrdinanza = allegatoService.getAllegatoParereByTipo(parere, tipoAllegatoOrdinanza);

		if (allegatoOrdinanza != null) {
			// recupero email destinatari
			List<String> emailDestinatari = new ArrayList<>();
			for (Long idUtente : listaIdUtentiEmail) {
				Utente utente = utenteRepository.findById(idUtente)
						.orElseThrow(() -> new BusinessException(ErrorCode.E1,
								"Errore: ID utente destinatario ordinanza non presente nel sistema", idUtente));

				emailDestinatari.add(utente.getEmail());
			}

			// costruisco email
			// costruisco comunicazione email
			ComunicazioneMailInsertDTO comunicazioneMail = new ComunicazioneMailInsertDTO();
			comunicazioneMail.setPratica(parere.getRichiestaParere().getPratica());
			comunicazioneMail.setRichiestaParere(parere.getRichiestaParere());
			comunicazioneMail.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione inserimento ordinanza"));
			comunicazioneMail.setTesto(
					EmailUtils.getContentEmail(EmailUtils.getMessageOrdinanza(pratica, codiceProtocolloPratica, codiceProtocolloOperazione),
							"Comunicazione inserimento ordinanza"));

			// setto il flag che indica se e' una PEC
			comunicazioneMail.setFlagPec(flagPec);

			if (allegatoOrdinanza.getMimeType().equals(Constants.MIME_TYPE_DOCX)) {
				comunicazioneMail.setNomeFileAllegato(allegatoOrdinanza.getNomeFile().replace(".docx", ".pdf"));
				comunicazioneMail.setMimeTypeFileAllegato(Constants.MIME_TYPE_PDF);
				try {
					comunicazioneMail.setFileAllegato(DocUtils.docxToPdf(allegatoOrdinanza.getFileAllegato()));
				} catch (Docx4JException e) {
					log.error("Errore durante la conversione dell'ordinanza in pdf", e);
					throw new BusinessException(ErrorCode.E22, "Errore durante la conversione dell'ordinanza in pdf");
				}
			} else {
				comunicazioneMail.setNomeFileAllegato(allegatoOrdinanza.getNomeFile());
				comunicazioneMail.setMimeTypeFileAllegato(allegatoOrdinanza.getMimeType());
				comunicazioneMail.setFileAllegato(allegatoOrdinanza.getFileAllegato());
			}

			if (!emailDestinatari.isEmpty()) {
				comunicazioneMail.setDestinatari(String.join(",", emailDestinatari));
				comunicazioneMailService.insertComunicazioneMail(comunicazioneMail);
			}
		}
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
