package it.fincons.osp.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import it.fincons.osp.model.*;
import it.fincons.osp.repository.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.ComunicazioneMailInsertDTO;
import it.fincons.osp.dto.RichiestaIntegrazioneDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.RichiestaIntegrazioneMapper;
import it.fincons.osp.payload.protocollazione.response.ProtocolloResponse;
import it.fincons.osp.services.ComunicazioneMailService;
import it.fincons.osp.services.ProtocollazioneService;
import it.fincons.osp.services.ProtocolloService;
import it.fincons.osp.services.RichiestaIntegrazioneService;
import it.fincons.osp.utils.Constants;
import it.fincons.osp.utils.EmailUtils;

@Service
public class RichiestaIntegrazioneServiceImpl implements RichiestaIntegrazioneService {
	@Value("${osp.app.protocollo.callNewEndpoint}")
	private boolean callNewEndpoint;

	@Value("${osp.app.scadenzario.integrazione.giorni}")
	private Integer numGiorniScadenzaIntegrazione;

	@Value("${osp.app.richiestaIntegrazione.numMax}")
	private Integer numMaxRichiesteIntegrazione;

	@Value("${osp.app.scadenzario.preavvisoDiniego.giorni}")
	private Integer numGiorniScadenzaPreavvisoDiniego;

	@Value("${osp.app.scadenzario.rettificaDate.giorni}")
	private Integer numGiorniScadenzaRettificaDate;

	@Autowired
	RichiestaIntegrazioneRepository richiestaIntegrazioneRepository;

	@Autowired
	PraticaRepository praticaRepository;

	@Autowired
	UtenteRepository utenteRepository;

	@Autowired
	StatoPraticaRepository statoPraticaRepository;

	@Autowired
	AllegatoRepository allegatoRepository;

	@Autowired
	ProtocollazioneService protocollazioneService;

	@Autowired
	ProtocolloService protocolloService;

	@Autowired
	ComunicazioneMailService comunicazioneMailService;

	@Autowired
	RichiestaIntegrazioneMapper richiestaIntegrazioneMapper;

	@Autowired
	GruppoRepository gruppoRepository;

	@Override
	@Transactional
	@LogEntryExit(showArgs = true)
	public RichiestaIntegrazioneDTO insertRichiestaIntegrazione(RichiestaIntegrazioneDTO richiestaIntegrazioneRequest) {
		Pratica pratica = praticaRepository.findById(richiestaIntegrazioneRequest.getIdPratica())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						richiestaIntegrazioneRequest.getIdPratica()));

		Utente utenteRichiedente = utenteRepository.findById(richiestaIntegrazioneRequest.getIdUtenteRichiedente())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID utente richiedente non presente nel sistema",
						richiestaIntegrazioneRequest.getIdUtenteRichiedente()));

		// controllo numero richieste integrazione
		if (
				(pratica.getContatoreRichiesteIntegrazioni() == null
				|| pratica.getContatoreRichiesteIntegrazioni() < numMaxRichiesteIntegrazione - 1)
			&& (pratica.getFlagProceduraDiniego() == null || !pratica.getFlagProceduraDiniego())
		) {

			// inserimento richiesta integrazione
			RichiestaIntegrazione richiestaIntegrazione = this.performInsertRichiestaIntegrazione(richiestaIntegrazioneRequest, pratica, utenteRichiedente, false);

			List<Utente> listaDestinatari = getDestinatariRichiestaIntegrazione(pratica);

			// aggiorno stato della pratica
			StatoPratica statoPraticaNecessariaIntegrazione = statoPraticaRepository
					.findById(Constants.ID_STATO_PRATICA_NECESSARIA_INTEGRAZIONE).orElseThrow(
							() -> new RuntimeException("Errore: stato pratica NECESSARIA INTEGRAZIONE non trovato"));
			pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_NECESSARIA_INTEGRAZIONE));
			pratica.setStatoPratica(statoPraticaNecessariaIntegrazione);
			pratica.setContatoreRichiesteIntegrazioni(pratica.getContatoreRichiesteIntegrazioni() == null ? 1
					: pratica.getContatoreRichiesteIntegrazioni() + 1);
			pratica.setUtenteModifica(utenteRichiedente);
			pratica.setDataModifica(LocalDateTime.now().withNano(0));

			/*aggiorno flag flagEsenzioneMarcaDaBollo*/
			if(
				richiestaIntegrazioneRequest.getFlagEsenzioneMarcaDaBollo() != null &&
				!"".equals(richiestaIntegrazioneRequest.getFlagEsenzioneMarcaDaBollo())
			){
				Boolean flagEsenzione = Boolean.valueOf(richiestaIntegrazioneRequest.getFlagEsenzioneMarcaDaBollo());
				pratica.getDatiRichiesta().setFlagEsenzioneMarcaDaBolloModificato(
					pratica.getDatiRichiesta().isFlagEsenzioneMarcaDaBolloModificato() ||
					!flagEsenzione.equals(new Boolean(pratica.getDatiRichiesta().isFlagEsenzioneMarcaDaBollo()))
				);
				pratica.getDatiRichiesta().setFlagEsenzioneMarcaDaBollo(flagEsenzione);
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

			praticaRepository.save(pratica);

			if (pratica.getRichiesteIntegrazioni() == null) {
				List<RichiestaIntegrazione> richiesteIntegrazioni = new ArrayList<>();
				pratica.setRichiesteIntegrazioni(richiesteIntegrazioni);
			}
			pratica.getRichiesteIntegrazioni().add(richiestaIntegrazione);

			// richiesta numero protocollo
			ProtocolloResponse protocolloResponse = null;

			if(callNewEndpoint){
				List<Allegato> allegatiList = new ArrayList<>();
				if(richiestaIntegrazioneRequest.getIdAllegato()!=null){
					Allegato allegato = allegatoRepository.findById(richiestaIntegrazioneRequest.getIdAllegato())
							.orElseThrow(
									() -> new BusinessException(ErrorCode.E1, "Errore: Allegato non presente nel sistema",
											richiestaIntegrazioneRequest.getIdAllegato()));

					// allegato.setFileAllegato(Base64.getEncoder().encode(allegato.getFileAllegato()));
					// (bug doppia codifica protocollo) Trattandosi di una transazione questo modificherebbe anche l'entità su db al successivo save (riga 173)
					// codifica spostata nel metodo getNumeroProtocolloUscita
					allegatiList.add(allegato);
				}

				protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(
						pratica,
						utenteRichiedente,
						Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_INTEGRAZIONE,
						listaDestinatari,
						true,
						Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA,
						allegatiList,
						PassaggioStato.RICHIESTA_PARERI_TO_NECESSARIA_INTEGRAZIONE,
						null);

			}else{
				protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(pratica,
						utenteRichiedente, Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_INTEGRAZIONE, listaDestinatari,
						true);
			}

			// inserisco protocollo sulla richiesta integrazione
			richiestaIntegrazione
					.setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());
			richiestaIntegrazione.setDataProtocollo(protocolloResponse.getDataProtocollo());

			richiestaIntegrazioneRepository.save(richiestaIntegrazione);

			//aggiorno codiceProtocollo dell'allegato
			if(richiestaIntegrazioneRequest.getIdAllegato()!=null){
				Allegato allegato = allegatoRepository.findById(richiestaIntegrazioneRequest.getIdAllegato())
						.orElseThrow(
								() -> new BusinessException(ErrorCode.E1, "Errore: Allegato non presente nel sistema",
										richiestaIntegrazioneRequest.getIdAllegato()));

				allegato.setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());

				allegatoRepository.save(allegato);
			}

			// inserimento comunicazione mail
			Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);
			this.sendEmailNotificaRichiestaIntegrazione(pratica, richiestaIntegrazione,
					protocolloInserimento.getCodiceProtocollo(), listaDestinatari, utenteRichiedente);

			return richiestaIntegrazioneMapper.entityToDto(richiestaIntegrazione);
		} else {
			Protocollo protocollo = this.gestionePreavvisoDiniego(pratica, utenteRichiedente, richiestaIntegrazioneRequest);
			RichiestaIntegrazioneDTO result = new RichiestaIntegrazioneDTO();
			result.setCodiceProtocollo(protocollo.getCodiceProtocollo());
			return result;
		}
	}

	@Override
	@LogEntryExit
	public Protocollo gestionePreavvisoDiniego(Pratica pratica, Utente utente, RichiestaIntegrazioneDTO richiestaIntegrazioneRequest) {

		// se c'e' stato già un prevviso diniego la pratica passa direttamente in
		// "pratica da rigettare"
		if (pratica.getFlagProceduraDiniego() != null && pratica.getFlagProceduraDiniego()) {
			// aggiorno stato della pratica in "pratica da rigettare"
			StatoPratica statoPraticaDaRigettare = statoPraticaRepository
					.findById(Constants.ID_STATO_PRATICA_DA_RIGETTARE)
					.orElseThrow(() -> new RuntimeException("Errore: stato pratica DA RIGETTARE non trovato"));
			pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_DA_RIGETTARE));
			pratica.setStatoPratica(statoPraticaDaRigettare);
			pratica.setDataModifica(LocalDateTime.now().withNano(0));
			praticaRepository.save(pratica);

			// inserimento comunicazioni mail
			Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);
			this.sendEmailNotificaPraticaDaRigettare(utente, pratica, protocolloInserimento.getCodiceProtocollo());
			return protocolloInserimento;
		} else {
			StatoPratica statoPraticaPreavvisoDiniego = statoPraticaRepository
					.findById(Constants.ID_STATO_PRATICA_PREAVVISO_DINIEGO)
					.orElseThrow(() -> new RuntimeException("Errore: stato pratica PREAVVISO DINIEGO non trovato"));

			List<Utente> listaDestinatari = getDestinatariPreavvisoDiniego(pratica);

			// inserimento richiesta integrazione
			RichiestaIntegrazione richiestaIntegrazione = this
					.performInsertRichiestaIntegrazione(richiestaIntegrazioneRequest, pratica, utente, true);

			pratica.setFlagProceduraDiniego(true);
			pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_PREAVVISO_DINIEGO));
			pratica.setStatoPratica(statoPraticaPreavvisoDiniego);
			pratica.setDataScadenzaPreavvisoDiniego(
					LocalDateTime.now().withNano(0).plusDays(numGiorniScadenzaPreavvisoDiniego));
			pratica.setDataModifica(LocalDateTime.now().withNano(0));
			pratica.setUtenteModifica(utente);

			praticaRepository.save(pratica);

			pratica.getRichiesteIntegrazioni().add(richiestaIntegrazione);

			// richiesta numero protocollo
			ProtocolloResponse protocolloResponse = null;

			if(callNewEndpoint){
				List<Allegato> allegatiList = new ArrayList<>();
				if(richiestaIntegrazioneRequest.getIdAllegato()!=null){
					Allegato allegato = allegatoRepository.findById(richiestaIntegrazioneRequest.getIdAllegato())
							.orElseThrow(
									() -> new BusinessException(ErrorCode.E1, "Errore: Allegato non presente nel sistema",
											richiestaIntegrazioneRequest.getIdAllegato()));

					// allegato.setFileAllegato(Base64.getEncoder().encode(allegato.getFileAllegato()));
					// (bug doppia codifica protocollo) Trattandosi di una transazione questo modificherebbe anche l'entità su db al successivo save (riga 173)
					// codifica spostata nel metodo getNumeroProtocolloUscita
					allegatiList.add(allegato);
				}
				protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(
						pratica,
						utente,
						Constants.TIPO_EVENTO_PROTOCOLLAZIONE_PREAVVISO_DINIEGO,
						listaDestinatari,
						true,
						Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA,
						allegatiList,
						PassaggioStato.RICHIESTA_PARERI_TO_PREAVVISO_DINIEGO,
						null);

			}else{
				protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(pratica, utente,
						Constants.TIPO_EVENTO_PROTOCOLLAZIONE_PREAVVISO_DINIEGO, listaDestinatari, true);
			}

			// inserimento protocollo sulla pratica
			Protocollo protocolloInserito = protocolloService.insertProtocollo(pratica, statoPraticaPreavvisoDiniego, protocolloResponse, null);

			// inserisco protocollo anche sulla richiesta integrazione
			richiestaIntegrazione
					.setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());
			richiestaIntegrazione.setDataProtocollo(protocolloResponse.getDataProtocollo());
			richiestaIntegrazioneRepository.save(richiestaIntegrazione);

			//aggiorno codiceProtocollo dell'allegato
			if(richiestaIntegrazioneRequest.getIdAllegato()!=null){
				Allegato allegato = allegatoRepository.findById(richiestaIntegrazioneRequest.getIdAllegato())
						.orElseThrow(
								() -> new BusinessException(ErrorCode.E1, "Errore: Allegato non presente nel sistema",
										richiestaIntegrazioneRequest.getIdAllegato()));

				allegato.setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());

				allegatoRepository.save(allegato);
			}

			// inserimento comunicazioni mail
			Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);
			this.sendEmailNotificaPreavvisoDiniego(listaDestinatari, utente, pratica,
					protocolloInserimento.getCodiceProtocollo(), protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno(),
					richiestaIntegrazione.getMotivoRichiesta());
			return protocolloInserito;
		}
	}

	@Override
	@Transactional
	@LogEntryExit
	public RichiestaIntegrazioneDTO insertRichiestaRettificaDate(Long idPratica, Long idUtenteRichiedente,
			String notaAlCittadino) {

		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Utente utenteRichiedente = utenteRepository.findById(idUtenteRichiedente)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema",
						idUtenteRichiedente));

		RichiestaIntegrazione richiestaIntegrazione = new RichiestaIntegrazione();
		richiestaIntegrazione.setPratica(pratica);
		richiestaIntegrazione.setUtenteRichiedente(utenteRichiedente);
		richiestaIntegrazione.setStatoPratica(pratica.getStatoPratica());
		richiestaIntegrazione.setTipoRichiesta(TipoRichiestaIntegrazione.RETTIFICA_DATE);
		richiestaIntegrazione.setMotivoRichiesta(notaAlCittadino);
		richiestaIntegrazione.setDataInserimento(LocalDateTime.now().withNano(0));
		richiestaIntegrazione
				.setDataScadenza(richiestaIntegrazione.getDataInserimento().plusDays(numGiorniScadenzaRettificaDate));

		richiestaIntegrazioneRepository.save(richiestaIntegrazione);

		// aggiorno stato della pratica
		StatoPratica statoPraticaRettificaDate = statoPraticaRepository
				.findById(Constants.ID_STATO_PRATICA_RETTIFICA_DATE)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica RETTIFICA DATE non trovato"));

		// aggiornamento pratica
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_RETTIFICA_DATE));
		pratica.setStatoPratica(statoPraticaRettificaDate);
		pratica.setUtenteModifica(utenteRichiedente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));
		praticaRepository.save(pratica);

		if (pratica.getRichiesteIntegrazioni() == null) {
			List<RichiestaIntegrazione> richiesteIntegrazioni = new ArrayList<>();
			pratica.setRichiesteIntegrazioni(richiesteIntegrazioni);
		}
		pratica.getRichiesteIntegrazioni().add(richiestaIntegrazione);

		// richiesta numero protocollo
		ProtocolloResponse protocolloResponse = null;
		List<Utente> listaDestinatari = this.getDestinatariRettificaDate(pratica);
		if(callNewEndpoint){

			protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(
					pratica,
					utenteRichiedente,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_RETTIFICA_DATE,
					listaDestinatari,
					true,
					Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA,
					null,
					PassaggioStato.RETTIFICA_DATE,
					null);

		}else{
			protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(pratica,
					utenteRichiedente, Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_RETTIFICA_DATE, new ArrayList<>(),
					true);
		}

		// inserisco protocollo sulla richiesta integrazione
		richiestaIntegrazione
				.setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());
		richiestaIntegrazione.setDataProtocollo(protocolloResponse.getDataProtocollo());
		richiestaIntegrazioneRepository.save(richiestaIntegrazione);

		// inserimento comunicazione mail
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);
		this.sendEmailNotificaRichiestaRettificaDate(pratica, utenteRichiedente, richiestaIntegrazione,
				protocolloInserimento.getCodiceProtocollo(), listaDestinatari);

		return richiestaIntegrazioneMapper.entityToDto(richiestaIntegrazione);
	}
	/**
	 * Effettua la creazione e il salvataggio di una nuova richiesta integrazione
	 * 
	 * 
	 * @param richiestaIntegrazioneRequest
	 * @param pratica
	 * @param utenteRichiedente
	 * @param isDiniego
	 * @return l'oggetto richiesta integrazione salvato
	 */
	@LogEntryExit
	private RichiestaIntegrazione performInsertRichiestaIntegrazione(
			RichiestaIntegrazioneDTO richiestaIntegrazioneRequest, Pratica pratica, Utente utenteRichiedente,
			boolean isDiniego) {

		RichiestaIntegrazione richiestaIntegrazione = new RichiestaIntegrazione();
		richiestaIntegrazione.setPratica(pratica);
		richiestaIntegrazione.setUtenteRichiedente(utenteRichiedente);
		richiestaIntegrazione.setStatoPratica(pratica.getStatoPratica());
		richiestaIntegrazione.setTipoRichiesta(isDiniego ? TipoRichiestaIntegrazione.PROCEDURA_DINIEGO
				: richiestaIntegrazioneRequest.getTipoRichiesta());
		richiestaIntegrazione.setMotivoRichiesta(richiestaIntegrazioneRequest.getMotivoRichiesta());
		richiestaIntegrazione.setDataInserimento(LocalDateTime.now().withNano(0));
		richiestaIntegrazione.setDataScadenza(richiestaIntegrazione.getDataInserimento()
				.plusDays(isDiniego ? numGiorniScadenzaPreavvisoDiniego : numGiorniScadenzaIntegrazione));

		richiestaIntegrazioneRepository.save(richiestaIntegrazione);

		if(richiestaIntegrazioneRequest.getIdAllegato()!=null){
			Allegato allegato = allegatoRepository.findById(richiestaIntegrazioneRequest.getIdAllegato())
					.orElseThrow(
							() -> new BusinessException(ErrorCode.E1, "Errore: Allegato non presente nel sistema",
									richiestaIntegrazioneRequest.getIdAllegato()));

			allegato.setPratica(pratica);
			allegato.setIdRichiestaIntegrazione(richiestaIntegrazione.getId());

			allegatoRepository.save(allegato);
		}

		return richiestaIntegrazione;
	}

	/**
	 * Costruisce la mail di notifica richiesta integrazione per il cittadino e
	 * inserisce la comunicazione email da inviare
	 * 
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param richiestaIntegrazione
	 */
	@LogEntryExit
	private void sendEmailNotificaRichiestaIntegrazione(Pratica pratica, RichiestaIntegrazione richiestaIntegrazione,
			String codiceProtocolloPratica, List<Utente> destinatari, Utente utente) {

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMail = new ComunicazioneMailInsertDTO();

		List<Allegato> allegati = allegatoRepository.findByIdRichiestaIntegrazione(richiestaIntegrazione.getId());

		if(allegati!=null&&allegati.size()>0){
			Allegato allegato=allegati.get(0);

			comunicazioneMail.setNomeFileAllegato(allegato.getNomeFile());
			comunicazioneMail.setMimeTypeFileAllegato(allegato.getMimeType());
			comunicazioneMail.setFileAllegato(allegato.getFileAllegato());
		}

		comunicazioneMail.setPratica(pratica);
		comunicazioneMail.setRichiestaIntegrazione(richiestaIntegrazione);
		comunicazioneMail.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione richiesta integrazione"));
		comunicazioneMail
				.setTesto(EmailUtils.getContentEmail(EmailUtils.getMessageRichiestaIntegrazioneCittadino(pratica,
						codiceProtocolloPratica, richiestaIntegrazione), "Comunicazione richiesta integrazione"));

		if (!pratica.getFirmatario().getEmail().isEmpty()) {
			// setto il flag che indica che e' una PEC
			comunicazioneMail.setFlagPec(true);

			comunicazioneMail.setDestinatari(pratica.getFirmatario().getEmail());

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMail.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMail);
		}

		// costruisco email per gli attori del municipio
		List<String> toMunicipio = new ArrayList<>();
		destinatari.stream().forEach(u -> toMunicipio.add(u.getEmail()));

		if (!toMunicipio.isEmpty()) {
			ComunicazioneMailInsertDTO comunicazioneMailAttori = new ComunicazioneMailInsertDTO(comunicazioneMail);

			// setto il flag che indica che e' una PEC
			comunicazioneMailAttori.setFlagPec(false);

			comunicazioneMailAttori
					.setTesto(EmailUtils.getContentEmail(EmailUtils.getMessageRichiestaIntegrazioneToAttori(pratica,
							codiceProtocolloPratica, richiestaIntegrazione), "Comunicazione richiesta integrazione"));

			comunicazioneMailAttori.setDestinatari(String.join(",", toMunicipio));

			if (utente != null) {
				comunicazioneMailAttori.setDestinatariCc(utente.getEmail());
			}
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailAttori);
		}
	}

	@Override
	@LogEntryExit
	public void sendEmailNotificaPreavvisoDiniego(List<Utente> destinatari, Utente utente, Pratica pratica,
			String codiceProtocolloPratica, String codiceProtocolloOperazione, String note) {

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMailCittadino = new ComunicazioneMailInsertDTO();
		comunicazioneMailCittadino.setPratica(pratica);
		comunicazioneMailCittadino
				.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione pratica rigettata: preavviso di diniego"));
		comunicazioneMailCittadino.setTesto(EmailUtils.getContentEmail(
				EmailUtils.getMessagePreavvisoDiniegoCittadino(pratica, codiceProtocolloPratica,
						codiceProtocolloOperazione, numGiorniScadenzaPreavvisoDiniego, note),
				"Comunicazione pratica rigettata: preavviso di diniego"));

		if (!pratica.getFirmatario().getEmail().isEmpty()) {
			// setto il flag che indica che e' una PEC
			comunicazioneMailCittadino.setFlagPec(true);

			comunicazioneMailCittadino.setDestinatari(pratica.getFirmatario().getEmail());

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMailCittadino.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailCittadino);
		}

		// costruisco email per gli attori del municipio
		List<String> toMunicipio = new ArrayList<>();
		destinatari.stream().forEach(u -> toMunicipio.add(u.getEmail()));

		if (!toMunicipio.isEmpty()) {
			ComunicazioneMailInsertDTO comunicazioneMailAttori = new ComunicazioneMailInsertDTO(
					comunicazioneMailCittadino);

			// setto il flag che indica che e' una PEC
			comunicazioneMailAttori.setFlagPec(false);

			comunicazioneMailAttori
					.setTesto(
							EmailUtils.getContentEmail(
									EmailUtils.getMessagePreavvisoDiniegoToAttori(pratica, codiceProtocolloPratica,
											codiceProtocolloOperazione),
									"Comunicazione pratica rigettata: preavviso di diniego"));

			comunicazioneMailAttori.setDestinatari(String.join(",", toMunicipio));

			if (utente != null) {
				comunicazioneMailAttori.setDestinatariCc(utente.getEmail());
			}
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailAttori);
		}
	}

	@Override
	@LogEntryExit
	public void sendEmailNotificaPraticaDaRigettare(Utente utente, Pratica pratica, String codiceProtocolloPratica) {

		// costruisco comunicazione email per l'istruttore di municipio
		ComunicazioneMailInsertDTO comunicazioneMailIstruttore = new ComunicazioneMailInsertDTO();
		comunicazioneMailIstruttore.setPratica(pratica);
		comunicazioneMailIstruttore.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione pratica da rigettare"));
		comunicazioneMailIstruttore.setTesto(EmailUtils.getContentEmail(
				EmailUtils.getMessagePraticaDaRigettareToAttori(pratica, codiceProtocolloPratica),
				"Comunicazione pratica da rigettare"));

		List<String> toMunicipio = new ArrayList<>();

		Gruppo gruppoIstruttoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_ISTRUTTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo istruttore municipio non trovato"));
		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoIstruttoreMunicipio);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		List<Utente> listaUtenti = utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);
		listaUtenti.stream().forEach(u -> toMunicipio.add(u.getEmail()));

		if (!toMunicipio.isEmpty()) {
			comunicazioneMailIstruttore.setDestinatari(String.join(",", toMunicipio));
			if (utente != null && !toMunicipio.contains(utente.getEmail())) {
				comunicazioneMailIstruttore.setDestinatariCc(utente.getEmail());
			}
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailIstruttore);
		}
	}

	/**
	 * Costruisce la mail di notifica richiesta rettifica date per il cittadino e
	 * inserisce la comunicazione email da inviare
	 * 
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param richiestaIntegrazione
	 */
	@LogEntryExit
	private void sendEmailNotificaRichiestaRettificaDate(
		Pratica pratica,
		Utente utente,
		RichiestaIntegrazione richiestaIntegrazione,
		String codiceProtocolloPratica,
		List<Utente> listaDestinatari
	) {
		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMail = new ComunicazioneMailInsertDTO();
		comunicazioneMail.setPratica(pratica);
		comunicazioneMail.setRichiestaIntegrazione(richiestaIntegrazione);
		comunicazioneMail
				.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione necessaria rettifica date occupazione"));
		comunicazioneMail.setTesto(EmailUtils.getContentEmail(EmailUtils
				.getMessageRichiestaRettificaDateCittadino(pratica, codiceProtocolloPratica, richiestaIntegrazione),
				"Comunicazione necessaria rettifica date occupazione"));

		if (!pratica.getFirmatario().getEmail().isEmpty()) {
			// setto il flag che indica che e' una PEC
			comunicazioneMail.setFlagPec(true);

			comunicazioneMail.setDestinatari(pratica.getFirmatario().getEmail());

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMail.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMail);
		}

		if (listaDestinatari.size() > 0) {
			ComunicazioneMailInsertDTO comunicazioneMailAttori = new ComunicazioneMailInsertDTO(comunicazioneMail);

			// setto il flag che indica che e' una PEC
			comunicazioneMailAttori.setFlagPec(false);

			comunicazioneMailAttori
					.setTesto(EmailUtils.getContentEmail(EmailUtils.getMessageRichiestaRettificaDateAttori(pratica,
							codiceProtocolloPratica, richiestaIntegrazione), "Comunicazione richiesta integrazione"));

			comunicazioneMailAttori.setDestinatari(
				String.join(",",
				listaDestinatari.stream().map(el -> el.getEmail()).collect(Collectors.toList()))
			);

			if (utente != null) {
				comunicazioneMailAttori.setDestinatariCc(utente.getEmail());
			}
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailAttori);
		}
	}

	/**
	 * Ritorna gli utenti destinatari per l'invio delle mail e per la
	 * protocollazione per l'operazione di preavviso diniego
	 * 
	 * @param pratica
	 * @return la lista dei destinatari
	 */
	private List<Utente> getDestinatariPreavvisoDiniego(Pratica pratica) {
		// ricerco utenti direttori di municipio
		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDirettoreMunicipio);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		List<Utente> listaUtenti = utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);

		// aggiungo le mail degli utenti coinvolti nelle richieste pareri
		// pratica.getRichiestePareri().stream().forEach(r -> listaUtenti.add(r.getUtenteRichiedente()));

		return listaUtenti;
	}

	/**
	 * Ritorna gli utenti destinatari per l'invio delle mail e per la
	 * protocollazione per l'operazione di richiesta integrazione
	 * 
	 * @param pratica
	 * @return la lista dei destinatari
	 */
	private List<Utente> getDestinatariRichiestaIntegrazione(Pratica pratica) {
		// ricerco utenti direttori di municipio
		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDirettoreMunicipio);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		List<Utente> listaUtenti = utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);

		// aggiungo le mail degli utenti coinvolti nelle richieste pareri
//		pratica.getRichiestePareri().stream().forEach(r -> listaUtenti.add(r.getUtenteRichiedente()));

		return listaUtenti;
	}
	/**
	 * Ritorna gli utenti destinatari per l'invio delle mail e per la
	 * rettifica date
	 *
	 * @param pratica
	 * @return la lista dei destinatari
	 */
	private List<Utente> getDestinatariRettificaDate(Pratica pratica) {
		// ricerco utenti direttori di municipio
		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDirettoreMunicipio);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		List<Utente> listaUtenti = utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);

		// aggiungo le mail degli utenti coinvolti nelle richieste pareri
//		pratica.getRichiestePareri().stream().forEach(r -> listaUtenti.add(r.getUtenteRichiedente()));

		return listaUtenti;
	}
	public List<RichiestaIntegrazione> findAllByProtocollo(String protocollo) {
		return this.richiestaIntegrazioneRepository.findAllByCodiceProtocolloLike("%" + protocollo + "%");
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
