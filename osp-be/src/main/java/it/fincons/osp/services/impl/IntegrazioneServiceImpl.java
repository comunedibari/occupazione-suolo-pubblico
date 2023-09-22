package it.fincons.osp.services.impl;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.ValidationException;

import it.fincons.osp.dto.*;
import it.fincons.osp.model.*;
import it.fincons.osp.model.TipoOperazioneVerificaOccupazione;
import it.fincons.osp.repository.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.IntegrazioneMapper;
import it.fincons.osp.payload.protocollazione.response.ProtocolloResponse;
import it.fincons.osp.payload.request.AllegatoIntegrazioneInsertRequest;
import it.fincons.osp.payload.request.DateOccupazioneEditRequest;
import it.fincons.osp.payload.request.IntegrazioneCompletaEgovInsertRequest;
import it.fincons.osp.payload.request.IntegrazioneEgovInsertRequest;
import it.fincons.osp.payload.request.RettificaDateEgovInsertRequest;
import it.fincons.osp.services.AllegatoService;
import it.fincons.osp.services.ComunicazioneMailService;
import it.fincons.osp.services.DatiRichiestaService;
import it.fincons.osp.services.IntegrazioneService;
import it.fincons.osp.services.ProtocollazioneService;
import it.fincons.osp.services.ProtocolloService;
import it.fincons.osp.utils.Constants;
import it.fincons.osp.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IntegrazioneServiceImpl implements IntegrazioneService {
	@Value("${osp.app.protocollo.callNewEndpoint}")
	private boolean callNewEndpoint;

	@Autowired
	IntegrazioneRepository integrazioneRepository;

	@Autowired
	RichiestaIntegrazioneRepository richiestaIntegrazioneRepository;

	@Autowired
	UtenteRepository utenteRepository;

	@Autowired
	ProtocollazioneService protocollazioneService;

	@Autowired
	ProtocolloService protocolloService;

	@Autowired
	StatoPraticaRepository statoPraticaRepository;

	@Autowired
	PraticaRepository praticaRepository;

	@Autowired
	AllegatoRepository allegatoRepository;

	@Autowired
	IntegrazioneMapper integrazioneMapper;

	@Autowired
	GruppoRepository gruppoRepository;

	@Autowired
	ComunicazioneMailService comunicazioneMailService;

	@Autowired
	AllegatoService allegatoService;

	@Autowired
	DatiRichiestaService datiRichiestaService;

	@Autowired
	DatiRichiestaRepository datiRichiestaRepository;

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional(Transactional.TxType.REQUIRED)
	public IntegrazioneDTO insertIntegrazione(IntegrazioneDTO integrazioneRequest) {
		RichiestaIntegrazione richiestaIntegrazione = richiestaIntegrazioneRepository
				.findById(integrazioneRequest.getIdRichiestaIntegrazione())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID richiesta integrazione non presente nel sistema",
						integrazioneRequest.getIdRichiestaIntegrazione()));

		Utente utenteIntegrazione = utenteRepository.findById(integrazioneRequest.getIdUtenteIntegrazione())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID utente integrazione non presente nel sistema",
						integrazioneRequest.getIdUtenteIntegrazione()));

		// inserisco integrazione
		IntegrazioneDTO result = this.performInsertIntegrazione(richiestaIntegrazione, utenteIntegrazione,
				integrazioneRequest);

		// aggiorno pratica
		this.aggiornaPraticaPostIntegrazione(richiestaIntegrazione, utenteIntegrazione);

		TypologicalDTO statoPratica=new TypologicalDTO();
		statoPratica.setId(richiestaIntegrazione.getPratica().getStatoPratica().getId());
		statoPratica.setDescrizione(richiestaIntegrazione.getPratica().getStatoPratica().getDescrizione());
		result.setStatoPratica(statoPratica);

		return result;
	}

	@Override
	@LogEntryExit(showArgs = true)
	public IntegrazioneDTO getIntegrazione(Long id) {
		Integrazione integrazione = integrazioneRepository.findById(id).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: id integrazione non presente nel sistema", id));

		return integrazioneMapper.entityToDto(integrazione);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public IntegrazioneDTO insertIntegrazioneCompletaEgov(
			IntegrazioneCompletaEgovInsertRequest integrazioneCompletaEgovInsertRequest) {

		if (integrazioneCompletaEgovInsertRequest.getIdPratica() == null
				|| StringUtils.isBlank(integrazioneCompletaEgovInsertRequest.getNumeroProtocollo())) {
			throw new ValidationException("Errore: i campi id pratica e numero protocollo sono obbligatori");
		}

		if (StringUtils.isBlank(integrazioneCompletaEgovInsertRequest.getNomeCittadinoEgov())
				|| StringUtils.isBlank(integrazioneCompletaEgovInsertRequest.getCognomeCittadinoEgov())
				|| StringUtils.isBlank(integrazioneCompletaEgovInsertRequest.getCfCittadinoEgov())) {
			throw new BusinessException(ErrorCode.E30,
					"Il nome, il cognome e il codice fiscale del cittadino sono obbligatori");
		}

		// gestione recupero utente EGOV
		Gruppo gruppoEgov = gruppoRepository.findById(Constants.ID_GRUPPO_EGOV)
				.orElseThrow(() -> new RuntimeException("Errore: ID gruppo egov non presente nel sistema"));
		List<Utente> utentiEgov = utenteRepository.findByGruppo(gruppoEgov);
		if (utentiEgov.isEmpty()) {
			throw new RuntimeException("Nessun utente EGOV impostato nel sistema");
		}

		// recupero pratica
		Pratica pratica = praticaRepository.findById(integrazioneCompletaEgovInsertRequest.getIdPratica()).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						integrazioneCompletaEgovInsertRequest.getIdPratica()));

		// recupero richiesta integrazione senza risposta
		if (CollectionUtils.isEmpty(pratica.getRichiesteIntegrazioni())) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: nessuna richiesta integrazione presente per questa pratica");
		}

		Optional<RichiestaIntegrazione> richiestaIntegrazioneTrovata = pratica.getRichiesteIntegrazioni().stream().filter(r -> !r.isFlagInseritaRisposta()).findFirst();

		if (richiestaIntegrazioneTrovata.isEmpty()) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: nessuna richiesta integrazione in attesa di risposta presente per questa pratica");
		}

		RichiestaIntegrazione richiestaIntegrazione = richiestaIntegrazioneTrovata.get();

		// inserisco allegati
		for (AllegatoDTO allegato : integrazioneCompletaEgovInsertRequest.getAllegati()) {
			AllegatoIntegrazioneInsertRequest allegatoIntegrazioneInsertRequest = new AllegatoIntegrazioneInsertRequest();
			allegatoIntegrazioneInsertRequest.setIdRichiestaIntegrazione(richiestaIntegrazione.getId());
			allegatoIntegrazioneInsertRequest.setAllegato(allegato);

			allegatoService.insertAllegatoIntegrazione(allegatoIntegrazioneInsertRequest);
		}

		// inserisco integrazione
		IntegrazioneDTO integrazioneRequest = new IntegrazioneDTO();
		integrazioneRequest.setIdRichiestaIntegrazione(richiestaIntegrazione.getId());
		integrazioneRequest.setIdUtenteIntegrazione(utentiEgov.get(0).getId());
		integrazioneRequest.setMotivoIntegrazione(integrazioneCompletaEgovInsertRequest.getMotivoIntegrazione());
		integrazioneRequest.setNomeCittadinoEgov(integrazioneCompletaEgovInsertRequest.getNomeCittadinoEgov());
		integrazioneRequest.setCognomeCittadinoEgov(integrazioneCompletaEgovInsertRequest.getCognomeCittadinoEgov());
		integrazioneRequest.setCfCittadinoEgov(integrazioneCompletaEgovInsertRequest.getCfCittadinoEgov());

		//setto le informazioni relative alla protocollazione
		integrazioneRequest.setNumeroProtocollo(integrazioneCompletaEgovInsertRequest.getNumeroProtocollo());
		integrazioneRequest.setAnno(integrazioneCompletaEgovInsertRequest.getAnno());
		integrazioneRequest.setDataProtocollo(integrazioneCompletaEgovInsertRequest.getDataProtocollo());
		integrazioneRequest.setOriginEgov(true);

		IntegrazioneDTO result = this.insertIntegrazione(integrazioneRequest);

		log.info("Inserimento integrazione completa da EGOV avvenuto con successo");

		return result;
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public IntegrazioneDTO insertIntegrazioneEgov(IntegrazioneEgovInsertRequest integrazioneEgovInsertRequest) {
		if (integrazioneEgovInsertRequest.getIdPratica() == null
				&& StringUtils.isBlank(integrazioneEgovInsertRequest.getCodiceProtocollo())) {
			throw new ValidationException("Errore: è obbligatorio almeno uno fra Id Pratica e Codice Protocollo");
		}

		if (StringUtils.isBlank(integrazioneEgovInsertRequest.getNomeCittadinoEgov())
				|| StringUtils.isBlank(integrazioneEgovInsertRequest.getCognomeCittadinoEgov())
				|| StringUtils.isBlank(integrazioneEgovInsertRequest.getCfCittadinoEgov())) {
			throw new BusinessException(ErrorCode.E30,
					"Il nome, il cognome e il codice fiscale del cittadino sono obbligatori");
		}

		// gestione recupero utente EGOV
		Gruppo gruppoEgov = gruppoRepository.findById(Constants.ID_GRUPPO_EGOV)
				.orElseThrow(() -> new RuntimeException("Errore: ID gruppo egov non presente nel sistema"));
		List<Utente> utentiEgov = utenteRepository.findByGruppo(gruppoEgov);
		if (utentiEgov.isEmpty()) {
			throw new RuntimeException("Nessun utente EGOV impostato nel sistema");
		}

		// recupero pratica
		Pratica pratica;
		if (integrazioneEgovInsertRequest.getIdPratica() != null) {
			pratica = praticaRepository.findById(integrazioneEgovInsertRequest.getIdPratica()).orElseThrow(
					() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
							integrazioneEgovInsertRequest.getIdPratica()));
		} else {
			Protocollo protocollo = protocolloService
					.getProtocolloByCodice(integrazioneEgovInsertRequest.getCodiceProtocollo());
			if (protocollo == null) {
				throw new BusinessException(ErrorCode.E21, "Errore: codice protocollo non presente nel sistema",
						integrazioneEgovInsertRequest.getCodiceProtocollo());
			}

			pratica = protocollo.getPratica();
		}

		// recupero richiesta integrazione senza risposta
		if (CollectionUtils.isEmpty(pratica.getRichiesteIntegrazioni())) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: nessuna richiesta integrazione presente per questa pratica");
		}

		Optional<RichiestaIntegrazione> richiestaIntegrazioneTrovata = pratica.getRichiesteIntegrazioni().stream()
				.filter(r -> !r.isFlagInseritaRisposta()).findFirst();

		if (richiestaIntegrazioneTrovata.isEmpty()) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: nessuna richiesta integrazione in attesa di risposta presente per questa pratica");
		}

		RichiestaIntegrazione richiestaIntegrazione = richiestaIntegrazioneTrovata.get();

		// inserisco integrazione
		IntegrazioneDTO integrazioneRequest = new IntegrazioneDTO();
		integrazioneRequest.setIdRichiestaIntegrazione(richiestaIntegrazione.getId());
		integrazioneRequest.setIdUtenteIntegrazione(utentiEgov.get(0).getId());
		integrazioneRequest.setMotivoIntegrazione(integrazioneEgovInsertRequest.getMotivoIntegrazione());
		integrazioneRequest.setNomeCittadinoEgov(integrazioneEgovInsertRequest.getNomeCittadinoEgov());
		integrazioneRequest.setCognomeCittadinoEgov(integrazioneEgovInsertRequest.getCognomeCittadinoEgov());
		integrazioneRequest.setCfCittadinoEgov(integrazioneEgovInsertRequest.getCfCittadinoEgov());

		IntegrazioneDTO result = this.insertIntegrazione(integrazioneRequest);

		log.info("Inserimento integrazione da EGOV avvenuto con successo");

		return result;
	}

	/**
	 * Inizializza e salva un nuovo oggetto  ed invia la mail di
	 * comunicazione all'istruttore di municipio
	 * 
	 * @param richiestaIntegrazione
	 * @param utenteIntegrazione
	 * @param integrazioneRequest
	 * @return le informazioni dell'integrazione inserita
	 */
	@Transactional(Transactional.TxType.REQUIRED)
	@LogEntryExit
	private IntegrazioneDTO performInsertIntegrazione(RichiestaIntegrazione richiestaIntegrazione,
			Utente utenteIntegrazione, IntegrazioneDTO integrazioneRequest) {

		Pratica pratica = richiestaIntegrazione.getPratica();

		if (!pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_NECESSARIA_INTEGRAZIONE)
				&& !pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_PREAVVISO_DINIEGO)) {
			throw new BusinessException(ErrorCode.E13,
					"Errore: lo stato della pratica non è coerente per l'inserimento di integrazioni e il passaggio di stato in 'Verifica formale'",
					pratica.getStatoPratica().getId());
		}

		if (pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_PREAVVISO_DINIEGO)) {

			// controllo data scadenza preavviso diniego
			if (LocalDateTime.now().withNano(0).isAfter(pratica.getDataScadenzaPreavvisoDiniego())) {
				throw new BusinessException(ErrorCode.E13,
						"Errore: non è possibile inserire una risposta al preavviso di diniego in quanto è stata superata la data limite per l'integrazione");
			}

			// resetto la data della scadenza del preavviso diniego, in quanto c'è stata una
			// risposta entro il tempo prestabilito
			pratica.setDataScadenzaPreavvisoDiniego(null);
		}

		// inserimento integrazione
		Integrazione integrazione = new Integrazione();
		integrazione.setRichiestaIntegrazione(richiestaIntegrazione);
		integrazione.setUtenteIntegrazione(utenteIntegrazione);
		integrazione.setMotivoIntegrazione(integrazioneRequest.getMotivoIntegrazione());

		integrazione.setNomeCittadinoEgov(integrazioneRequest.getNomeCittadinoEgov());
		integrazione.setCognomeCittadinoEgov(integrazioneRequest.getCognomeCittadinoEgov());
		integrazione.setCfCittadinoEgov(integrazioneRequest.getCfCittadinoEgov());

		integrazione.setDataInserimento(LocalDateTime.now().withNano(0));

		integrazioneRepository.save(integrazione);

		// aggiorno flag risposta in richiesta integrazione
		richiestaIntegrazione.setFlagInseritaRisposta(true);
		richiestaIntegrazioneRepository.save(richiestaIntegrazione);

		// aggiornamento fittizio pratica per richiesta protocollo e inserimento storico
		pratica.setUtenteModifica(utenteIntegrazione);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		pratica.setNomeCittadinoEgov(integrazioneRequest.getNomeCittadinoEgov());
		pratica.setCognomeCittadinoEgov(integrazioneRequest.getCognomeCittadinoEgov());
		pratica.setCfCittadinoEgov(integrazioneRequest.getCfCittadinoEgov());
		praticaRepository.save(pratica);

		richiestaIntegrazione.setIntegrazione(integrazione);
		pratica.getRichiesteIntegrazioni().removeIf(r -> r.getId().equals(richiestaIntegrazione.getId()));
		pratica.getRichiesteIntegrazioni().add(richiestaIntegrazione);

		// richiesta numero protocollo
		// nel caso di egov la protoccollazione è già avvenuta quindi prendiamo le info passate in input
		if(StringUtils.isBlank(integrazioneRequest.getNumeroProtocollo())){
			ProtocolloResponse protocolloResponse = null;

			if(callNewEndpoint){

				PassaggioStato passaggioStato=null;

				if (pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_NECESSARIA_INTEGRAZIONE)) {
					passaggioStato=PassaggioStato.NECESSARIA_INTEGRAZIONE_TO_VERIFICA_FORMALE;
				}else{
					passaggioStato=PassaggioStato.PREAVVISO_DINIEGO_TO_VERIFICA_FORMALE;
				}

				//recupero allegati pratica
				List<Allegato> allegati=allegatoRepository.findByIdRichiestaIntegrazione(richiestaIntegrazione.getId());
				allegati = allegati.stream().filter((el) -> el.getCodiceProtocollo() == null).collect(Collectors.toList());

				protocolloResponse = protocollazioneService.getNumeroProtocolloEntrata(
						pratica,
						utenteIntegrazione,
						Constants.TIPO_EVENTO_PROTOCOLLAZIONE_INTEGRAZIONE,
						Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA,
						allegati,
						passaggioStato);

			}else{
				protocolloResponse = protocollazioneService.getNumeroProtocolloEntrata(pratica, utenteIntegrazione, Constants.TIPO_EVENTO_PROTOCOLLAZIONE_INTEGRAZIONE);
			}

			integrazione.setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());
			integrazione.setDataProtocollo(protocolloResponse.getDataProtocollo());
			integrazione.setNumeroProtocollo(protocolloResponse.getNumeroProtocollo());
			integrazione.setAnno(protocolloResponse.getAnno());
		}else{
			integrazione.setCodiceProtocollo(integrazioneRequest.getNumeroProtocollo() + "|" + integrazioneRequest.getAnno());
			integrazione.setDataProtocollo(integrazioneRequest.getDataProtocollo());
			integrazione.setNumeroProtocollo(integrazioneRequest.getNumeroProtocollo());
			integrazione.setAnno(integrazioneRequest.getAnno());
		}

		integrazioneRepository.save(integrazione);

		// aggiorno allegati integrazione
		allegatoService.updateAllegatiIntegrazione(integrazione);

		log.info("Passaggio pratica in stato VERIFICA FORMALE avvenuto con successo");

		// invio mail di notifica
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);

		this.sendEmailNotificaAvvenutaIntegrazione(utenteIntegrazione, pratica,	protocolloInserimento.getCodiceProtocollo(), integrazione.getCodiceProtocollo());

		return integrazioneMapper.entityToDto(integrazione);
	}

	/**
	 * Aggiorna lo stato della pratica e la data scadenza dopo che è stata fatta
	 * un'integrazione
	 * 
	 * @param richiestaIntegrazione
	 * @param utenteIntegrazione
	 */
	@Transactional(Transactional.TxType.REQUIRED)
	@LogEntryExit
	private void aggiornaPraticaPostIntegrazione(RichiestaIntegrazione richiestaIntegrazione,
			Utente utenteIntegrazione) {

		Pratica pratica = richiestaIntegrazione.getPratica();

		StatoPratica statoPraticaVerificaFormale = statoPraticaRepository
				.findById(Constants.ID_STATO_PRATICA_VERIFICA_FORMALE)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica VERIFICA FORMALE non trovato"));

		// aggiornamento dati richiesta
		if (pratica.getDatiRichiesta().getTipoOperazioneVerificaOccupazione() != null
				&& pratica.getDatiRichiesta().getTipoOperazioneVerificaOccupazione()
						.equals(TipoOperazioneVerificaOccupazione.SALTA_VERIFICA_OCCUPAZIONE)) {
			pratica.getDatiRichiesta().setTipoOperazioneVerificaOccupazione(null);
			datiRichiestaRepository.save(pratica.getDatiRichiesta());
		}

		// aggiornamento pratica
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_VERIFICA_FORMALE));
		pratica.setStatoPratica(statoPraticaVerificaFormale);

		pratica.setUtenteModifica(utenteIntegrazione);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));
		pratica.setDataScadenzaPratica(this.getNewDataScadenzaPratica(pratica.getDataScadenzaPratica(),
				richiestaIntegrazione.getDataInserimento()));

		praticaRepository.save(pratica);
	}

	@Override
	@Transactional
	@LogEntryExit(showArgs = true)
	public IntegrazioneDTO insertRettificaDate(IntegrazioneDTO integrazioneRequest) {
		RichiestaIntegrazione richiestaIntegrazione = richiestaIntegrazioneRepository
				.findById(integrazioneRequest.getIdRichiestaIntegrazione())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID richiesta integrazione non presente nel sistema",
						integrazioneRequest.getIdRichiestaIntegrazione()));

		Utente utenteIntegrazione = utenteRepository.findById(integrazioneRequest.getIdUtenteIntegrazione())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID utente integrazione non presente nel sistema",
						integrazioneRequest.getIdUtenteIntegrazione()));

		Pratica pratica = richiestaIntegrazione.getPratica();

		if (!pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_RETTIFICA_DATE)
				&& !pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_PREAVVISO_DINIEGO)) {
			throw new BusinessException(ErrorCode.E13,
					"Errore: lo stato della pratica non è coerente per l'inserimento di una rettifica date e il passaggio di stato in 'Verifica formale'",
					pratica.getStatoPratica().getId());
		}

		if (pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_PREAVVISO_DINIEGO)) {

			// controllo data scadenza preavviso diniego
			if (LocalDateTime.now().withNano(0).isAfter(pratica.getDataScadenzaPreavvisoDiniego())) {
				throw new BusinessException(ErrorCode.E13,
						"Errore: non è possibile inserire una risposta al preavviso di diniego in quanto è stata superata la data limite per l'integrazione");
			}

			// resetto la data della scadenza del preavviso diniego, in quanto c'è stata una
			// risposta entro il tempo prestabilito
			pratica.setDataScadenzaPreavvisoDiniego(null);
		}

		LocalDateTime dtInizio = integrazioneRequest.getDataInizioOccupazione().atStartOfDay();
		if(integrazioneRequest.getOraInizioOccupazione() != null) {
			dtInizio = LocalDateTime.of(integrazioneRequest.getDataInizioOccupazione(), integrazioneRequest.getOraInizioOccupazione());
		}

		LocalDateTime dtInizioRichiesta = pratica.getDatiRichiesta().getDataInizioOccupazione().atStartOfDay();
		if(pratica.getDatiRichiesta().getOraInizioOccupazione() != null) {
			dtInizioRichiesta = LocalDateTime.of(pratica.getDatiRichiesta().getDataInizioOccupazione(), pratica.getDatiRichiesta().getOraInizioOccupazione());
		}

		if (!dtInizio.isAfter(dtInizioRichiesta)) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: la nuova data di inizio occupazione deve essere successiva alla data di inizio precedentemente richiesta");
		}

		// aggiornamento date occupazione sui dati richiesta
		DateOccupazioneEditRequest dateOccupazioneEditRequest = new DateOccupazioneEditRequest();
		dateOccupazioneEditRequest.setDataInizioOccupazione(integrazioneRequest.getDataInizioOccupazione());
		dateOccupazioneEditRequest.setOraInizioOccupazione(integrazioneRequest.getOraInizioOccupazione());
		dateOccupazioneEditRequest.setDataScadenzaOccupazione(integrazioneRequest.getDataScadenzaOccupazione());
		dateOccupazioneEditRequest.setOraScadenzaOccupazione(integrazioneRequest.getOraScadenzaOccupazione());
		datiRichiestaService.editDateOccupazione(pratica, dateOccupazioneEditRequest);

		// inserimento integrazione
		Integrazione integrazione = new Integrazione();
		integrazione.setRichiestaIntegrazione(richiestaIntegrazione);
		integrazione.setUtenteIntegrazione(utenteIntegrazione);
		integrazione.setDataInizioOccupazione(integrazioneRequest.getDataInizioOccupazione());
		integrazione.setOraInizioOccupazione(integrazioneRequest.getOraInizioOccupazione());
		integrazione.setDataScadenzaOccupazione(integrazioneRequest.getDataScadenzaOccupazione());
		integrazione.setOraScadenzaOccupazione(integrazioneRequest.getOraScadenzaOccupazione());

		integrazione.setDataInserimento(LocalDateTime.now().withNano(0));

		integrazione.setNomeCittadinoEgov(integrazioneRequest.getNomeCittadinoEgov());
		integrazione.setCognomeCittadinoEgov(integrazioneRequest.getCognomeCittadinoEgov());
		integrazione.setCfCittadinoEgov(integrazioneRequest.getCfCittadinoEgov());

		integrazioneRepository.save(integrazione);

		// aggiorno flag risposta in richiesta integrazione
		richiestaIntegrazione.setFlagInseritaRisposta(true);
		richiestaIntegrazioneRepository.save(richiestaIntegrazione);

		// aggiornamento fittizio pratica per richiesta protocollo e inserimento storico
		pratica.setUtenteModifica(utenteIntegrazione);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		pratica.setNomeCittadinoEgov(integrazioneRequest.getNomeCittadinoEgov());
		pratica.setCognomeCittadinoEgov(integrazioneRequest.getCognomeCittadinoEgov());
		pratica.setCfCittadinoEgov(integrazioneRequest.getCfCittadinoEgov());
		pratica.setOriginEgov(integrazioneRequest.isOriginEgov());
		
		praticaRepository.save(pratica);

		richiestaIntegrazione.setIntegrazione(integrazione);
		pratica.getRichiesteIntegrazioni().removeIf(r -> r.getId().equals(richiestaIntegrazione.getId()));
		pratica.getRichiesteIntegrazioni().add(richiestaIntegrazione);

		// per EGOV non viene più effettuata la chiamata verso il middleware.
		// le informazioni di protocollazione ci vengono fornite in input

		ProtocolloResponse protocolloResponse = null;

		if(StringUtils.isBlank(integrazioneRequest.getNumeroProtocollo())){
			// richiesta numero protocollo
			if(callNewEndpoint){
				PassaggioStato passaggioStato=null;

				if (pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_RETTIFICA_DATE)) {
					passaggioStato=PassaggioStato.RETTIFICA_DATE_TO_VERIFICA_FORMALE;
				}else{
					passaggioStato=PassaggioStato.PREAVVISO_DINIEGO_TO_VERIFICA_FORMALE;
				}

				//recupero allegati pratica
				// List<Allegato> allegati=allegatoRepository.findByPraticaAndStatoPratica(pratica.getId(), pratica.getStatoPratica().getId());

				protocolloResponse = protocollazioneService.getNumeroProtocolloEntrata(
						pratica,
						utenteIntegrazione,
						Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RETTIFICA_DATE,
						Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA,
						null,
						passaggioStato);

			}else{
				protocolloResponse = protocollazioneService.getNumeroProtocolloEntrata(pratica, utenteIntegrazione, Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RETTIFICA_DATE);
			}
		}else{
			protocolloResponse = new ProtocolloResponse();
			protocolloResponse.setNumeroProtocollo(integrazioneRequest.getNumeroProtocollo());
			protocolloResponse.setAnno(integrazioneRequest.getAnno());
			protocolloResponse.setDataProtocollo(integrazioneRequest.getDataProtocollo());
		}

		integrazione.setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());
		integrazione.setDataProtocollo(protocolloResponse.getDataProtocollo());
		integrazione.setNumeroProtocollo(protocolloResponse.getNumeroProtocollo());
		integrazione.setAnno(protocolloResponse.getAnno());

		integrazioneRepository.save(integrazione);

		// invio mail di notifica
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);

		this.sendEmailNotificaAvvenutaRettificaDate(utenteIntegrazione, pratica, protocolloInserimento.getCodiceProtocollo(), integrazione.getCodiceProtocollo());

		this.aggiornaPraticaPostIntegrazione(richiestaIntegrazione, utenteIntegrazione);

		return integrazioneMapper.entityToDto(integrazione);
	}

	@Override
	@Transactional
	@LogEntryExit(showArgs = true)
	public IntegrazioneDTO insertRettificaDateEgov(RettificaDateEgovInsertRequest rettificaDateEgovInsertRequest) {
		if (rettificaDateEgovInsertRequest.getIdPratica() == null
				&& StringUtils.isBlank(rettificaDateEgovInsertRequest.getNumeroProtocollo())) {
			throw new ValidationException("Errore: è obbligatorio almeno uno fra Id Pratica e Numero Protocollo");
		}

		if (StringUtils.isBlank(rettificaDateEgovInsertRequest.getNomeCittadinoEgov())
				|| StringUtils.isBlank(rettificaDateEgovInsertRequest.getCognomeCittadinoEgov())
				|| StringUtils.isBlank(rettificaDateEgovInsertRequest.getCfCittadinoEgov())) {
			throw new BusinessException(ErrorCode.E30,
					"Il nome, il cognome e il codice fiscale del cittadino sono obbligatori");
		}

		// gestione recupero utente EGOV
		Gruppo gruppoEgov = gruppoRepository.findById(Constants.ID_GRUPPO_EGOV)
				.orElseThrow(() -> new RuntimeException("Errore: ID gruppo egov non presente nel sistema"));
		List<Utente> utentiEgov = utenteRepository.findByGruppo(gruppoEgov);
		if (utentiEgov.isEmpty()) {
			throw new RuntimeException("Nessun utente EGOV impostato nel sistema");
		}

		// recupero pratica
		Pratica pratica;
		if (rettificaDateEgovInsertRequest.getIdPratica() != null) {
			pratica = praticaRepository.findById(rettificaDateEgovInsertRequest.getIdPratica()).orElseThrow(
					() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
							rettificaDateEgovInsertRequest.getIdPratica()));
		} else {
			Protocollo protocollo = protocolloService
					.getProtocolloByNumeroProtcollo(rettificaDateEgovInsertRequest.getNumeroProtocollo());
			if (protocollo == null) {
				throw new BusinessException(ErrorCode.E21, "Errore: numero protocollo non presente nel sistema",
						rettificaDateEgovInsertRequest.getNumeroProtocollo());
			}

			pratica = protocollo.getPratica();
		}

		// recupero richiesta integrazione senza risposta
		if (CollectionUtils.isEmpty(pratica.getRichiesteIntegrazioni())) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: nessuna richiesta integrazione presente per questa pratica");
		}

		Optional<RichiestaIntegrazione> richiestaIntegrazioneTrovata = pratica.getRichiesteIntegrazioni().stream()
				.filter(r -> !r.isFlagInseritaRisposta()).findFirst();

		if (richiestaIntegrazioneTrovata.isEmpty()) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: nessuna richiesta integrazione in attesa di risposta presente per questa pratica");
		}

		RichiestaIntegrazione richiestaIntegrazione = richiestaIntegrazioneTrovata.get();

		// inserisco integrazione
		IntegrazioneDTO integrazioneRequest = new IntegrazioneDTO();
		integrazioneRequest.setIdRichiestaIntegrazione(richiestaIntegrazione.getId());
		integrazioneRequest.setIdUtenteIntegrazione(utentiEgov.get(0).getId());
		integrazioneRequest.setDataInizioOccupazione(rettificaDateEgovInsertRequest.getDataInizioOccupazione());
		integrazioneRequest.setOraInizioOccupazione(rettificaDateEgovInsertRequest.getOraInizioOccupazione());
		integrazioneRequest.setDataScadenzaOccupazione(rettificaDateEgovInsertRequest.getDataScadenzaOccupazione());
		integrazioneRequest.setOraScadenzaOccupazione(rettificaDateEgovInsertRequest.getOraScadenzaOccupazione());

		integrazioneRequest.setNomeCittadinoEgov(rettificaDateEgovInsertRequest.getNomeCittadinoEgov());
		integrazioneRequest.setCognomeCittadinoEgov(rettificaDateEgovInsertRequest.getCognomeCittadinoEgov());
		integrazioneRequest.setCfCittadinoEgov(rettificaDateEgovInsertRequest.getCfCittadinoEgov());

		integrazioneRequest.setNumeroProtocollo(rettificaDateEgovInsertRequest.getNumeroProtocollo());
		integrazioneRequest.setDataProtocollo(rettificaDateEgovInsertRequest.getDataProtocollo());
		integrazioneRequest.setAnno(rettificaDateEgovInsertRequest.getAnno());

		integrazioneRequest.setOriginEgov(rettificaDateEgovInsertRequest.isOriginEgov());

		IntegrazioneDTO result = this.insertRettificaDate(integrazioneRequest);

		result.setId(rettificaDateEgovInsertRequest.getIdPratica());

		log.info("Inserimento rettifica date da EGOV avvenuto con successo");

		return result;
	}

	/**
	 * Costruisce le mail di notifica integrazione pratica e le invia all'istruttore
	 * di municipio
	 * 
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloIntegrazione
	 */
	private void sendEmailNotificaAvvenutaIntegrazione(Utente utente, Pratica pratica, String codiceProtocolloPratica,
			String codiceProtocolloIntegrazione) {

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMail = new ComunicazioneMailInsertDTO();
		comunicazioneMail.setPratica(pratica);
		comunicazioneMail.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione integrazione documentazione"));
		comunicazioneMail.setTesto(EmailUtils.getContentEmail(EmailUtils.getMessageReInvioPraticaToCittadino(pratica,
				codiceProtocolloPratica, codiceProtocolloIntegrazione), "Comunicazione integrazione documentazione"));

		if (!pratica.getFirmatario().getEmail().isEmpty()) {
			// setto il flag che indica se e' una PEC
			comunicazioneMail.setFlagPec(true);

			comunicazioneMail.setDestinatari(pratica.getFirmatario().getEmail());

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMail.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMail);
		}

		// costruisco mail per l'istruttore di municipio
		ComunicazioneMailInsertDTO comunicazioneMailInsertMunicipio = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsertMunicipio.setPratica(pratica);
		comunicazioneMailInsertMunicipio
				.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione integrazione documentazione"));
		comunicazioneMailInsertMunicipio.setTesto(
				EmailUtils.getContentEmail(EmailUtils.getMessageReInvioPraticaToAttori(pratica, codiceProtocolloPratica,
						codiceProtocolloIntegrazione), "Comunicazione integrazione documentazione"));

		Set<String> toMunicipio = new HashSet<>();

		// ricerco utenti istruttori di municipio
		Gruppo gruppoIstruttoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_ISTRUTTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo istruttore municipio non trovato"));
		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoIstruttoreMunicipio);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		List<Utente> listaUtenti = utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);

		// aggiungo le mail degli utenti coinvolti nelle richieste pareri
		pratica.getRichiestePareri().stream().forEach(r -> listaUtenti.add(r.getUtenteRichiedente()));

		listaUtenti.stream().forEach(u -> toMunicipio.add(u.getEmail()));

		if (!toMunicipio.isEmpty()) {
			if (toMunicipio.size() > 1) {
				toMunicipio.remove(utente.getEmail());
				comunicazioneMailInsertMunicipio.setDestinatariCc(utente.getEmail());
			}
			comunicazioneMailInsertMunicipio.setDestinatari(String.join(",", toMunicipio));
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertMunicipio);
		}
	}

	/**
	 * Costruisce le mail di notifica rettifica date occupazione e le invia
	 * all'istruttore di municipio e agli attori coinvolti in caso di richieste
	 * pareri attive
	 * 
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloIntegrazione
	 */
	private void sendEmailNotificaAvvenutaRettificaDate(Utente utente, Pratica pratica, String codiceProtocolloPratica,
			String codiceProtocolloIntegrazione) {

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMail = new ComunicazioneMailInsertDTO();
		comunicazioneMail.setPratica(pratica);
		comunicazioneMail.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione rettifica date occupazione pratica"));
		comunicazioneMail.setTesto(EmailUtils.getContentEmail(EmailUtils.getMessageRettificaDateCittadino(pratica,
				codiceProtocolloPratica, codiceProtocolloIntegrazione), "Comunicazione rettifica date occupazione pratica"));

		if (!pratica.getFirmatario().getEmail().isEmpty()) {
			// setto il flag che indica se e' una PEC
			comunicazioneMail.setFlagPec(true);

			comunicazioneMail.setDestinatari(pratica.getFirmatario().getEmail());

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMail.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMail);
		}
		// costruisco mail per l'istruttore di municipio

		ComunicazioneMailInsertDTO comunicazioneMailInsertMunicipio = new ComunicazioneMailInsertDTO(comunicazioneMail);
		comunicazioneMailInsertMunicipio.setFlagPec(false);
		comunicazioneMailInsertMunicipio.setPratica(pratica);
		comunicazioneMailInsertMunicipio
				.setOggetto(this.buildOggettoEmail(pratica, "Comunicazione rettifica date occupazione pratica"));
		comunicazioneMailInsertMunicipio.setTesto(
				EmailUtils.getContentEmail(EmailUtils.getMessageRettificaDateToAttori(pratica, codiceProtocolloPratica,
						codiceProtocolloIntegrazione), "Comunicazione rettifica date occupazione pratica"));

		Set<String> toMunicipio = new HashSet<>();

		// ricerco utenti istruttori di municipio
		Gruppo gruppoIstruttoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_ISTRUTTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo istruttore municipio non trovato"));
		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoIstruttoreMunicipio);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		Set<Utente> listaUtenti = new HashSet<>();
		listaUtenti.addAll(utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi));

		// recupero utenti dei gruppi ai quali e' stato richiesto un parere ed ancora
		// non e' stato inserito
		List<RichiestaParere> listaRichiestePareri = pratica.getRichiestePareri().stream()
				.filter(r -> !r.isFlagInseritaRisposta()).collect(Collectors.toList());

		if (!listaRichiestePareri.isEmpty()) {
			List<Gruppo> listaGruppiDestinatariRichiestePareriAperte = new ArrayList<>();
			listaRichiestePareri
					.forEach(r -> listaGruppiDestinatariRichiestePareriAperte.add(r.getGruppoDestinatarioParere()));
			List<Municipio> municipi = List.of(pratica.getMunicipio());

			listaUtenti.addAll(utenteRepository
					.findUtentiAttiviByGruppiAndMunicipi(listaGruppiDestinatariRichiestePareriAperte, municipi));
		}

		listaUtenti.stream().forEach(u -> toMunicipio.add(u.getEmail()));

		if (!toMunicipio.isEmpty()) {
			if (toMunicipio.size() > 1) {
				toMunicipio.remove(utente.getEmail());
				comunicazioneMailInsertMunicipio.setDestinatariCc(utente.getEmail());
			}
			comunicazioneMailInsertMunicipio.setDestinatari(String.join(",", toMunicipio));
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertMunicipio);
		}
	}

	/**
	 * Calcola la nuova data di scadenza della pratica, calcolando i giorni
	 * trascorsi fra l'inserimento della richiesta di integrazione e la relativa
	 * risposta. Infatti durante le richieste di integrazione e preavviso diniego il
	 * conteggio dei giorni per il termine procedurale viene interrotto
	 * 
	 * @param dataScadenzaPratica
	 * @param dataInserimentoRichiestaIntegrazione
	 * @return la nuova data di scadenza pratica
	 */
	private LocalDateTime getNewDataScadenzaPratica(LocalDateTime dataScadenzaPratica,
			LocalDateTime dataInserimentoRichiestaIntegrazione) {
		long daysBetween = ChronoUnit.DAYS.between(dataInserimentoRichiestaIntegrazione, LocalDateTime.now());
		return dataScadenzaPratica.plusDays(daysBetween);
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
