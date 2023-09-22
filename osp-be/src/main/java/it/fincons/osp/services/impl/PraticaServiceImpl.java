package it.fincons.osp.services.impl;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.*;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.AllegatoMapper;
import it.fincons.osp.mapper.DatiRichiestaMapper;
import it.fincons.osp.mapper.PraticaMapper;
import it.fincons.osp.mapper.RichiedenteMapper;
import it.fincons.osp.model.TipoOperazioneVerificaOccupazione;
import it.fincons.osp.model.*;
import it.fincons.osp.payload.protocollazione.response.ProtocolloResponse;
import it.fincons.osp.payload.request.*;
import it.fincons.osp.repository.*;
import it.fincons.osp.repository.specification.PraticaSpecification;
import it.fincons.osp.services.*;
import it.fincons.osp.utils.Constants;
import it.fincons.osp.utils.DocUtils;
import it.fincons.osp.utils.EmailUtils;
import it.fincons.osp.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.bcel.Const;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.order.AuditOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.stream.Collectors;

import static it.fincons.osp.model.PassaggioStato.BOZZA_TO_INSERITA;

@Service
@Slf4j
public class PraticaServiceImpl implements PraticaService {

	@Value("${osp.app.scadenzario.dateOccupazione.modifica.giorni}")
	private Integer numGiorniModificaDateOccupazione;

	@Value("${osp.app.scadenzario.termineProcedimentale.giorni}")
	private Integer numGiorniTermineProcedimentale;

	@Value("${osp.app.scadenzario.pagamento.giorni}")
	private Integer numGiorniScadenzaPagamento;

	@Value("${osp.app.proroga.edilizia.numeroMax}")
	private Integer numMaxProrogheEdilizia;

	@Value("${osp.app.proroga.edilizia.giorniMax}")
	private Integer giorniMaxProrogheEdilizia;

	@Value("${osp.app.proroga.altro.numeroMax}")
	private Integer numMaxProrogheAltro;

	@Value("${osp.app.proroga.altro.giorniTotMax}")
	private Integer giorniTotMaxProrogheAltro;

	@Value("${osp.app.proroga.altro.giorniMinAllaScadenza}")
	private Integer giorniMinAllaScadenza;

	@Value("${osp.app.protocollo.callNewEndpoint}")
	private boolean callNewEndpoint;

	@Autowired
	UtenteRepository utenteRepository;

	@Autowired
	MunicipioRepository municipioRepository;

	@Autowired
	PraticaRepository praticaRepository;

	@Autowired
	DatiRichiestaRepository datiRichiestaRepository;

	@Autowired
	RichiedenteService richiedenteService;

	@Autowired
	DatiRichiestaService datiRichiestaService;

	@Autowired
	StatoPraticaRepository statoPraticaRepository;

	@Autowired
	TipoProcessoRepository tipoProcessoRepository;

	@Autowired
	AllegatoService allegatoService;

	@Autowired
	ProtocollazioneService protocollazioneService;

	@Autowired
	ProtocolloService protocolloService;

	@Autowired
	GruppoRepository gruppoRepository;

	@Autowired
	PraticaSpecification praticaSpecification;

	@Autowired
	PraticaMapper praticaMapper;

	@Autowired
	AllegatoMapper allegatoMapper;

	@Autowired
	ComunicazioneMailService comunicazioneMailService;

	@Autowired
	RichiestaParereRepository richiestaParereRepository;

	@Autowired
	RichiestaIntegrazioneService richiestaIntegrazioneService;

	@PersistenceContext
	EntityManager entityManager;

	@Autowired
	NotificaScadenzarioRepository notificaScadenzarioRepository;

	@Autowired
	RichiedenteMapper richiedenteMapper;

	@Autowired
	DatiRichiestaMapper datiRichiestaMapper;

	@Autowired
	RichiestaParereService richiestaParereService;

	@Autowired
	TipoAllegatoRepository tipoAllegatoRepository;

	@Autowired
	MarcaBolloPraticaRepository marcaBolloPraticaRepository;

	@Autowired
	AllegatoRepository allegatoRepository;

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO insertPratica(PraticaInsertEditRequest praticaInsertRequest) {

		if (praticaInsertRequest.getIdUtente() == null) {
			throw new ValidationException("Errore: ID utente null");
		}

		Utente utente = utenteRepository.findById(praticaInsertRequest.getIdUtente())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema",
						praticaInsertRequest.getIdUtente()));

		Integer idMunicipio = praticaInsertRequest.getIdMunicipio();
		Municipio municipio = municipioRepository.findById(idMunicipio)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID municipio non presente nel sistema",
						idMunicipio));

		// controllo coerenza municipio
//		if (praticaInsertRequest.getDatiRichiesta().getIdMunicipio() != null
//				&& !praticaInsertRequest.getDatiRichiesta().getIdMunicipio().equals(idMunicipio)) {
//			throw new BusinessException(ErrorCode.E7,
//					"Errore: è consentito inserire pratiche solamente per il proprio municipio di appartenenza");
//		}

		TipoProcesso tipoProcesso = tipoProcessoRepository.findById(praticaInsertRequest.getIdTipoProcesso())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID tipo processo non presente nel sistema", praticaInsertRequest.getIdTipoProcesso()));

		Pratica praticaOriginaria = null;
		if (tipoProcesso.getId().equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)) {

			if (praticaInsertRequest.getIdPraticaOriginaria() == null) {
				throw new BusinessException(ErrorCode.E7,
						"Errore: per il processo di proroga, l'id della pratica originaria è obbligatorio");
			}

			praticaOriginaria = praticaRepository.findById(praticaInsertRequest.getIdPraticaOriginaria())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID pratica originaria non presente nel sistema",
							praticaInsertRequest.getIdPraticaOriginaria()));


			//controllo sulla durata della richiesta di proroga che deve essere inferiore alla durata della concessione
			//a cui fa riferimento (eccetto per cantieri edili e stradali)

			long ctrGiorniPraticaOriginaria = ChronoUnit.DAYS.between(praticaOriginaria.getDatiRichiesta().getDataInizioOccupazione(), praticaOriginaria.getDatiRichiesta().getDataScadenzaOccupazione());

			long ctrGiorniProroga = ChronoUnit.DAYS.between(praticaInsertRequest.getDatiRichiesta().getDataInizioOccupazione(), praticaInsertRequest.getDatiRichiesta().getDataScadenzaOccupazione());

			if (ctrGiorniProroga > ctrGiorniPraticaOriginaria && (praticaOriginaria.getDatiRichiesta().getAttivitaDaSvolgere() == null || !praticaOriginaria.getDatiRichiesta().getAttivitaDaSvolgere().getId().equals(Constants.ID_TIPO_ATTIVITA_EDILIZIA))) {
				throw new BusinessException(ErrorCode.E16,
						"Per questa pratica non è possibile effettuare richieste di proroga: la durata della richiesta di proroga non può essere superiore alla durata della concessone a cui fa riferimento");
			}

			this.checkDateProroga(
				praticaOriginaria.getDatiRichiesta().getDataScadenzaOccupazione(),
				praticaOriginaria.getDatiRichiesta().getOraScadenzaOccupazione(),
				praticaInsertRequest.getDatiRichiesta().getDataInizioOccupazione(),
				praticaInsertRequest.getDatiRichiesta().getOraInizioOccupazione()
			);
			this.prorogaVerificheApertura(praticaOriginaria.getId());
		}

		if (tipoProcesso.getId().equals(Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA)) {
			if (praticaInsertRequest.getIdPraticaOriginaria() == null) {
				throw new BusinessException(ErrorCode.E7,
						"Errore: per il processo di rinuncia, l'id della pratica originaria è obbligatorio");
			}

			praticaOriginaria = praticaRepository.findById(praticaInsertRequest.getIdPraticaOriginaria())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID pratica originaria non presente nel sistema",
							praticaInsertRequest.getIdPraticaOriginaria()));

			// controllo se e' possibile inserire una rinuncia
			this.rinunciaVerificheApertura(praticaOriginaria.getId());

			if (praticaInsertRequest.getMotivazioneRichiesta() == null) {
				throw new BusinessException(ErrorCode.E7,
						"Errore: per il processo di rinuncia, la motivazione richiesta è obbligatoria");
			}
		}

		Richiedente firmatario = richiedenteService.insertRichiedente(praticaInsertRequest.getFirmatario());

		Richiedente destinatario = null;
		if (praticaInsertRequest.getDestinatario() != null) {
			destinatario = richiedenteService.insertRichiedente(praticaInsertRequest.getDestinatario());
		}

		DatiRichiesta datiRichiesta = datiRichiestaService.insertDatiRichiesta(praticaInsertRequest.getDatiRichiesta(), tipoProcesso, praticaOriginaria);

		//Per tenere traccia dell’avvenuto pagamento della marca da bollo anche lato OSP,
		//il json della pratica sarà aggiornato con i campi relativi al pagamento e non sarà richiesto il documento di attestazione del pagamento della marca da bollo.

		MarcaBolloPratica marcaBolloPratica=null;

		if(praticaInsertRequest.getMarcaBolloPratica()!=null){
			marcaBolloPratica=new MarcaBolloPratica();
			//marcaBolloPratica.setPratica(pratica);
			marcaBolloPratica.setIuv(praticaInsertRequest.getMarcaBolloPratica().getIuv());
			marcaBolloPratica.setImprontaFile(praticaInsertRequest.getMarcaBolloPratica().getImprontaFile());
			marcaBolloPratica.setImportoPagato(praticaInsertRequest.getMarcaBolloPratica().getImportoPagato());
			marcaBolloPratica.setCausalePagamento(praticaInsertRequest.getMarcaBolloPratica().getCausalePagamento());
			marcaBolloPratica.setIdRichiesta(praticaInsertRequest.getMarcaBolloPratica().getIdRichiesta());
			marcaBolloPratica.setDataOperazione(praticaInsertRequest.getMarcaBolloPratica().getDataOperazione());

			marcaBolloPraticaRepository.save(marcaBolloPratica);

		}

		Pratica pratica = new Pratica();
		if (praticaOriginaria != null) {
			if (praticaOriginaria.getTipoProcesso().getId()
					.equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)) {
				pratica.setIdPraticaOriginaria(praticaOriginaria.getIdPraticaOriginaria());
				pratica.setIdProrogaPrecedente(praticaOriginaria.getId());
			} else {
				pratica.setIdPraticaOriginaria(praticaOriginaria.getId());
			}
		}
		pratica.setMotivazioneRichiesta(praticaInsertRequest.getMotivazioneRichiesta());
		pratica.setDatiRichiesta(datiRichiesta);
		pratica.setFirmatario(firmatario);
		pratica.setDestinatario(destinatario);
		pratica.setMunicipio(municipio);
		pratica.setUtenteCreazione(utente);
		pratica.setDataCreazione(LocalDateTime.now().withNano(0));
		pratica.setDataModifica(pratica.getDataCreazione());
		pratica.setTipoProcesso(tipoProcesso);
		pratica.setStatoPratica(statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_BOZZA)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica BOZZA non trovato")));

		// campi cittadino EGOV
		pratica.setNomeCittadinoEgov(praticaInsertRequest.getNomeCittadinoEgov());
		pratica.setCognomeCittadinoEgov(praticaInsertRequest.getCognomeCittadinoEgov());
		pratica.setCfCittadinoEgov(praticaInsertRequest.getCfCittadinoEgov());

		//flag che indica se la pratica è stata inserita lato EGOV
		pratica.setOriginEgov(praticaInsertRequest.isOriginEgov());

		pratica.setMarcaBolloPratica(marcaBolloPratica);

		pratica = praticaRepository.save(pratica);

		log.info("Inserimento pratica avvenuto con successo");

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO editPraticaEgov(PraticaEgovEditRequest praticaInsertRequest) {

		Pratica pratica = praticaRepository.findById(praticaInsertRequest.getPratica().getId())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						praticaInsertRequest.getPratica().getId()));

		if (StringUtils.isBlank(praticaInsertRequest.getPratica().getNomeCittadinoEgov())
				|| StringUtils.isBlank(praticaInsertRequest.getPratica().getCognomeCittadinoEgov())
				|| StringUtils.isBlank(praticaInsertRequest.getPratica().getCfCittadinoEgov())) {
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

		Utente utente = utentiEgov.get(0);

		if (!allegatoService.checkAllegatiObbligatoriAggiornamento(pratica, praticaInsertRequest)) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: non risultano presenti gli allegati obbligatori per l'aggiornamento della pratica");
		}

		StatoPratica statoPraticaProntaAlRilascio = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_PRONTO_AL_RILASCIO)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica \"Pronto al rilascio\" non trovato"));

		// aggiornamento pratica
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_PRONTO_AL_RILASCIO));
		pratica.setStatoPratica(statoPraticaProntaAlRilascio);
		pratica.setDataInserimento(LocalDateTime.now().withNano(0));
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));
		pratica.setDataScadenzaPratica(LocalDateTime.now().withNano(0).plusDays(numGiorniTermineProcedimentale));

		pratica.setNomeCittadinoEgov(praticaInsertRequest.getPratica().getNomeCittadinoEgov());
		pratica.setCognomeCittadinoEgov(praticaInsertRequest.getPratica().getCognomeCittadinoEgov());
		pratica.setCfCittadinoEgov(praticaInsertRequest.getPratica().getCfCittadinoEgov());

		praticaRepository.save(pratica);


		// NON viene più effettuata la chiamata verso il middleware.
		// le informazioni di protocollazione ci vengono fornite in input
		ProtocolloResponse protocolloResponse = new ProtocolloResponse();
		protocolloResponse.setNumeroProtocollo(praticaInsertRequest.getPratica().getNumeroProtocollo());
		protocolloResponse.setAnno(praticaInsertRequest.getPratica().getAnno());
		protocolloResponse.setDataProtocollo(praticaInsertRequest.getPratica().getDataProtocollo());

		// inserimento protocollo
		Protocollo protocollo = protocolloService.insertProtocollo(pratica, statoPraticaProntaAlRilascio, protocolloResponse, null);

		for (AllegatoDTO allegato : praticaInsertRequest.getAllegati()) {
			allegato.setCodiceProtocollo(protocollo.getCodiceProtocollo());
			AllegatoPraticaInsertRequest allegatoPraticaInsertRequest = new AllegatoPraticaInsertRequest();
			allegatoPraticaInsertRequest.setIdPratica(pratica.getId());
			allegatoPraticaInsertRequest.setAllegato(allegato);

			allegatoService.insertAllegatoPratica(allegatoPraticaInsertRequest);
		}
		// aggiungo protocollo all'oggetto pratica
		if (pratica.getProtocolli() == null) {
			pratica.setProtocolli(new ArrayList<>());
		}
		pratica.getProtocolli().add(protocollo);

		// inserisco il codice del protocollo anche sugli allegati
		// allegatoService.insertCodiceProtocolloAllegatiPratica(pratica, statoPraticaProntaAlRilascio, protocollo.getCodiceProtocollo());

		log.info("Passaggio pratica da EGOV in stato \"Pronto al rilascio\" avvenuto con successo");

		// invio mail di notifica
		this.sendEmailNotificaInserimentoPratica(utente, pratica, protocollo.getCodiceProtocollo());

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	public List<PraticaDTO> searchPraticheRettificabili(String codiceFiscalePartitaIva) {
		List<Integer> idStati=new ArrayList<>();
		idStati.add(Constants.ID_STATO_PRATICA_INSERITA);
		idStati.add(Constants.ID_STATO_PRATICA_VERIFICA_FORMALE);
		idStati.add(Constants.ID_STATO_PRATICA_RICHIESTA_PARERI);

		List<PraticaDTO> lista =
			praticaRepository.findPraticheByStatoPraticaAndCodiceFiscalePivaAndTipoProcesso(
				idStati,
				codiceFiscalePartitaIva,
				Constants.ID_TIPO_PROCESSO_CONCESSIONE_TEMPORANEA
			).stream().map(praticaMapper::entityToDto)
			.collect(Collectors.toList());

		if(lista!=null&&lista.size()>0){
			lista=lista.stream().filter(
					pratica ->
						ChronoUnit.DAYS.between(pratica.getDataInserimento(), LocalDateTime.now())<=10
			).collect(Collectors.toList());
		}

		return lista;
	}
	/**
	 * Restituisce tutte le pratiche in cui lo stato pratica è concessione valida e
	 * la cui la data scadenza occupazione non sia minore di 15 giorni (proroga) oppure 60 giorni (rinuncia) dalla data della richiesta.
	 * Inoltre non deve essere stato avviato un altro processo post concessione (rinuncia o proroga)
	 *
	 * @param codiceFiscalePartitaIva
	 * @param idTipoProcesso
	 * @return
	 */

	@Override
	public List<PraticaDTO> searchPraticheAvviabiliPostConcessioone(String codiceFiscalePartitaIva, Integer idTipoProcesso) {

		TipoProcesso tipoProcesso = tipoProcessoRepository.findById(idTipoProcesso)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID tipo processo non presente nel sistema", idTipoProcesso));

		if(idTipoProcesso!=Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA && idTipoProcesso!=Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA){
			throw new BusinessException(ErrorCode.E1, "Errore: ID tipo processo non valido", idTipoProcesso);
		}
		List<Integer> idStati=new ArrayList<>();
		idStati.add(Constants.ID_STATO_PRATICA_CONCESSIONE_VALIDA);

		List<PraticaDTO> lista= praticaRepository.findPraticheByStatoPraticaAndCodiceFiscalePiva(idStati, codiceFiscalePartitaIva).stream().map(praticaMapper::entityToDto)
				.collect(Collectors.toList());

		if(lista!=null&&lista.size()>0){
			TipoProcesso tipoProcessoProroga = tipoProcessoRepository
					.findById(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)
					.orElseThrow(() -> new RuntimeException("Errore: tipo processo proroga non presente nel sistema"));

			TipoProcesso tipoProcessoRinuncia = tipoProcessoRepository
					.findById(Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA)
					.orElseThrow(() -> new RuntimeException("Errore: tipo processo rinuncia non presente nel sistema"));

			if(idTipoProcesso==Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA){
				lista=lista.stream().filter(
					pratica -> {
						try {
							this.prorogaVerificheApertura(pratica.getId());
							return true;
						} catch (Exception e) {
							return false;
						}
						/*if (pratica.getDatiRichiesta().getOraScadenzaOccupazione() != null) {
							LocalDateTime dataScadenzaOccupazione = LocalDateTime.of(
									pratica.getDatiRichiesta().getDataScadenzaOccupazione(),
									pratica.getDatiRichiesta().getOraScadenzaOccupazione()
							);
							return Math.abs(ChronoUnit.DAYS.between(dataScadenzaOccupazione, LocalDateTime.now())) >= 15;
						} else {
							return Math.abs(ChronoUnit.DAYS.between(pratica.getDatiRichiesta().getDataScadenzaOccupazione(), LocalDateTime.now())) >= 15;
						}*/
					}
				).collect(Collectors.toList());

				/*lista=lista.stream().filter(
						pratica -> praticaRepository.findByIdPraticaOriginariaAndTipoProcessoOrderByIdDesc(pratica.getId(), tipoProcessoProroga)
								.isEmpty()
				).collect(Collectors.toList());*/

			}
			if(idTipoProcesso==Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA) {
				lista=lista.stream().filter(pratica -> {
					try {
						this.rinunciaVerificheApertura(pratica.getId());
						return true;
					} catch (Exception e) {
						return false;
					}
					/*if (pratica.getDatiRichiesta().getOraScadenzaOccupazione() != null) {
						LocalDateTime dataScadenzaOccupazione = LocalDateTime.of(
								pratica.getDatiRichiesta().getDataScadenzaOccupazione(),
								pratica.getDatiRichiesta().getOraScadenzaOccupazione()
						);
						return Math.abs(ChronoUnit.DAYS.between(dataScadenzaOccupazione, LocalDateTime.now())) >= 60;
					} else {
						return Math.abs(ChronoUnit.DAYS.between(pratica.getDatiRichiesta().getDataScadenzaOccupazione(), LocalDateTime.now())) >= 60;
					}*/
				}).collect(Collectors.toList());

				/*lista=lista.stream().filter(
						pratica -> pratica.getIdPraticaOriginaria() == null && praticaRepository.findByIdPraticaOriginariaAndTipoProcessoOrderByIdDesc(pratica.getId(), tipoProcessoRinuncia)
								.isEmpty()
				).collect(Collectors.toList());*/
			}
		}
		/*List<Long> idPrecedenti = new ArrayList<>();
		lista.stream().forEach(
			praticaDTO -> {
				if (
					!praticaDTO.getTipoProcesso().getId().equals(Constants.ID_TIPO_PROCESSO_CONCESSIONE_TEMPORANEA) &&
					!List.of(
						Constants.ID_STATO_PRATICA_RIGETTATA,
						Constants.ID_STATO_PRATICA_ARCHIVIATA,
						Constants.ID_STATO_PRATICA_CONCESSIONE_VALIDA,
						Constants.ID_STATO_PRATICA_TERMINATA,
						Constants.ID_STATO_PRATICA_ANNULLATA,
						Constants.ID_STATO_PRATICA_RINUNCIATA
					).contains(praticaDTO.getStatoPratica().getId())
				) {
					idPrecedenti.add(praticaDTO.getIdPraticaOriginaria());
					if (praticaDTO.getIdProrogaPrecedente() != null) {
						idPrecedenti.add(praticaDTO.getIdProrogaPrecedente());
					}
				}
			}
		);
		lista = lista.stream().filter(el -> !idPrecedenti.contains(el.getId())).collect(Collectors.toList());*/
		return lista;
	}

	@Override
	public Long getCountPratiche(String username, List<Integer> idsStatiPratica, List<Integer> idsMunicipi) {
		Utente utente = utenteRepository.findByUsernameAndFlagEliminatoFalse(username).orElseThrow(
				() -> new BusinessException(ErrorCode.E3, "Errore: username non presente nel sistema", username));

		Integer gruppoDestinatarioParere=null;

		// gestione stato richiesta pareri
		if (idsStatiPratica.contains(Constants.ID_STATO_PRATICA_RICHIESTA_PARERI)
				&& Utils.getIdGruppiDestinatariPareri().contains(utente.getGruppo().getId())) {

			gruppoDestinatarioParere=utente.getGruppo().getId();
		}

		return praticaRepository.getCountPratiche(idsStatiPratica, gruppoDestinatarioParere, idsMunicipi);
	}

	@Override
	@Transactional
	@LogEntryExit(showArgs = true)
	public void editFlagEsenzioneCup(String username, FlagEsenzioneCupEditRequest flagEsenzioneCupEditRequest) {
		Utente utente = utenteRepository.findByUsernameAndFlagEliminatoFalse(username).orElseThrow(
				() -> new BusinessException(ErrorCode.E3, "Errore: username non presente nel sistema", username));

		Pratica pratica = praticaRepository.findById(flagEsenzioneCupEditRequest.getIdPratica())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						flagEsenzioneCupEditRequest.getIdPratica()));

		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));
		pratica.setInfoPassaggioStato(PassaggioStato.VERIFICA_FORMALE_TO_VERIFICA_FORMALE.getDescStatoPratica());
		pratica.setFlagEsenzionePagamentoCUP(Boolean.valueOf(flagEsenzioneCupEditRequest.getFlagEsenzionePagamentoCUP()));
		pratica.setMotivazioneEsenzionePagamentoCup(flagEsenzioneCupEditRequest.getMotivazioneEsenzionePagamentoCup());

		praticaRepository.save(pratica);

		log.info("Modifica flag  esenzione cup avvenuta con successo");
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public void editPratica(PraticaInsertEditRequest praticaInsertRequest) {

		if (praticaInsertRequest.getId() == null) {
			throw new ValidationException("Errore: id pratica null");
		}

		if (praticaInsertRequest.getIdUtente() == null) {
			throw new ValidationException("Errore: ID utente null");
		}

		Pratica pratica = praticaRepository.findById(praticaInsertRequest.getId())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						praticaInsertRequest.getId()));

		if (!pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_BOZZA)) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: modifica della pratica consentita solo nello stato BOZZA");
		}

		Utente utente = utenteRepository.findById(praticaInsertRequest.getIdUtente())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema",
						praticaInsertRequest.getIdUtente()));

		Integer idMunicipio = praticaInsertRequest.getIdMunicipio();
		Municipio municipio = municipioRepository.findById(idMunicipio)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID municipio non presente nel sistema",
						idMunicipio));

		// controllo coerenza municipio
		if (praticaInsertRequest.getDatiRichiesta().getIdMunicipio() != null
				&& !praticaInsertRequest.getDatiRichiesta().getIdMunicipio().equals(idMunicipio)) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: è consentito inserire pratiche solamente per il proprio municipio di appartenenza");
		}

		richiedenteService.editRichiedente(pratica.getFirmatario(), praticaInsertRequest.getFirmatario());

		if (praticaInsertRequest.getDestinatario() != null) {
			if (pratica.getDestinatario() == null) {
				pratica.setDestinatario(new Richiedente());
			}
			richiedenteService.editRichiedente(pratica.getDestinatario(), praticaInsertRequest.getDestinatario());
		}

		Pratica praticaOriginaria = null;

		if (pratica.getIdPraticaOriginaria() != null) {
			praticaOriginaria = praticaRepository.findById(pratica.getIdPraticaOriginaria())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID pratica originaria non presente nel sistema",
							pratica.getIdPraticaOriginaria()));
		}

		datiRichiestaService.editDatiRichiesta(pratica.getDatiRichiesta(), praticaInsertRequest.getDatiRichiesta(),
				pratica.getTipoProcesso(), praticaOriginaria);

		pratica.setMunicipio(municipio);
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));
		pratica.setMotivazioneRichiesta(praticaInsertRequest.getMotivazioneRichiesta());

		praticaRepository.save(pratica);

		log.info("Modifica pratica avvenuta con successo");
	}

	@Override
	@Transactional
	@LogEntryExit
	public void deletePratica(Long id) {
		if (id == null) {
			throw new ValidationException("Errore: id pratica null");
		}

		Pratica pratica = praticaRepository.findById(id).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", id));

		if (!pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_BOZZA)) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: modifica della pratica consentita solo nello stato BOZZA");
		}
		List<Allegato> allegatiPratica = this.allegatoRepository.findAllByPratica_Id(id);
		for(Allegato all: allegatiPratica) {
			this.allegatoRepository.delete(all);
		}
		praticaRepository.delete(pratica);

		log.info("Eliminazione pratica avvenuta con successo");
	}

	@Override
	@LogEntryExit(showArgs = true)
	public Page<PraticaDTO> getPratiche(String username, List<Integer> idsMunicipi, List<Integer> idsStatiPratica,
										FiltriRicercaPraticaDTO filtriRicerca, Boolean richiestaVerificaRipristinoLuoghi, Pageable pageable) {
		Specification<Pratica> conditions = Specification.where(praticaSpecification.inizialize());
		if (idsMunicipi != null) {
			// recupero lista oggetti Municipio
			List<Municipio> listaMunicipi = new ArrayList<>();

			for (Integer id : idsMunicipi) {
				listaMunicipi.add(municipioRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID municipio non presente nel sistema", id)));
			}

			conditions = praticaSpecification
					.addSpecificationToConditionListAnd(praticaSpecification.inMunicipio(listaMunicipi), conditions);
		}

		if (filtriRicerca.getTipoProcesso()!=null) {
			List<TipoProcesso> listaTipoProcesso = new ArrayList<>();

			listaTipoProcesso
					.add(tipoProcessoRepository.findById(filtriRicerca.getTipoProcesso()).orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID tipo processo non presente nel sistema", filtriRicerca.getTipoProcesso())));

			conditions = praticaSpecification.addSpecificationToConditionListAnd(
					praticaSpecification.inTipoProcesso(listaTipoProcesso), conditions);
		}

		if (idsStatiPratica != null) {
			// recupero lista oggetti Stato Pratica
			List<StatoPratica> listaStatiPratica = new ArrayList<>();

			for (Integer id : idsStatiPratica) {
				listaStatiPratica
						.add(statoPraticaRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.E1,
								"Errore: ID stato pratica non presente nel sistema", id)));
			}

			conditions = praticaSpecification.addSpecificationToConditionListAnd(
					praticaSpecification.inStatoPratica(listaStatiPratica), conditions);

			Utente utente = utenteRepository.findByUsernameAndFlagEliminatoFalse(username).orElseThrow(
					() -> new BusinessException(ErrorCode.E3, "Errore: username non presente nel sistema", username));

			// gestione stato richiesta pareri
			if (listaStatiPratica.size() == 1
					&& listaStatiPratica.get(0).getId().equals(Constants.ID_STATO_PRATICA_RICHIESTA_PARERI)
					&& Utils.getIdGruppiDestinatariPareri().contains(utente.getGruppo().getId())) {

				List<RichiestaParere> richiestePareri = richiestaParereRepository
						.findByGruppoDestinatarioParereAndFlagInseritaRispostaFalse(utente.getGruppo());

				if (!richiestePareri.isEmpty()) {
					conditions = praticaSpecification.addSpecificationToConditionListAnd(
							praticaSpecification.inRichiestaPareri(richiestePareri), conditions);
				} else {
					// condizione fittizia per non far ritornare alcun risultato
					conditions = conditions.and(praticaSpecification.alwaysFalse());
				}
			}

			// gestione stati verifica ripristino dei luoghi
			if (richiestaVerificaRipristinoLuoghi != null && richiestaVerificaRipristinoLuoghi
					&& Utils.getIdStatiPraticaVerificaRipristinoDeiLuoghi().equals(idsStatiPratica)) {
				List<RichiestaParere> richiesteVerificaRipristinoLuoghi = richiestaParereRepository
						.findByGruppoDestinatarioParereAndNotaRichiestaParereAndFlagInseritaRispostaFalse(
								utente.getGruppo(), Constants.NOTA_RICHIESTA_PARERE_VERIFICA_RIPRISTINO_LUOGHI);

				if (!richiesteVerificaRipristinoLuoghi.isEmpty()) {
					conditions = praticaSpecification.addSpecificationToConditionListAnd(
							praticaSpecification.inRichiestaPareri(richiesteVerificaRipristinoLuoghi), conditions);
				} else {
					// condizione fittizia per non far ritornare alcun risultato
					conditions = conditions.and(praticaSpecification.alwaysFalse());
				}
			}

		} else {
			// Se non è stato fornito il filtro stato, eliminiamo le pratiche in bozza
			conditions = praticaSpecification
					.addSpecificationToConditionListAnd(praticaSpecification.notEquals(0, "statoPratica"), conditions);
		}


		if (StringUtils.isNotBlank(filtriRicerca.getNumProtocollo())) {

			List<Protocollo> protocolli=protocolloService.getProtocolliByNumeroProtocollo(filtriRicerca.getNumProtocollo());
			List<Long> praticaIds = new ArrayList<>();
			if (protocolli != null) {
				praticaIds.addAll(protocolli.stream().map(
						el -> el.getPratica().getId()).collect(Collectors.toList())
				);
			}
			List<RichiestaIntegrazione> richiesteIntegrazione = richiestaIntegrazioneService.findAllByProtocollo(filtriRicerca.getNumProtocollo());
			if (richiesteIntegrazione != null) {
				praticaIds.addAll(richiesteIntegrazione.stream().map(
						el -> el.getPratica().getId()).collect(Collectors.toList())
				);
			}
			List<RichiestaParere> richiestaParere = richiestaParereService.findAllByProtocollo(filtriRicerca.getNumProtocollo());
			if (richiestaParere != null) {
				praticaIds.addAll(richiestaParere.stream().map(
						el -> el.getPratica().getId()).collect(Collectors.toList())
				);
			}
			if (!praticaIds.isEmpty()) {
				conditions = praticaSpecification.addSpecificationToConditionListAnd(
					praticaSpecification.praticaByIds(
						praticaIds
					),
					conditions
				);
			} else {
				// condizione fittizia per non far ritornare alcun risultato
				conditions = conditions.and(praticaSpecification.alwaysFalse());
			}
		}

		conditions = praticaSpecification.addSpecificationToConditionListAnd(
				praticaSpecification.contains(filtriRicerca.getNumProvvedimento(), "codiceDetermina"), conditions);

		// gestione filtri ricerca sui richiedenti
		if (!checkIfBlankAllSearchFiltersRichiedenti(filtriRicerca.getNome(), filtriRicerca.getCognome(),
				filtriRicerca.getDenominazioneRagSoc(), filtriRicerca.getCodFiscalePIva())) {

			List<Richiedente> listaRichiedenti = richiedenteService.searchDestinatari(filtriRicerca.getNome(),
					filtriRicerca.getCognome(), filtriRicerca.getDenominazioneRagSoc(),
					filtriRicerca.getCodFiscalePIva());

			if (!listaRichiedenti.isEmpty()) {
				Specification<Pratica> conditionsDestinatarioOr = Specification
						.where(praticaSpecification.inFirmatario(listaRichiedenti).orElseThrow(RuntimeException::new));

				conditionsDestinatarioOr = praticaSpecification.addSpecificationToConditionListOr(
						praticaSpecification.inDestinatario(listaRichiedenti), conditionsDestinatarioOr);

				conditions = conditions.and(conditionsDestinatarioOr);
			} else {
				// se non sono stati trovati richiedenti corrispondenti ai filtri allora imposto
				// una condizione fittizzia per non far ritornare alcun risultato
				conditions = conditions.and(praticaSpecification.alwaysFalse());
			}
		}

		List<Pratica> listaPratiche = praticaRepository.findAll(conditions);

		/*
		TODO non funziona quindi adotto workaround filtrando risultati tramite filter
		if (StringUtils.isNotBlank(filtriRicerca.getIndirizzo())) {

			String indirizzo=filtriRicerca.getIndirizzo().replaceAll(" ","%");

			List<DatiRichiesta> listaDatiRichiesta=datiRichiestaRepository.findByUbicazioneOccupazione(filtriRicerca.getIndirizzo());

			conditions = praticaSpecification.addSpecificationToConditionListAnd(
					praticaSpecification.inDatiRichiesta(listaDatiRichiesta), conditions);
		}
		*/

		if (StringUtils.isNotBlank(filtriRicerca.getIndirizzo())&&listaPratiche.size()>0) {
			listaPratiche = listaPratiche.stream().filter(pratica -> pratica.getDatiRichiesta().getUbicazioneOccupazione().equals(filtriRicerca.getIndirizzo())).collect(Collectors.toList());
		}

		// work around per non utilizzare l'oggetto pageable per la paginazione
		int start = 0;
		int end = listaPratiche.size();

		return new PageImpl<Pratica>(listaPratiche.subList(start, end), pageable, listaPratiche.size())
				.map(praticaMapper::entityToDto);

	}

	@Override
	@LogEntryExit(showArgs = true)
	public PraticaDTO getPratica(Long id) {
		Pratica pratica = praticaRepository.findById(id).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", id));

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	@Transactional
	@LogEntryExit(showArgs = true)
	public void editDateOccupazione(String usernameUtente, DateOccupazioneEditRequest dateOccupazioneEditRequest) {

		Utente utente = utenteRepository.findByUsernameAndFlagEliminatoFalse(usernameUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E3, "Errore: username non presente nel sistema", usernameUtente));

		Pratica pratica = praticaRepository.findById(dateOccupazioneEditRequest.getIdPratica())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						dateOccupazioneEditRequest.getIdPratica()));

		Set<Integer> idStatiConsentiti = Set.of(Constants.ID_STATO_PRATICA_INSERITA,
				Constants.ID_STATO_PRATICA_VERIFICA_FORMALE, Constants.ID_STATO_PRATICA_RICHIESTA_PARERI);

		if (!idStatiConsentiti.contains(pratica.getStatoPratica().getId())) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: la modifica delle date di occupazione non è consentita in questo stato della pratica",
					"ID stato pratica attuale: " + pratica.getStatoPratica().getId());
		}

		// controllo che non siano trascorsi i giorni consentiti per la modifica delle
		// date di occupazione
		if (pratica.getDataInserimento().plusDays(numGiorniModificaDateOccupazione)
				.isBefore(LocalDateTime.now().withNano(0))) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: la modifica delle date di occupazione non è consentita dopo "
							+ numGiorniModificaDateOccupazione + " giorni dall'inserimento della pratica");
		}

		datiRichiestaService.editDateOccupazione(pratica, dateOccupazioneEditRequest);

		// se la pratica è nello stato "Richiesta pareri", allora la riporto allo stato
		// "Verifica formale"
		if (pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_RICHIESTA_PARERI)) {

			StatoPratica statoPraticaVerificaFormale = statoPraticaRepository.getById(Constants.ID_STATO_PRATICA_VERIFICA_FORMALE);

			pratica.setStatoPratica(statoPraticaVerificaFormale);

			pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_VERIFICA_FORMALE));
		} else {
			pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), pratica.getStatoPratica().getId()));
		}

		pratica.setNomeCittadinoEgov(dateOccupazioneEditRequest.getNomeCittadinoEgov());
		pratica.setCognomeCittadinoEgov(dateOccupazioneEditRequest.getCognomeCittadinoEgov());
		pratica.setCfCittadinoEgov(dateOccupazioneEditRequest.getCfCittadinoEgov());

		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));
		pratica.setOriginEgov(dateOccupazioneEditRequest.isOriginEgov());


		ProtocolloResponse protocolloResponse = new ProtocolloResponse();
		protocolloResponse.setNumeroProtocollo(dateOccupazioneEditRequest.getNumeroProtocollo());
		protocolloResponse.setAnno(dateOccupazioneEditRequest.getAnnoProtocollo());
		protocolloResponse.setDataProtocollo(dateOccupazioneEditRequest.getDataProtocollo());
		// inserimento protocollo
		Protocollo protocollo = protocolloService.insertProtocollo(
			pratica,
			(pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_INSERITA)) ?
				statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_INSERITA_MODIFICA_DATE).orElseThrow(( )-> new BusinessException(ErrorCode.E1,
					"Errore: ID stato pratica non presente nel sistema", Constants.ID_STATO_PRATICA_INSERITA_MODIFICA_DATE)) :
				pratica.getStatoPratica(),
			protocolloResponse,
			null
		);
		// aggiungo protocollo all'oggetto pratica
		if (pratica.getProtocolli() == null) {
			pratica.setProtocolli(new ArrayList<>());
		}
		pratica.getProtocolli().add(protocollo);

		praticaRepository.save(pratica);

		// invio mail di notifica
		this.sendEmailCambioDateOccupazione(utente, pratica);

		log.info("Modifica date occupazione avvenuta con successo");
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO switchToStatoInserita(Long idPratica, Long idUtente, String numeroProtocollo, String anno, LocalDateTime dataProtocollo) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		
		if (!allegatoService.checkAllegatiObbligatoriInserimento(pratica, utente.getGruppo())) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: non risultano presenti gli allegati obbligatori per l'inserimento della pratica");
		}

		StatoPratica statoPraticaPreOperazione = pratica.getStatoPratica();

		//recupero allegati pratica
		List<Allegato> allegati=allegatoRepository.findByPraticaAndStatoPratica(pratica.getId(), statoPraticaPreOperazione.getId());

		StatoPratica statoPraticaInserita = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_INSERITA)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica INSERITA non trovato"));

		// aggiornamento pratica
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_INSERITA));
		pratica.setStatoPratica(statoPraticaInserita);
		pratica.setDataInserimento(LocalDateTime.now().withNano(0));
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));
		pratica.setDataScadenzaPratica(LocalDateTime.now().withNano(0).plusDays(numGiorniTermineProcedimentale));

		praticaRepository.save(pratica);

		// per EGOV non viene più effettuata la chiamata verso il middleware.
		// le informazioni di protocollazione ci vengono fornite in input

		ProtocolloResponse protocolloResponse =null;

		if(StringUtils.isBlank(numeroProtocollo)){
			// richiesta numero protocollo

			if(callNewEndpoint){
				protocolloResponse = protocollazioneService.getNumeroProtocolloEntrata(
						pratica,
						utente,
						Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_CITTADINO,
						Constants.TEMPLATE_NAME_INSERIMENTO_PRATICA,
						allegati,
						PassaggioStato.BOZZA_TO_INSERITA);

			}else{
				protocolloResponse = protocollazioneService.getNumeroProtocolloEntrata(pratica, utente, Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_CITTADINO);
			}

		}else{
			protocolloResponse = new ProtocolloResponse();

			protocolloResponse.setNumeroProtocollo(numeroProtocollo);
			protocolloResponse.setAnno(anno);
			protocolloResponse.setDataProtocollo(dataProtocollo);

		}

		// inserimento protocollo
		Protocollo protocollo = protocolloService.insertProtocollo(pratica, statoPraticaInserita, protocolloResponse, null);

		// aggiungo protocollo all'oggetto pratica
		if (pratica.getProtocolli() == null) {
			pratica.setProtocolli(new ArrayList<>());
		}
		pratica.getProtocolli().add(protocollo);

		// inserisco il codice del protocollo anche sugli allegati
		allegatoService.insertCodiceProtocolloAllegatiPratica(pratica, statoPraticaPreOperazione, protocollo.getCodiceProtocollo());

		log.info("Passaggio pratica in stato INSERITA avvenuto con successo");

		// invio mail di notifica
		this.sendEmailNotificaInserimentoPratica(utente, pratica, protocollo.getCodiceProtocollo());

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO presaInCarico(Long idPratica, Long idUtenteIstruttore, Long idUtenteDirettore) {

		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Utente utenteIstruttore = utenteRepository.findById(idUtenteIstruttore)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID utente istruttore non presente nel sistema", idUtenteIstruttore));

		if (!utenteIstruttore.getGruppo().getId().equals(Constants.ID_GRUPPO_ISTRUTTORE_MUNICIPIO)) {
			throw new BusinessException(ErrorCode.E11, "Errore: l'utente non è un Istruttore Municipio");
		}

		Utente utenteDirettore = null;
		if (idUtenteDirettore != null) {
			utenteDirettore = utenteRepository.findById(idUtenteDirettore)
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID utente direttore non presente nel sistema", idUtenteDirettore));

			if (!utenteDirettore.getGruppo().getId().equals(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)) {
				throw new BusinessException(ErrorCode.E11, "Errore: l'utente non è un Direttore Municipio");
			}
		}

		StatoPratica statoPraticaVerificaFormale = statoPraticaRepository
				.findById(Constants.ID_STATO_PRATICA_VERIFICA_FORMALE)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica VERIFICA FORMALE non trovato"));

		// aggiornamento pratica
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_VERIFICA_FORMALE));
		pratica.setStatoPratica(statoPraticaVerificaFormale);
		pratica.setUtentePresaInCarico(utenteIstruttore);

		if (utenteDirettore != null) {
			pratica.setUtenteModifica(utenteDirettore);
			pratica.setUtenteAssegnatario(utenteDirettore);
		} else {
			pratica.setUtenteModifica(utenteIstruttore);
		}

		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		praticaRepository.save(pratica);

		// recupero destinatari email e protocollazione
		List<Utente> listaDestinatari = this.getDestinatariPresaInCarico(pratica);

		/*
		protocollazione non più necessaria, commento come da richiesta

		// richiesta numero protocollo
		ProtocolloResponse protocolloResponse = null;

		if(callNewEndpoint){
			//recupero allegati pratica
			List<Allegato> allegati=allegatoRepository.findByPraticaAndStatoPratica(pratica.getId(), pratica.getStatoPratica().getId());

			protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(
					pratica,
					utenteDirettore != null ? utenteDirettore : utenteIstruttore,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_PRESA_IN_CARICO,
					listaDestinatari,
					true,
					Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA, allegati);

		}else{
			protocolloResponse =  protocollazioneService.getNumeroProtocolloUscita(pratica,
					utenteDirettore != null ? utenteDirettore : utenteIstruttore,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_PRESA_IN_CARICO, listaDestinatari, true);
		}

		// inserimento protocollo
		Protocollo protocolloPresaInCarico = protocolloService.insertProtocollo(pratica, statoPraticaVerificaFormale,
				protocolloResponse, null);
*/
		log.info("Presa in carico pratica avvenuta con successo");

		// invio mail di notifica
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);

		this.sendEmailNotificaPresaInCaricoPratica(listaDestinatari,
				utenteDirettore != null ? utenteDirettore : utenteIstruttore, pratica,
				protocolloInserimento.getCodiceProtocollo(),
				null
				//protocolloPresaInCarico.getCodiceProtocollo()

		);

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO approvazione(Long idPratica, Long idUtente) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		StatoPratica statoPraticaApprovata = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_APPROVATA)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica APPROVATA non trovato"));

		// controllo coordinate ubicazione definitive
		if (pratica.getDatiRichiesta().getCoordUbicazioneDefinitiva() == null) {
			datiRichiestaService.verificaOccupazione(pratica.getDatiRichiesta(),
					TipoOperazioneVerificaOccupazione.OCCUPAZIONE_CORRETTA, null);
		}

		// aggiornamento pratica
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_APPROVATA));
		pratica.setStatoPratica(statoPraticaApprovata);
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		praticaRepository.save(pratica);

		log.info("Passaggio pratica in stato APPROVATA avvenuto con successo");

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO rigettoRichiesta(RichiestaIntegrazioneDTO richiestaIntegrazioneRequest) {
		Pratica pratica = praticaRepository.findById(richiestaIntegrazioneRequest.getIdPratica())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						richiestaIntegrazioneRequest.getIdPratica()));

		Utente utente = utenteRepository.findById(richiestaIntegrazioneRequest.getIdUtenteRichiedente())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema",
						richiestaIntegrazioneRequest.getIdUtenteRichiedente()));

		Protocollo protocollo = richiestaIntegrazioneService.gestionePreavvisoDiniego(pratica, utente, richiestaIntegrazioneRequest);

		PraticaDTO result = praticaMapper.entityToDto(this.praticaRepository.findById(richiestaIntegrazioneRequest.getIdPratica())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						richiestaIntegrazioneRequest.getIdPratica())));
		if (result != null && result.getProtocolli() != null) {
			result.getProtocolli().add(praticaMapper.protocolloToDto(protocollo));
		}
		return result;
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO inserimentoDetermina(Long idPratica, Long idUtente, String codiceDetermina,
										   LocalDate dataEmissioneDetermina) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		boolean isRinuncia = pratica.getTipoProcesso().getId()
				.equals(Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA);

		StatoPratica statoPraticaPreOperazione = pratica.getStatoPratica();
		StatoPratica statoPraticaPostDetermina;

		if (isRinuncia) {
			statoPraticaPostDetermina = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_RINUNCIATA)
					.orElseThrow(() -> new RuntimeException("Errore: stato pratica RINUNCIATA non trovato"));

			// aggiornamento pratica
			pratica.setCodiceDeterminaRinuncia(codiceDetermina);
			pratica.setDataEmissioneDeterminaRinuncia(dataEmissioneDetermina);
			pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_RINUNCIATA));
			pratica.setStatoPratica(statoPraticaPostDetermina);
		} else {
			statoPraticaPostDetermina = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_ATTESA_DI_PAGAMENTO)
					.orElseThrow(() -> new RuntimeException("Errore: stato pratica ATTESA DI PAGAMENTO non trovato"));

			// aggiornamento pratica
			pratica.setCodiceDetermina(codiceDetermina);
			pratica.setDataEmissioneDetermina(dataEmissioneDetermina);
			pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_ATTESA_DI_PAGAMENTO));
			pratica.setStatoPratica(statoPraticaPostDetermina);
			pratica.setDataScadenzaPagamento(LocalDateTime.now().withNano(0).plusDays(numGiorniScadenzaPagamento));
		}

		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		praticaRepository.save(pratica);

		// recupero destinatari email e protocollazione
		List<Utente> listaDestinatari = this.getDestinatariDetermina(pratica);

		// richiesta numero protocollo
		ProtocolloResponse protocolloResponse = null;

		if(callNewEndpoint){
			Integer idTipoAllegato=0;

			if(isRinuncia){
				idTipoAllegato=Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RINUNCIA;
			}else {
				if(pratica.getTipoProcesso().getId().equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)){
					idTipoAllegato=Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_PROROGA;
				}else{
					idTipoAllegato=Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_CONCESSIONE;
				}
			}

			DeterminaDTO determina=getDetermina(pratica.getId(), idTipoAllegato);

			protocolloResponse = protocollazioneService.getNumeroProtocolloUscitaV2(
				pratica,
				utente,
				isRinuncia ? Constants.TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_RINUNCIA:Constants.TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA,
				listaDestinatari,
				true,
				determina.getContenuto().getBytes(),//determina
				Utils.getFileExtension(determina.getNomeFile()),
				null
			);//allegati null in questo caso

		}else{
			protocolloResponse =  protocollazioneService.getNumeroProtocolloUscita(pratica,
					utente,
					isRinuncia ? Constants.TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_RINUNCIA:Constants.TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA,
					listaDestinatari, true);
		}

		// inserimento protocollo
		Protocollo protocolloDetermina = protocolloService.insertProtocollo(pratica, statoPraticaPostDetermina,
				protocolloResponse, null);

		allegatoService.insertCodiceProtocolloAllegatiPratica(pratica, statoPraticaPreOperazione,
				protocolloDetermina.getCodiceProtocollo());

		if (isRinuncia) {
			log.info("Passaggio pratica in stato RINUNCIATA avvenuto con successo");
		} else {
			log.info("Passaggio pratica in stato ATTESA DI PAGAMENTO avvenuto con successo");
		}

		// invio mail di notifica
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);

		if (isRinuncia) {
			this.sendEmailNotificaDeterminaRinunciaPratica(listaDestinatari, utente, pratica,
					protocolloInserimento.getCodiceProtocollo(), protocolloDetermina.getCodiceProtocollo(), dataEmissioneDetermina);

			// inserisco anche richiesta parere per la richiesta di verifica del ripristino
			// dei luoghi
			richiestaParereService.insertRichiestaVerificaRipristinoLuoghi(pratica, utente);

			allegatoService.insertCodiceProtocolloAllegatiPraticaByIdTipo(
				pratica,
				Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RINUNCIA,
				protocolloDetermina.getCodiceProtocollo()
			);

			// passaggio di stato della pratica originaria (oppure, se c'è, della proroga da cui è partita) in "Archiviata"
			Pratica praticaOriginaria = praticaRepository.findById(
					pratica.getIdProrogaPrecedente() != null ? pratica.getIdProrogaPrecedente() : pratica.getIdPraticaOriginaria())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID pratica originaria non presente nel sistema",
							pratica.getIdPraticaOriginaria()));

			StatoPratica statoPraticaArchiviata = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_ARCHIVIATA)
					.orElseThrow(() -> new RuntimeException("Errore: stato pratica ARCHIVIATA non trovato"));

			praticaOriginaria.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(praticaOriginaria.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_ARCHIVIATA));
			praticaOriginaria.setStatoPratica(statoPraticaArchiviata);
			praticaOriginaria.setUtenteModifica(utente);
			praticaOriginaria.setDataModifica(LocalDateTime.now().withNano(0));
			praticaRepository.save(praticaOriginaria);

		} else {
			this.sendEmailNotificaDeterminaPratica(listaDestinatari, utente, pratica,
					protocolloInserimento.getCodiceProtocollo(), protocolloDetermina.getCodiceProtocollo(), dataEmissioneDetermina);
		}

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO switchToStatoProntoAlRilascio(Long idPratica, Long idUtente) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		if (!pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_ATTESA_DI_PAGAMENTO)) {
			throw new BusinessException(ErrorCode.E13,
					"Errore: lo stato della pratica non è coerente per il passaggio di stato in 'Pronto al rilascio'",
					pratica.getStatoPratica().getId());
		}

		if (!allegatoService.checkAllegatiObbligatori(pratica, pratica.getStatoPratica(), pratica.getTipoProcesso(),
				utente.getGruppo())) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: non risultano presenti gli allegati obbligatori per il passaggio di stato in 'Pronto al rilascio'");
		}

		//recupero allegati pratica
		List<Allegato> allegati=allegatoRepository.findByPraticaAndStatoPratica(pratica.getId(), pratica.getStatoPratica().getId());

		StatoPratica statoPraticaProntoAlRilascio = statoPraticaRepository
				.findById(Constants.ID_STATO_PRATICA_PRONTO_AL_RILASCIO)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica PRONTO AL RILASCIO non trovato"));

		// aggiornamento pratica
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_PRONTO_AL_RILASCIO));
		pratica.setStatoPratica(statoPraticaProntoAlRilascio);
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		// richiesta numero protocollo
		ProtocolloResponse protocolloResponse = null;

		if(callNewEndpoint){
			if(allegati!=null&&allegati.size()>0 ){
				allegati=allegati.stream().filter(allegato -> allegato.getCodiceProtocollo()==null || "".equals(allegato.getCodiceProtocollo())).collect(Collectors.toList());
			}

			protocolloResponse = protocollazioneService.getNumeroProtocolloEntrata(
					pratica,
					utente,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_CITTADINO,
					Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA,
					allegati,
					PassaggioStato.ATTESA_DI_PAGAMENTO_TO_PRONTO_AL_RILASCIO);

			for (int i = 0; i < allegati.size(); i++) {
				allegati.get(i).setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());
				allegatoRepository.save(allegati.get(i));
			}
		}else{
			protocolloResponse = protocollazioneService.getNumeroProtocolloEntrata(pratica, utente, Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_CITTADINO);
		}

		// inserimento protocollo
		Protocollo protocollo = protocolloService.insertProtocollo(pratica, statoPraticaProntoAlRilascio, protocolloResponse, null);

		// aggiungo protocollo all'oggetto pratica
		if (pratica.getProtocolli() == null) {
			pratica.setProtocolli(new ArrayList<>());
		}
		pratica.getProtocolli().add(protocollo);

		log.info("Passaggio pratica in stato PRONTO AL RILASCIO avvenuto con successo");

		// invio mail di notifica
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);

		List<Utente> listaDestinatari = this.getDestinatariProntoRilascio(pratica);
		this.sendEmailNotificaProntoRilascio(
			listaDestinatari,
			utente,
			pratica,
			protocolloInserimento.getCodiceProtocollo(),
			protocollo.getCodiceProtocollo()
		);
		this.praticaRepository.save(pratica);

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO switchToStatoConcessioneValida(Long idPratica, Long idUtente) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		if (!pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_PRONTO_AL_RILASCIO)) {
			throw new BusinessException(ErrorCode.E13,
					"Errore: lo stato della pratica non è coerente per il passaggio di stato in 'Concessione valida'",
					pratica.getStatoPratica().getId());
		}

		StatoPratica statoPraticaPreOperazione = pratica.getStatoPratica();

		StatoPratica statoPraticaConcessioneValida = statoPraticaRepository
				.findById(Constants.ID_STATO_PRATICA_CONCESSIONE_VALIDA)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica CONCESSIONE VALIDA non trovato"));

		// aggiornamento pratica
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_CONCESSIONE_VALIDA));
		pratica.setStatoPratica(statoPraticaConcessioneValida);
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		praticaRepository.save(pratica);

		// recupero destinatari email e protocollazione
		List<Utente> listaDestinatari = this.getDestinatariRitiroAttoConcessorio(pratica);

		log.info("Passaggio pratica in stato CONCESSIONE VALIDA avvenuto con successo");

		// invio mail di notifica
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);

		this.sendEmailNotificaRitiroAttoConcessorio(
				listaDestinatari,
				utente,
				pratica,
				protocolloInserimento.getCodiceProtocollo(),
				//protocolloConcessioneValida.getCodiceProtocollo()
				null
		);

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO inserimentoDeterminaRigetto(Long idPratica, Long idUtente, String codiceDeterminaRigetto,
												  LocalDate dataEmissioneDetermina) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		if (!pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_DA_RIGETTARE)) {
			throw new BusinessException(ErrorCode.E13,
					"Errore: lo stato della pratica non è coerente per il passaggio di stato in 'Pratica rigettata'",
					pratica.getStatoPratica().getId());
		}

		if (!allegatoService.checkAllegatiObbligatori(pratica, pratica.getStatoPratica(), pratica.getTipoProcesso(),
				utente.getGruppo())) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: non risultano presenti gli allegati obbligatori per il passaggio di stato in 'Pratica rigettata'");
		}

		StatoPratica statoPraticaPreOperazione = pratica.getStatoPratica();
		//recupero allegati pratica
		List<Allegato> allegati=allegatoRepository.findByPraticaAndStatoPratica(pratica.getId(), pratica.getStatoPratica().getId());

		StatoPratica statoPraticaRigettata = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_RIGETTATA)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica RIGETTATA non trovato"));

		// aggiornamento pratica
		pratica.setCodiceDetermina(codiceDeterminaRigetto);
		pratica.setDataEmissioneDetermina(dataEmissioneDetermina);
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_RIGETTATA));
		pratica.setStatoPratica(statoPraticaRigettata);
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		praticaRepository.save(pratica);

		// recupero destinatari email e protocollazione
		List<Utente> listaDestinatari = this.getDestinatariDeterminaRigetto(pratica);

		ProtocolloResponse protocolloResponse = null;

		if(callNewEndpoint){
			DeterminaDTO determina=getDetermina(pratica.getId(), Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RIGETTO);

			protocolloResponse = protocollazioneService.getNumeroProtocolloUscitaV2(
					pratica,
					utente,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_RIGETTO,
					listaDestinatari,
					true,
					determina.getContenuto().getBytes(),//determina
					Utils.getFileExtension(determina.getNomeFile()),
					null);//allegati null in questo caso

		}else{
			protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(pratica, utente,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_RIGETTO, listaDestinatari, true);
		}

		// inserimento protocollo
		Protocollo protocolloDeterminaRigetto = protocolloService.insertProtocollo(pratica, statoPraticaRigettata,
				protocolloResponse, null);

		allegatoService.insertCodiceProtocolloAllegatiPratica(pratica, statoPraticaPreOperazione,
				protocolloDeterminaRigetto.getCodiceProtocollo());

		log.info("Passaggio pratica in stato RIGETTATA avvenuto con successo");

		// invio mail di notifica
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);
		this.sendEmailNotificaRigettoPratica(listaDestinatari, utente, pratica,
				protocolloInserimento.getCodiceProtocollo(), protocolloDeterminaRigetto.getCodiceProtocollo());

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	@SuppressWarnings("unchecked")
	@LogEntryExit(showArgs = true)
	public Page<PraticaDTO> getStoricoPratica(Long idPratica, Pageable pageable) {

		// recupero le revisioni della pratica
		AuditReader reader = AuditReaderFactory.get(entityManager);
		AuditOrder order = AuditEntity.revisionNumber().asc();

		List<Pratica> listaPratiche = reader.createQuery().forRevisionsOfEntity(Pratica.class, true, false)
				.add(AuditEntity.property("id").eq(idPratica)).addOrder(order).getResultList();

		// mappe di appoggio per gestione richieste pareri e integrazioni
		Map<SimpleEntry<Long, Long>, RichiestaParere> mapRichiestePareri = new HashMap<>();
		Map<SimpleEntry<Long, Long>, RichiestaIntegrazione> mapRichiesteIntegrazioni = new HashMap<>();

		Iterator<Pratica> iteratorPratiche = listaPratiche.iterator();
		while (iteratorPratiche.hasNext()) {
			Pratica currentPratica = iteratorPratiche.next();

			// se l'utente modifica non e' un utente egov non si devono vedere le info del
			// cittadino
			if (currentPratica.getUtenteModifica() != null
					&& !currentPratica.getUtenteModifica().getGruppo().getId().equals(Constants.ID_GRUPPO_EGOV)) {
				currentPratica.setNomeCittadinoEgov(null);
				currentPratica.setCognomeCittadinoEgov(null);
				currentPratica.setCfCittadinoEgov(null);
			}

			// gestione richieste pareri, per mostrare solo le richieste effettivamente
			// modificate in una determinata revisione
			if (!currentPratica.getRichiestePareri().isEmpty()) {

				Iterator<RichiestaParere> iteratorRichiestePareri = currentPratica.getRichiestePareri().iterator();

				while (iteratorRichiestePareri.hasNext()) {
					RichiestaParere currentRichiestaParere = iteratorRichiestePareri.next();

					SimpleEntry<Long, Long> keyParere = new SimpleEntry<>(currentRichiestaParere.getId(),
							currentRichiestaParere.getParere() != null ? currentRichiestaParere.getParere().getId()
									: null);
					if (mapRichiestePareri.containsKey(keyParere)) {
						iteratorRichiestePareri.remove();
					} else {
						mapRichiestePareri.put(keyParere, currentRichiestaParere);
					}
				}
			}

			// gestione richieste integrazioni, per mostrare solo le richieste
			// effettivamente modificate in una determinata revisione
			if (!currentPratica.getRichiesteIntegrazioni().isEmpty()) {

				Iterator<RichiestaIntegrazione> iteratorRichiesteIntegrazioni = currentPratica
						.getRichiesteIntegrazioni().iterator();

				while (iteratorRichiesteIntegrazioni.hasNext()) {
					RichiestaIntegrazione currentRichiestaIntegrazione = iteratorRichiesteIntegrazioni.next();

					SimpleEntry<Long, Long> keyIntegrazione = new SimpleEntry<>(currentRichiestaIntegrazione.getId(),
							currentRichiestaIntegrazione.getIntegrazione() != null
									? currentRichiestaIntegrazione.getIntegrazione().getId()
									: null);
					if (mapRichiesteIntegrazioni.containsKey(keyIntegrazione)) {
						iteratorRichiesteIntegrazioni.remove();
					} else {
						mapRichiesteIntegrazioni.put(keyIntegrazione, currentRichiestaIntegrazione);
					}
				}
			}
		}

		// inverto ordine pratiche, per mostrarle dalla revisione più recente a quella
		// più vecchia
		Collections.reverse(listaPratiche);

		// gestione paginazione risultati
		int start = 0;
		int end = listaPratiche.size();
		if (pageable.getPageSize() != 1) {
			start = (int) pageable.getOffset();
			end = (start + pageable.getPageSize()) > listaPratiche.size() ? listaPratiche.size()
					: (start + pageable.getPageSize());
		}
		return new PageImpl<Pratica>(listaPratiche.subList(start, end), pageable, listaPratiche.size())
				.map(praticaMapper::entityToDto);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO archiviazione(Long idPratica, Long idUtente) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		StatoPratica statoPraticaArchiviata = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_ARCHIVIATA)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica RIGETTATA non trovato"));

		if (pratica.getStatoPratica().equals(statoPraticaArchiviata)) {
			throw new BusinessException(ErrorCode.E13, "Errore: la pratica si trova già nello stato 'Archiviata'");
		}
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_ARCHIVIATA));
		pratica.setStatoPratica(statoPraticaArchiviata);
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		praticaRepository.save(pratica);

		// elimino notifiche pratica
		notificaScadenzarioRepository.deleteByPratica(pratica);

		log.info("Passaggio pratica in stato ARCHIVIATA avvenuto con successo");

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public void prorogaVerificheApertura(Long idPratica) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		// controllo se la pratica sia nello stato corretto per chiedere una proroga
		if (!pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_CONCESSIONE_VALIDA)) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: la pratica di riferimento non è nello stato 'Concessione Valida', pertanto non può essere richiesta una proroga");
		}

		boolean primoInserimento = true;

		// in caso si tratti gia' di una pratica di proroga, recupero la pratica
		// originaria sulla quale vanno fatti i controlli
		if (pratica.getTipoProcesso().getId().equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)) {
			pratica = praticaRepository.findById(pratica.getIdPraticaOriginaria())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID pratica originaria non presente nel sistema"));

			primoInserimento = false;
		}


		StatoPratica statoPraticaRigettata = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_RIGETTATA).orElseThrow(
				() -> new BusinessException(ErrorCode.ERRORE_INTERNO, ErrorCode.ERRORE_INTERNO.getMessage()));
		List<Pratica> proroghe = praticaRepository.findByIdPraticaOriginariaAndStatoPraticaNotOrderByIdDesc(pratica.getId(), statoPraticaRigettata);

		/*
		revocata, annullata, decaduta, archiviata, terminata
		 */

		StatoPratica statoPraticaRevocata = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_REVOCATA).orElseThrow(
				() -> new BusinessException(ErrorCode.ERRORE_INTERNO, ErrorCode.ERRORE_INTERNO.getMessage()));
		StatoPratica statoPraticaAnnullata = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_ANNULLATA).orElseThrow(
				() -> new BusinessException(ErrorCode.ERRORE_INTERNO, ErrorCode.ERRORE_INTERNO.getMessage()));
		StatoPratica statoPraticaDecaduta = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_DECADUTA).orElseThrow(
				() -> new BusinessException(ErrorCode.ERRORE_INTERNO, ErrorCode.ERRORE_INTERNO.getMessage()));
		StatoPratica statoPraticaArchiviata = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_ARCHIVIATA).orElseThrow(
				() -> new BusinessException(ErrorCode.ERRORE_INTERNO, ErrorCode.ERRORE_INTERNO.getMessage()));
		StatoPratica statoPraticaTerminata = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_TERMINATA).orElseThrow(
				() -> new BusinessException(ErrorCode.ERRORE_INTERNO, ErrorCode.ERRORE_INTERNO.getMessage()));

		List<Pratica> prorogheControlloAvvio = this.praticaRepository.findByIdPraticaOriginariaAndStatoPraticaNotInOrderByIdDesc(
			pratica.getId(),
			List.of(
				statoPraticaRigettata,
				statoPraticaRevocata,
				statoPraticaAnnullata,
				statoPraticaDecaduta,
				statoPraticaArchiviata,
				statoPraticaTerminata
			)
		);

		// controllo che non sia già stata inserita una richiesta di proroga
		if (primoInserimento) {
			if (!prorogheControlloAvvio.isEmpty()) {
				throw new BusinessException(ErrorCode.E7,
						"Errore: è stata già inserita una richiesta di proroga per questa pratica");
			}
		} else {
			Long idPraticaProroga = pratica.getId();

			if (prorogheControlloAvvio.stream().anyMatch(
					p -> p.getIdProrogaPrecedente() != null && p.getIdProrogaPrecedente().equals(idPraticaProroga))) {
				throw new BusinessException(ErrorCode.E7,
						"Errore: è stata già inserita una richiesta di proroga per questa pratica");
			}
		}

		// Verifico limite temporale massimo complessivo.
		// La proroga di concessione temporanea può essere richiesta massimo 2 volte e
		// ciascuna richiesta deve essere di durata
		// minore della richiesta originaria. Per i cantieri edili e stradali la proroga
		// può essere richiesta di un ulteriore anno.
		// Inoltre, eccetto per i cantieri edili e stradali, la somma della durata della
		// concessione e della proroga non deve
		// superare l’anno. Di conseguenza, nel caso venga richiesta la concessione
		// temporanea della durata di un anno non sarà
		// possibile richiedere una proroga (eccetto per i cantieri edili e stradali).

		//Verifico se mancano meno di 15 giorni alla data scadenza occupazione
		long ctrGiorni = ChronoUnit.DAYS.between(LocalDate.now(), pratica.getDatiRichiesta().getDataScadenzaOccupazione());
/*
		if (ctrGiorni < giorniMinAllaScadenza) {
			throw new BusinessException(ErrorCode.E16,
					"Per questa pratica non è più possibile effettuare richieste di proroga: mancano meno di 15 giorni alla scadenza della concessione");
		}*/

		if (pratica.getDatiRichiesta().getAttivitaDaSvolgere().getId().equals(Constants.ID_TIPO_ATTIVITA_EDILIZIA)) {
			if (!proroghe.isEmpty() && proroghe.size() >= numMaxProrogheEdilizia) {
				throw new BusinessException(ErrorCode.E15,
						"Non è più possibile avviare una proroga della concessione in quanto è stato raggiunto il numero massimo");
			}
		} else {
			if (!proroghe.isEmpty() && proroghe.size() >= numMaxProrogheAltro) {
				throw new BusinessException(ErrorCode.E15,
						"Non è più possibile avviare una proroga della concessione in quanto è stato raggiunto il numero massimo");
			}

			//la somma della concessione e della proroga non deve superare un anno nel caso in cui la richiesta non sia riferita a cantieri edili e stradali
			ctrGiorni = ChronoUnit.DAYS.between(pratica.getDatiRichiesta().getDataInizioOccupazione(), pratica.getDatiRichiesta().getDataScadenzaOccupazione());
			if (!proroghe.isEmpty()) {
				for (Pratica proroga : proroghe) {
					ctrGiorni += ChronoUnit.DAYS.between(proroga.getDatiRichiesta().getDataInizioOccupazione(),
							proroga.getDatiRichiesta().getDataScadenzaOccupazione());
				}
			}
			if (ctrGiorni >= giorniTotMaxProrogheAltro) {
				throw new BusinessException(ErrorCode.E16,
						"Per questa pratica non è più possibile effettuare richieste di proroga: è stato raggiunto il numero di giorni massimo");
			}
		}
	}

	@LogEntryExit(showArgs = true)
	@Override
	public PraticaDTO getPraticaPrecompilataProroga(Long idPratica) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));
		Pratica result = new Pratica();

		result.setIdPraticaOriginaria(pratica.getId());
		result.setMunicipio(pratica.getMunicipio());
		result.setFirmatario(pratica.getFirmatario());
		result.getFirmatario().setId(null);
		if (pratica.getDestinatario() != null) {
			result.setDestinatario(pratica.getDestinatario());
			result.getDestinatario().setId(null);
		}

		result.setDatiRichiesta(pratica.getDatiRichiesta());
		result.getDatiRichiesta().setId(null);

		LocalTime oraScadenzaOccupazione = (result.getDatiRichiesta().getOraScadenzaOccupazione() == null) ?
			LocalTime.of(23, 59) : result.getDatiRichiesta().getOraScadenzaOccupazione();
		LocalDateTime scadenzaOccupazione = LocalDateTime.of(
			result.getDatiRichiesta().getDataScadenzaOccupazione(),
			oraScadenzaOccupazione
		).plus(1, ChronoUnit.MINUTES);
		result.getDatiRichiesta().setDataInizioOccupazione(scadenzaOccupazione.toLocalDate());
		if (result.getDatiRichiesta().getOraScadenzaOccupazione() != null) {
			result.getDatiRichiesta().setOraInizioOccupazione(scadenzaOccupazione.toLocalTime());
		}

		/*if(result.getDatiRichiesta().getOraScadenzaOccupazione() != null) {
			LocalDateTime dataScadenzaOccupazioneCompleta = LocalDateTime.of(
				result.getDatiRichiesta().getDataScadenzaOccupazione(),
				result.getDatiRichiesta().getOraScadenzaOccupazione()
			).plus(1, ChronoUnit.MINUTES);
			result.getDatiRichiesta().setDataInizioOccupazione(dataScadenzaOccupazioneCompleta.toLocalDate());
			result.getDatiRichiesta().setOraInizioOccupazione(dataScadenzaOccupazioneCompleta.toLocalTime());
		} else {
			LocalDate date = result.getDatiRichiesta().getDataScadenzaOccupazione().plusDays(1);
            result.getDatiRichiesta().setDataInizioOccupazione(date);
			result.getDatiRichiesta().setOraInizioOccupazione(null);
		}*/

		result.getDatiRichiesta().setDataScadenzaOccupazione(null);
		result.getDatiRichiesta().setOraScadenzaOccupazione(null);

		result.getDatiRichiesta().setFlagAccettazioneRegSuoloPubblico(false);
		result.getDatiRichiesta().setFlagConoscenzaTassaOccupazione(false);
		result.getDatiRichiesta().setFlagNonModificheRispettoConcessione(false);
		result.getDatiRichiesta().setFlagObbligoRiparazioneDanni(false);
		result.getDatiRichiesta().setFlagRispettoDisposizioniRegolamento(false);
		result.getDatiRichiesta().setFlagRispettoInteresseTerzi(false);

		return praticaMapper.entityToDto(result);
	}

	@LogEntryExit(showArgs = true)
	@Override
	public void checkSovrapposizioneUbicazione(VerificaOccupazioneDTO verificaOccupazione) {

		Pratica pratica = praticaRepository.findById(verificaOccupazione.getIdPratica())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						verificaOccupazione.getIdPratica()));

		List<String> overlapped = datiRichiestaService.existSovrapposizioneCoordUbicazione(pratica.getDatiRichiesta(),
				verificaOccupazione.getTipoOperazione(), verificaOccupazione.getCoordUbicazioneDefinitiva());
		if (overlapped.size() > 0) {
			throw new BusinessException(ErrorCode.E19,
					"Attenzione: esiste già una pratica attiva per la stessa ubicazione nello stesso periodo temporale. La pratica è associata al CF: " +
							overlapped.stream().collect(Collectors.joining(", ")));
		}
	}

	@LogEntryExit(showArgs = true)
	@Transactional
	@Override
	public PraticaDTO verificaOccupazione(VerificaOccupazioneDTO verificaOccupazione) {
		Pratica pratica = praticaRepository.findById(verificaOccupazione.getIdPratica())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						verificaOccupazione.getIdPratica()));

		Utente utente = utenteRepository.findById(verificaOccupazione.getIdUtente())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema",
						verificaOccupazione.getIdUtente()));

		// controllo coerenza stato pratica
		if (!Utils.getIdStatiPraticaVerificaOccupazione().contains(pratica.getStatoPratica().getId())) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: non è possibile effettuare la verifica dell'occupazione nello stato attuale della pratica",
					pratica.getStatoPratica().getId());
		}

		List<String> overlapped = datiRichiestaService.existSovrapposizioneCoordUbicazione(pratica.getDatiRichiesta(),
				verificaOccupazione.getTipoOperazione(), verificaOccupazione.getCoordUbicazioneDefinitiva());
		if (overlapped.size() > 0 && !verificaOccupazione.isSkipCheck()) {
			throw new BusinessException(ErrorCode.E19,
					"Attenzione: esiste già una pratica attiva per la stessa ubicazione nello stesso periodo temporale. La pratica è associata al CF: " +
					overlapped.stream().collect(Collectors.joining(", ")));
		}

		// aggiorno valore ubicazione definitiva
		datiRichiestaService.verificaOccupazione(pratica.getDatiRichiesta(), verificaOccupazione.getTipoOperazione(),
				verificaOccupazione.getCoordUbicazioneDefinitiva());

		// aggiornamento pratica
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		pratica.setInfoPassaggioStato("Verifica occupazione");

		praticaRepository.save(pratica);

		return praticaMapper.entityToDto(pratica);
	}

	@LogEntryExit(showArgs = true)
	@Transactional
	@Override
	public PraticaDTO insertPraticaCompletaEgov(PraticaEgovInsertRequest praticaEgovInsertRequest) {
		if (StringUtils.isBlank(praticaEgovInsertRequest.getPratica().getNomeCittadinoEgov())
				|| StringUtils.isBlank(praticaEgovInsertRequest.getPratica().getCognomeCittadinoEgov())
				|| StringUtils.isBlank(praticaEgovInsertRequest.getPratica().getCfCittadinoEgov())) {
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
		praticaEgovInsertRequest.getPratica().setIdUtente(utentiEgov.get(0).getId());

		PraticaDTO pratica = this.insertPratica(praticaEgovInsertRequest.getPratica());

		for (AllegatoDTO allegato : praticaEgovInsertRequest.getAllegati()) {
			AllegatoPraticaInsertRequest allegatoPraticaInsertRequest = new AllegatoPraticaInsertRequest();
			allegatoPraticaInsertRequest.setIdPratica(pratica.getId());
			allegatoPraticaInsertRequest.setAllegato(allegato);

			allegatoService.insertAllegatoPratica(allegatoPraticaInsertRequest);
		}

		pratica = this.switchToStatoInserita(
												pratica.getId(),
												pratica.getUtenteCreazione().getId(),
												praticaEgovInsertRequest.getPratica().getNumeroProtocollo(),
												praticaEgovInsertRequest.getPratica().getAnno(),
												praticaEgovInsertRequest.getPratica().getDataProtocollo()
				);

		log.info("Inserimento completo pratica da EGOV avvenuto con successo");

		return pratica;
	}

	@LogEntryExit(showArgs = true)
	@Transactional
	@Override
	public PraticaDTO insertPraticaEgov(PraticaInsertEditRequest praticaInsertRequest) {

		if (StringUtils.isBlank(praticaInsertRequest.getNomeCittadinoEgov())
				|| StringUtils.isBlank(praticaInsertRequest.getCognomeCittadinoEgov())
				|| StringUtils.isBlank(praticaInsertRequest.getCfCittadinoEgov())) {
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
		praticaInsertRequest.setIdUtente(utentiEgov.get(0).getId());

		PraticaDTO pratica = this.insertPratica(praticaInsertRequest);

		log.info("Inserimento pratica da EGOV avvenuto con successo");

		return pratica;
	}

	@LogEntryExit(showArgs = true)
	@Transactional
	@Override
	@Deprecated
	public PraticaDTO switchToStatoInseritaEgov(StatoInseritaEgovRequest statoInsertaEgovRequest) {
		Pratica pratica = praticaRepository.findById(statoInsertaEgovRequest.getIdPratica())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						statoInsertaEgovRequest.getIdPratica()));

		if (StringUtils.isBlank(statoInsertaEgovRequest.getNomeCittadinoEgov())
				|| StringUtils.isBlank(statoInsertaEgovRequest.getCognomeCittadinoEgov())
				|| StringUtils.isBlank(statoInsertaEgovRequest.getCfCittadinoEgov())) {
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

		Utente utente = utentiEgov.get(0);

		if (!allegatoService.checkAllegatiObbligatoriInserimento(pratica, utente.getGruppo())) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: non risultano presenti gli allegati obbligatori per l'inserimento della pratica");
		}

		StatoPratica statoPraticaPreOperazione = pratica.getStatoPratica();
		//recupero allegati pratica
		List<Allegato> allegati=allegatoRepository.findByPraticaAndStatoPratica(pratica.getId(), statoPraticaPreOperazione.getId());

		StatoPratica statoPraticaInserita = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_INSERITA)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica INSERITA non trovato"));

		// aggiornamento pratica
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_INSERITA));
		pratica.setStatoPratica(statoPraticaInserita);
		pratica.setDataInserimento(LocalDateTime.now().withNano(0));
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));
		pratica.setDataScadenzaPratica(LocalDateTime.now().withNano(0).plusDays(numGiorniTermineProcedimentale));

		pratica.setNomeCittadinoEgov(statoInsertaEgovRequest.getNomeCittadinoEgov());
		pratica.setCognomeCittadinoEgov(statoInsertaEgovRequest.getCognomeCittadinoEgov());
		pratica.setCfCittadinoEgov(statoInsertaEgovRequest.getCfCittadinoEgov());

		praticaRepository.save(pratica);

		// richiesta numero protocollo
		ProtocolloResponse protocolloResponse = null;

		if(callNewEndpoint){
			protocolloResponse = protocollazioneService.getNumeroProtocolloEntrata(
					pratica,
					utente,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_CITTADINO,
					Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA,
					allegati,
					BOZZA_TO_INSERITA);

		}else{
			protocolloResponse = protocollazioneService.getNumeroProtocolloEntrata(pratica, utente, Constants.TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_CITTADINO);
		}

		// inserimento protocollo
		Protocollo protocollo = protocolloService.insertProtocollo(pratica, statoPraticaInserita, protocolloResponse,
				null);

		// aggiungo protocollo all'oggetto pratica
		pratica.getProtocolli().add(protocollo);

		log.info("Passaggio pratica da EGOV in stato INSERITA avvenuto con successo");

		// invio mail di notifica
		this.sendEmailNotificaInserimentoPratica(utente, pratica, protocollo.getCodiceProtocollo());

		return praticaMapper.entityToDto(pratica);
	}

	@LogEntryExit(showArgs = true)
	@Transactional
	@Override
	public PraticaDTO editDateOccupazioneEgov(DateOccupazioneEgovEditRequest dateOccupazioneEgovEditRequest) {

		if (dateOccupazioneEgovEditRequest.getIdPratica() == null
				&& StringUtils.isBlank(dateOccupazioneEgovEditRequest.getNumeroProtocollo())) {
			throw new ValidationException("Errore: è obbligatorio almeno uno fra Id Pratica e Numero Protocollo");
		}

		if (StringUtils.isBlank(dateOccupazioneEgovEditRequest.getNomeCittadinoEgov())
				|| StringUtils.isBlank(dateOccupazioneEgovEditRequest.getCognomeCittadinoEgov())
				|| StringUtils.isBlank(dateOccupazioneEgovEditRequest.getCfCittadinoEgov())) {
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
		if (dateOccupazioneEgovEditRequest.getIdPratica() != null) {
			pratica = praticaRepository.findById(dateOccupazioneEgovEditRequest.getIdPratica()).orElseThrow(
					() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
							dateOccupazioneEgovEditRequest.getIdPratica()));
		} else {
			Protocollo protocollo = protocolloService
					.getProtocolloByNumeroProtcollo(dateOccupazioneEgovEditRequest.getNumeroProtocollo());
			if (protocollo == null) {
				throw new BusinessException(ErrorCode.E21, "Errore: numero protocollo non presente nel sistema",
						dateOccupazioneEgovEditRequest.getNumeroProtocollo());
			}

			pratica = protocollo.getPratica();
		}

		DateOccupazioneEditRequest dateOccupazioneEditRequest = new DateOccupazioneEditRequest();

		dateOccupazioneEditRequest.setIdPratica(pratica.getId());
		dateOccupazioneEditRequest.setNumeroProtocollo(dateOccupazioneEgovEditRequest.getNumeroProtocollo());
		dateOccupazioneEditRequest.setAnnoProtocollo(dateOccupazioneEgovEditRequest.getAnno());
		dateOccupazioneEditRequest.setDataProtocollo(dateOccupazioneEgovEditRequest.getDataProtocollo());
		dateOccupazioneEditRequest.setDataInizioOccupazione(dateOccupazioneEgovEditRequest.getDataInizioOccupazione());
		dateOccupazioneEditRequest.setOraInizioOccupazione(dateOccupazioneEgovEditRequest.getOraInizioOccupazione());
		dateOccupazioneEditRequest.setDataScadenzaOccupazione(dateOccupazioneEgovEditRequest.getDataScadenzaOccupazione());
		dateOccupazioneEditRequest.setOraScadenzaOccupazione(dateOccupazioneEgovEditRequest.getOraScadenzaOccupazione());
		dateOccupazioneEditRequest.setNomeCittadinoEgov(dateOccupazioneEgovEditRequest.getNomeCittadinoEgov());
		dateOccupazioneEditRequest.setCognomeCittadinoEgov(dateOccupazioneEgovEditRequest.getCognomeCittadinoEgov());
		dateOccupazioneEditRequest.setCfCittadinoEgov(dateOccupazioneEgovEditRequest.getCfCittadinoEgov());
		dateOccupazioneEditRequest.setOriginEgov(dateOccupazioneEgovEditRequest.isOriginEgov());

		this.editDateOccupazione(utentiEgov.get(0).getUsername(), dateOccupazioneEditRequest);

		return praticaMapper.entityToDto(pratica);
	}

	@Override
	public List<PraticaDTO> getPraticheEgovAttesaPagamentoBollo(String codiceFiscalePiva) {
		List<Integer> idStati=new ArrayList<>();
		idStati.add(Constants.ID_STATO_PRATICA_ATTESA_DI_PAGAMENTO);

		return praticaRepository.findPraticheByStatoPraticaAndCodiceFiscalePiva(idStati, codiceFiscalePiva).stream().map(praticaMapper::entityToDto)
				.collect(Collectors.toList());
	}
	@Override
	public List<PraticaDTO> getPraticheEgovRettificaDate(String codiceFiscalePiva) {
		List<Integer> idStati=new ArrayList<>();
		idStati.add(Constants.ID_STATO_PRATICA_RETTIFICA_DATE);

		return praticaRepository.findPraticheByStatoPraticaAndCodiceFiscalePiva(idStati, codiceFiscalePiva).stream().map(praticaMapper::entityToDto)
				.collect(Collectors.toList());
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<PraticaDTO> getPraticheEgov(String codiceFiscale, String codiceProtocollo, Long idPratica) {
		Specification<Pratica> conditions = Specification.where(praticaSpecification.inizialize());

		if (StringUtils.isNotBlank(codiceProtocollo)) {
			Protocollo protocollo = protocolloService.getProtocolloByCodice(codiceProtocollo);


			if (protocollo != null) {
				conditions = praticaSpecification.addSpecificationToConditionListAnd(
						praticaSpecification.inProtocollo(List.of(protocollo)), conditions);
			} else {
				// condizione fittizia per non far ritornare alcun risultato
				conditions = conditions.and(praticaSpecification.alwaysFalse());
			}
		}

		if (idPratica != null) {
			Specification<Pratica> conditionPraticaId = Specification
					.where(praticaSpecification.praticaById(idPratica).orElseThrow(RuntimeException::new));
			conditions = conditions.and(conditionPraticaId);
		}

		// gestione filtri ricerca sui richiedenti
		List<Richiedente> listaRichiedenti = richiedenteService.searchRichiedentiByCodiceFiscale(codiceFiscale);

		if (!listaRichiedenti.isEmpty()) {
			Specification<Pratica> conditionsDestinatarioOr = Specification
					.where(praticaSpecification.inFirmatario(listaRichiedenti).orElseThrow(RuntimeException::new));

			conditionsDestinatarioOr = praticaSpecification.addSpecificationToConditionListOr(
					praticaSpecification.inDestinatario(listaRichiedenti), conditionsDestinatarioOr);

			conditions = conditions.and(conditionsDestinatarioOr);
		} else {
			// se non sono stati trovati richiedenti corrispondenti ai filtri imposto una
			// condizione fittizzia per
			// non far ritornare alcun risultato
			conditions = conditions.and(praticaSpecification.alwaysFalse());
		}

		return praticaRepository.findAll(conditions).stream().map(praticaMapper::entityToDto).map(
				e -> {
					String protocollo = null;
					for(ProtocolloDTO p: e.getProtocolli()) {
						if (p.getIdStatoPratica().equals(Constants.ID_STATO_PRATICA_INSERITA)) {
							protocollo = p.getCodiceProtocollo();
							break;
						}
					}
					e.setCodiceProtocollo(protocollo);
					return e;
				}
		).collect(Collectors.toList());
	}

	@LogEntryExit(showArgs = true)
	@Transactional
	@Override
	public PraticaDTO insertProrogaCompletaEgov(ProrogaEgovInsertRequest prorogaEgovInsertRequest) {

		PraticaDTO pratica = this.insertPratica(this.buildInsertRequestProrogaFromEgov(prorogaEgovInsertRequest.getPratica()));

		for (AllegatoDTO allegato : prorogaEgovInsertRequest.getAllegati()) {
			AllegatoPraticaInsertRequest allegatoPraticaInsertRequest = new AllegatoPraticaInsertRequest();
			allegatoPraticaInsertRequest.setIdPratica(pratica.getId());
			allegatoPraticaInsertRequest.setAllegato(allegato);

			allegatoService.insertAllegatoPratica(allegatoPraticaInsertRequest);
		}

		pratica = this.switchToStatoInserita(pratica.getId(), pratica.getUtenteCreazione().getId(), prorogaEgovInsertRequest.getPratica().getNumeroProtocollo(), prorogaEgovInsertRequest.getPratica().getAnno(), prorogaEgovInsertRequest.getPratica().getDataProtocollo());

		log.info("Inserimento completo pratica di proroga da EGOV avvenuto con successo");

		return pratica;
	}

	@LogEntryExit(showArgs = true)
	@Transactional
	@Override
	public PraticaDTO insertProrogaEgov(PraticaProrogaEgovInsertEditRequest praticaProrogaEgovInsertEditRequest) {
		PraticaDTO pratica = this
				.insertPratica(this.buildInsertRequestProrogaFromEgov(praticaProrogaEgovInsertEditRequest));
		log.info("Inserimento pratica di proroga da EGOV avvenuto con successo");

		return pratica;
	}

	@Override
	@LogEntryExit(showArgs = true)
	public PraticaDTO getPraticaPrecompilataRinuncia(Long idPratica) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Pratica result = new Pratica();

		result.setIdPraticaOriginaria(pratica.getId());
		result.setMunicipio(pratica.getMunicipio());
		result.setFirmatario(pratica.getFirmatario());
		result.getFirmatario().setId(null);
		if (pratica.getDestinatario() != null) {
			result.setDestinatario(pratica.getDestinatario());
			result.getDestinatario().setId(null);
		}

		result.setDatiRichiesta(pratica.getDatiRichiesta());
		result.getDatiRichiesta().setId(null);
		result.getDatiRichiesta().setFlagAccettazioneRegSuoloPubblico(false);
		result.getDatiRichiesta().setFlagConoscenzaTassaOccupazione(false);
		result.getDatiRichiesta().setFlagNonModificheRispettoConcessione(false);
		result.getDatiRichiesta().setFlagObbligoRiparazioneDanni(false);
		result.getDatiRichiesta().setFlagRispettoDisposizioniRegolamento(false);
		result.getDatiRichiesta().setFlagRispettoInteresseTerzi(false);

		return praticaMapper.entityToDto(result);
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO insertRinunciaCompletaEgov(RinunciaEgovInsertRequest rinunciaEgovInsertRequest) {
		PraticaDTO pratica = this
				.insertPratica(this.buildInsertRequestRinunciaFromEgov(rinunciaEgovInsertRequest.getPratica()));

		for (AllegatoDTO allegato : rinunciaEgovInsertRequest.getAllegati()) {
			AllegatoPraticaInsertRequest allegatoPraticaInsertRequest = new AllegatoPraticaInsertRequest();
			allegatoPraticaInsertRequest.setIdPratica(pratica.getId());
			allegatoPraticaInsertRequest.setAllegato(allegato);

			allegatoService.insertAllegatoPratica(allegatoPraticaInsertRequest);
		}

		pratica = this.switchToStatoInserita(pratica.getId(), pratica.getUtenteCreazione().getId(), rinunciaEgovInsertRequest.getPratica().getNumeroProtocollo(), rinunciaEgovInsertRequest.getPratica().getAnno(), rinunciaEgovInsertRequest.getPratica().getDataProtocollo());

		log.info("Inserimento completo pratica di rinuncia da EGOV avvenuto con successo");

		return pratica;
	}

	@LogEntryExit(showArgs = true)
	@Transactional
	@Override
	public PraticaDTO insertRinunciaEgov(PraticaRinunciaEgovInsertEditRequest praticaRinunciaEgovInsertEditRequest) {
		PraticaDTO pratica = this
				.insertPratica(this.buildInsertRequestRinunciaFromEgov(praticaRinunciaEgovInsertEditRequest));
		log.info("Inserimento pratica di rinuncia da EGOV avvenuto con successo");

		return pratica;
	}

	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO rettificaPratica(PraticaRettificaRequest praticaRettificaRequest) {

		Utente utente = utenteRepository.findById(praticaRettificaRequest.getIdUtente())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema",
						praticaRettificaRequest.getIdUtente()));

		Pratica pratica = praticaRepository.findById(praticaRettificaRequest.getId())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						praticaRettificaRequest.getId()));

		//recupero allegati pratica
		List<Allegato> allegati=allegatoRepository.findByPraticaAndStatoPratica(pratica.getId(), pratica.getStatoPratica().getId());

		if (!allegatoService.checkAllegatoDeterminaRettifica(pratica)) {
			throw new BusinessException(ErrorCode.E2, "Errore: non risulta caricata la determina di rettifica");
		}

		// se ci sono processi post concessione attivi bisogna bloccare
		if (!this.checkProrogaAttiva(pratica)) {
			throw new BusinessException(ErrorCode.E36, ErrorCode.E36.getMessage());
		}
		if (!this.checkRinunciaAttiva(pratica)) {
			throw new BusinessException(ErrorCode.E37, ErrorCode.E37.getMessage());
		}


		richiedenteService.updateRichiedenteRettifica(pratica.getFirmatario(), praticaRettificaRequest.getFirmatario());

		if (praticaRettificaRequest.getDestinatario() != null) {
			richiedenteService.updateRichiedenteRettifica(pratica.getDestinatario(),
					praticaRettificaRequest.getDestinatario());
		}

		datiRichiestaService.updateDatiRichiestaRettifica(pratica.getDatiRichiesta(),
				praticaRettificaRequest.getDatiRichiesta());

		// aggiornamento pratica
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		praticaRepository.save(pratica);

		// recupero destinatari email e protocollazione
		List<Utente> listaDestinatari = this.getDestinatariDetermina(pratica);

		// richiesta numero protocollo
		ProtocolloResponse protocolloResponse = null;

		if(callNewEndpoint){
			DeterminaDTO determina=getDetermina(pratica.getId(),Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RETTIFICA);

			protocolloResponse = protocollazioneService.getNumeroProtocolloUscitaV2(
					pratica,
					utente,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_RETTIFICA,
					listaDestinatari,
					true,
					determina.getContenuto().getBytes(),//determina
					Utils.getFileExtension(determina.getNomeFile()),
					null);//allegati null in questo caso

		}else{
			protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(pratica, utente,
					Constants.TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_RETTIFICA, listaDestinatari, true);
		}

		// inserimento protocollo
		Protocollo protocolloDetermina = protocolloService.insertProtocollo(pratica, pratica.getStatoPratica(),
				protocolloResponse, TipoOperazioneProtocollo.RETTIFICA_ERRORI_MATERIALI);

		protocolloService.insertDeterminaRettifica(protocolloDetermina,
				praticaRettificaRequest.getCodiceDeterminaRettifica(),
				praticaRettificaRequest.getDataEmissioneDeterminaRettifica());

		// inserimento protocollo sull'allegato
		allegatoService.insertCodiceProtocolloAllegatiPraticaByIdTipo(pratica,
				Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RETTIFICA, protocolloDetermina.getCodiceProtocollo());

		// invio mail di notifica
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);

		this.sendEmailNotificaDeterminaRettificaPratica(listaDestinatari, utente, pratica, protocolloDetermina.getCodiceDeterminaRettifica(),
				protocolloInserimento.getCodiceProtocollo(), protocolloDetermina.getCodiceProtocollo(),
				praticaRettificaRequest.getDataEmissioneDeterminaRettifica());

		return praticaMapper.entityToDto(pratica);
	}

	private boolean checkProrogaAttiva(Pratica pratica) {
		List<Pratica> proroghe = this.praticaRepository.findPraticheByIdOrIdPraticaOriginariaAndStatoPraticaNotInAndTipoProcesso(
			pratica.getId(),
			List.of(
				Constants.ID_STATO_PRATICA_RIGETTATA,
				Constants.ID_STATO_PRATICA_ARCHIVIATA,
				Constants.ID_STATO_PRATICA_CONCESSIONE_VALIDA,
				Constants.ID_STATO_PRATICA_TERMINATA,
				Constants.ID_STATO_PRATICA_ANNULLATA,
				Constants.ID_STATO_PRATICA_RINUNCIATA
			),
			Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA
		);
		return proroghe.isEmpty();
	}
	private boolean checkRinunciaAttiva(Pratica pratica) {
		List<Pratica> proroghe = this.praticaRepository.findPraticheByIdOrIdPraticaOriginariaAndStatoPraticaNotInAndTipoProcesso(
			pratica.getId(),
			List.of(
				Constants.ID_STATO_PRATICA_RIGETTATA,
				Constants.ID_STATO_PRATICA_ARCHIVIATA,
				Constants.ID_STATO_PRATICA_CONCESSIONE_VALIDA,
				Constants.ID_STATO_PRATICA_TERMINATA,
				Constants.ID_STATO_PRATICA_ANNULLATA,
				Constants.ID_STATO_PRATICA_RINUNCIATA
			),
			Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA
		);
		return proroghe.isEmpty();
	}
	@Override
	@LogEntryExit(showArgs = true)
	@Transactional
	public PraticaDTO inserimentoDeterminaRda(Long idPratica, Long idUtente, String notaAlCittadino,
											  String codiceDetermina, LocalDate dataEmissioneDetermina, TipoDetermina tipoDetermina) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		// controllo che sia possibile effettuare un processo di revoca, decadenza o
		// annullamento
		this.rdaVerificheInserimento(pratica);

		StatoPratica statoPraticaPreOperazione = pratica.getStatoPratica();
		StatoPratica statoPraticaPostDetermina = null;
		String tipoEventoProtocollo = null;
		Integer idTipoProcesso = null;
		Integer idTipoAllegato=null;

		switch (tipoDetermina) {
			case REVOCA:
				statoPraticaPostDetermina = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_REVOCATA)
						.orElseThrow(() -> new RuntimeException("Errore: stato pratica REVOCATA non trovato"));
				tipoEventoProtocollo = Constants.TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_REVOCA;
				idTipoProcesso = Constants.ID_TIPO_PROCESSO_REVOCA_DELLA_CONCESSIONE;
				idTipoAllegato=Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_REVOCA;
				break;

			case DECADENZA:
				statoPraticaPostDetermina = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_DECADUTA)
						.orElseThrow(() -> new RuntimeException("Errore: stato pratica DECADUTA non trovato"));
				tipoEventoProtocollo = Constants.TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_DECADENZA;
				idTipoProcesso = Constants.ID_TIPO_PROCESSO_DECADENZA_DELLA_CONCESSIONE;
				idTipoAllegato=Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_DECADENZA;
				break;

			case ANNULLAMENTO:
				statoPraticaPostDetermina = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_ANNULLATA)
						.orElseThrow(() -> new RuntimeException("Errore: stato pratica ANNULLATA non trovato"));
				tipoEventoProtocollo = Constants.TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_ANNULLAMENTO;
				idTipoProcesso = Constants.ID_TIPO_PROCESSO_ANNULLAMENTO_DELLA_CONCESSIONE;
				idTipoAllegato=Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_ANNULLAMENTO;
				break;
		}

		TipoProcesso tipoProcesso = tipoProcessoRepository.findById(idTipoProcesso).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID tipo processo non presente nel sistema"));

		if (!allegatoService.checkAllegatiObbligatori(pratica, statoPraticaPreOperazione, tipoProcesso,
				utente.getGruppo())) {
			throw new BusinessException(ErrorCode.E2, "Errore: non risulta presente la determina");
		}

		pratica.setNotaAlCittadinoRda(notaAlCittadino);
		pratica.setCodiceDeterminaRda(codiceDetermina);
		pratica.setDataEmissioneDeterminaRda(dataEmissioneDetermina);
		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), statoPraticaPostDetermina.getId()));
		pratica.setStatoPratica(statoPraticaPostDetermina);
		pratica.setUtenteModifica(utente);
		pratica.setDataModifica(LocalDateTime.now().withNano(0));

		praticaRepository.save(pratica);

		// recupero destinatari email e protocollazione
		List<Utente> listaDestinatari = null;
		if (tipoDetermina.equals(TipoDetermina.ANNULLAMENTO)) {
			listaDestinatari = this.getDestinatariDeterminaConPoliziaERagioneria(pratica);
		} else {
			listaDestinatari = this.getDestinatariDeterminaConPolizia(pratica);
		}

		// richiesta numero protocollo
		ProtocolloResponse protocolloResponse = null;

		if(callNewEndpoint){

			DeterminaDTO determina=getDetermina(pratica.getId(), idTipoAllegato);

			protocolloResponse = protocollazioneService.getNumeroProtocolloUscitaV2(
					pratica,
					utente,
					tipoEventoProtocollo,
					listaDestinatari,
					true,
					determina.getContenuto().getBytes(),//determina
					Utils.getFileExtension(determina.getNomeFile()),
					null);//allegati null in questo caso

		}else{
			protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(pratica, utente,
					tipoEventoProtocollo, listaDestinatari, true);
		}

		// inserimento protocollo
		Protocollo protocolloDetermina = protocolloService.insertProtocollo(pratica, statoPraticaPostDetermina,
				protocolloResponse, null);

		// inserimento numero protocollo su allegati
		TipoProcesso tipoProcessoPreAggiornamentoAllegati = pratica.getTipoProcesso();
		pratica.setTipoProcesso(tipoProcesso);
		allegatoService.insertCodiceProtocolloAllegatiPratica(pratica, statoPraticaPreOperazione,
				protocolloDetermina.getCodiceProtocollo());
		pratica.setTipoProcesso(tipoProcessoPreAggiornamentoAllegati);

		// invio mail di notifica
		Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);

		this.sendEmailNotificaDeterminaRda(listaDestinatari, utente, pratica,
				protocolloInserimento.getCodiceProtocollo(), protocolloDetermina.getCodiceProtocollo(),
				pratica.getNotaAlCittadinoRda(), idTipoProcesso);

		// inserisco anche richiesta parere per la richiesta di verifica del ripristino
		// dei luoghi
		richiestaParereService.insertRichiestaVerificaRipristinoLuoghi(pratica, utente);

		return praticaMapper.entityToDto(pratica);
	}

	private DeterminaDTO getDetermina(Long idPratica, Integer idTipoAllegato) {
		DeterminaDTO allegatoDTO = null;

		List<Integer> idTipiAllegati=new ArrayList<>();
		idTipiAllegati.add(idTipoAllegato);

		List<Allegato> allegatti= allegatoRepository.findDeterminaByPraticaAndTipoAllegato(idPratica, idTipiAllegati);

		if(allegatti!=null&&allegatti.size()>0){
			Allegato allegato=allegatti.get(0);

			allegatoDTO = new DeterminaDTO();
			allegatoDTO.setNomeFile(allegato.getNomeFile());
			allegatoDTO.setMimeType(allegato.getMimeType());
			allegatoDTO.setContenuto(new String(Base64.getEncoder().encode(allegato.getFileAllegato())));

		}

		return allegatoDTO;
	}

	@Override
	public DeterminaDTO getDeterminaInAttesaPagamento(Long idPratica) {
		DeterminaDTO allegatoDTO = null;

		List<Integer> idTipiAllegati=new ArrayList<>();

		idTipiAllegati.add(Integer.valueOf(Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_CONCESSIONE));
		idTipiAllegati.add(Integer.valueOf(Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RINUNCIA));
		idTipiAllegati.add(Integer.valueOf(Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RETTIFICA));
		idTipiAllegati.add(Integer.valueOf(Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_REVOCA));
		idTipiAllegati.add(Integer.valueOf(Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_DECADENZA));
		idTipiAllegati.add(Integer.valueOf(Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_ANNULLAMENTO));
		idTipiAllegati.add(Integer.valueOf(Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_PROROGA));

		List<Allegato> allegatti= allegatoRepository.findDeterminaByPraticaTipoAllegatoAndStato(idPratica, idTipiAllegati, Constants.ID_STATO_PRATICA_ATTESA_DI_PAGAMENTO);

		if(allegatti!=null&&allegatti.size()>0){
			Allegato allegato=allegatti.get(0);

			allegatoDTO = new DeterminaDTO();
			allegatoDTO.setNomeFile(allegato.getNomeFile());
			allegatoDTO.setMimeType(allegato.getMimeType());
			allegatoDTO.setContenuto(new String(Base64.getEncoder().encode(allegato.getFileAllegato())));

		}

		return allegatoDTO;
	}

	/**
	 * Costruisce la mail di notifica modifica date occupazione e la invia al
	 * cittadino e all'utente che ha in carico la pratica o che l'ha creata.
	 *
	 * @param utente
	 * @param pratica
	 */
	private void sendEmailCambioDateOccupazione(Utente utente, Pratica pratica) {

		// recupero stato pratica "inserita" per ricercare il numero di protocollo
		// iniziale
		StatoPratica statoPraticaInserita = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_INSERITA)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica INSERITA non trovato"));

		// recupero numero di protocollo dell'inserimento della pratica
		Protocollo protocollo = protocolloService.getProtocollo(pratica, statoPraticaInserita);

		// costruisco comunicazione mail per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMailInsertCittadino = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsertCittadino.setPratica(pratica);
		comunicazioneMailInsertCittadino
				.setOggetto(buildOggettoEmail(pratica,"Modifica Date Occupazione"));
		comunicazioneMailInsertCittadino.setTesto(EmailUtils.getContentEmail(
				EmailUtils.getMessageCambioDateOccupazioneToCittadino(pratica, protocollo.getCodiceProtocollo()),
				"Modifica Date Occupazione"));

		String emailCittadino = pratica.getFirmatario().getEmail();

		if (emailCittadino != null) {
			// setto il flag che indica se e' una PEC
			comunicazioneMailInsertCittadino.setFlagPec(true);

			comunicazioneMailInsertCittadino.setDestinatari(emailCittadino);

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMailInsertCittadino.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertCittadino);
		}

		// costruisco comunicazione mail per l'utente
		ComunicazioneMailInsertDTO comunicazioneMailInsertUtente = new ComunicazioneMailInsertDTO(
				comunicazioneMailInsertCittadino);

		// setto il flag che indica se e' una PEC
		comunicazioneMailInsertUtente.setFlagPec(false);

		comunicazioneMailInsertUtente.setTesto(EmailUtils.getContentEmail(
				EmailUtils.getMessageCambioDateOccupazioneToAttori(pratica, protocollo.getCodiceProtocollo()),
				"Modifica Date Occupazione"));

		// costruisco email per gli attori del municipio
		List<String> toMunicipio = new ArrayList<>();

		// ricerco utenti direttori di municipio
		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDirettoreMunicipio);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		List<Utente> listaUtenti = utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);

		if (pratica.getUtentePresaInCarico() != null) {
			listaUtenti.add(pratica.getUtentePresaInCarico());
		}

		listaUtenti.stream().forEach(u -> toMunicipio.add(u.getEmail()));

		if (!toMunicipio.isEmpty()) {
			comunicazioneMailInsertUtente.setDestinatari(String.join(",", toMunicipio));
			if (utente != null) {
				comunicazioneMailInsertUtente.setDestinatariCc(utente.getEmail());
			}
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertUtente);
		}

	}

	/**
	 * Costruisce le mail di notifica inserimento pratica e le invia al cittadino e
	 * agli utenti coinvolti
	 *
	 * @param pratica
	 * @param codiceProtocollo
	 */
	private void sendEmailNotificaInserimentoPratica(Utente utente, Pratica pratica, String codiceProtocollo) {

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMailInsertCittadino = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsertCittadino.setPratica(pratica);
		comunicazioneMailInsertCittadino
				.setOggetto(buildOggettoEmail(pratica,"Comunicazione avvio della pratica"));
		comunicazioneMailInsertCittadino.setTesto(
				EmailUtils.getContentEmail(EmailUtils.getMessageInserimentoPraticaCittadino(pratica, codiceProtocollo),
						"Comunicazione avvio della pratica"));

		String emailCittadino = pratica.getFirmatario().getEmail();

		if (emailCittadino != null) {
			// setto il flag che indica se e' una PEC
			comunicazioneMailInsertCittadino.setFlagPec(true);

			comunicazioneMailInsertCittadino.setDestinatari(emailCittadino);

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMailInsertCittadino.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertCittadino);
		}

		// costruisco comunicazione email per gli utenti coinvolti
		ComunicazioneMailInsertDTO comunicazioneMailInsertMunicipio = new ComunicazioneMailInsertDTO(
				comunicazioneMailInsertCittadino);
		comunicazioneMailInsertMunicipio.setTesto(EmailUtils.getContentEmail(
				EmailUtils.getMessageInserimentoPraticaUfficiCompetenti(pratica, codiceProtocollo),
				"Comunicazione avvio della pratica"));

		// setto il flag che indica se e' una PEC
		comunicazioneMailInsertMunicipio.setFlagPec(false);

		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
		Gruppo gruppoIstruttoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_ISTRUTTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo istruttore municipio non trovato"));

		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDirettoreMunicipio);
		listaGruppi.add(gruppoIstruttoreMunicipio);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		List<Utente> listaUtenti = utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);
		List<String> toUtente = new ArrayList<>();
		listaUtenti.remove(utente);
		listaUtenti.stream().forEach(u -> toUtente.add(u.getEmail()));

		if (!toUtente.isEmpty()) {
			comunicazioneMailInsertMunicipio.setDestinatari(String.join(",", toUtente));
			comunicazioneMailInsertMunicipio.setDestinatariCc(utente.getEmail());
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertMunicipio);
		}
	}

	/**
	 * Costruisce le mail di notifica presa in carico pratica e le invia al
	 * cittadino e agli utenti coinvolti
	 *
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 */
	private void sendEmailNotificaPresaInCaricoPratica(List<Utente> destinatari, Utente utente, Pratica pratica,
													   String codiceProtocolloPratica, String codiceProtocolloOperazione) {

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMailInsertCittadino = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsertCittadino.setPratica(pratica);
		comunicazioneMailInsertCittadino
				.setOggetto(buildOggettoEmail(pratica,"Comunicazione assegnazione della pratica"));
		comunicazioneMailInsertCittadino.setTesto(EmailUtils.getContentEmail(EmailUtils
						.getMessagePresaInCaricoPraticaCittadino(pratica, codiceProtocolloPratica, codiceProtocolloOperazione),
				"Comunicazione assegnazione della pratica"));

		String emailCittadino = pratica.getFirmatario().getEmail();

		if (emailCittadino != null) {
			// setto il flag che indica se e' una PEC
			comunicazioneMailInsertCittadino.setFlagPec(true);

			comunicazioneMailInsertCittadino.setDestinatari(emailCittadino);

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMailInsertCittadino.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertCittadino);
		}

		// costruisco comunicazione email per gli utenti
		if (!destinatari.isEmpty()) {
			ComunicazioneMailInsertDTO comunicazioneMailInsertMunicipio = new ComunicazioneMailInsertDTO(
					comunicazioneMailInsertCittadino);

			// setto il flag che indica se e' una PEC
			comunicazioneMailInsertMunicipio.setFlagPec(false);

			List<String> toMunicipio = new ArrayList<>();

			// se è presente l'utente assegnatario, vuol dire che la pratica e' stata
			// assegnata dal direttore ed invio quindi la email all'istruttore del
			// municipio,
			// altrimenti la invio al direttore, in quanto la pratica e' stata presa in
			// carico direttamente da un istruttore
			if (pratica.getUtenteAssegnatario() != null) {
				comunicazioneMailInsertMunicipio
						.setTesto(
								EmailUtils.getContentEmail(
										EmailUtils.getMessagePresaInCaricoPraticaToIstruttore(pratica,
												codiceProtocolloPratica, codiceProtocolloOperazione),
										"Comunicazione assegnazione della pratica"));
			} else {
				comunicazioneMailInsertMunicipio
						.setTesto(
								EmailUtils.getContentEmail(
										EmailUtils.getMessagePresaInCaricoPraticaToDirettore(pratica,
												codiceProtocolloPratica, codiceProtocolloOperazione),
										"Comunicazione assegnazione della pratica"));
			}

			destinatari.stream().forEach(u -> toMunicipio.add(u.getEmail()));

			if (!toMunicipio.isEmpty()) {
				comunicazioneMailInsertMunicipio.setDestinatari(String.join(",", toMunicipio));
				comunicazioneMailInsertMunicipio.setDestinatariCc(utente.getEmail());
				comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertMunicipio);
			}
		}
	}

	/**
	 * Costruisce le mail di notifica pronto rilascio
	 *
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 */
	private void sendEmailNotificaProntoRilascio(List<Utente> destinatari, Utente utente, Pratica pratica,
													   String codiceProtocolloPratica, String codiceProtocolloOperazione) {

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMailInsertCittadino = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsertCittadino.setPratica(pratica);
		comunicazioneMailInsertCittadino
				.setOggetto(buildOggettoEmail(pratica,"Comunicazione inserimento ricevute"));
		comunicazioneMailInsertCittadino.setTesto(EmailUtils.getContentEmail(EmailUtils
						.getNotificaRicevutePagateCittadino(pratica, codiceProtocolloPratica, codiceProtocolloOperazione),
				"Comunicazione inserimento ricevute"));

		String emailCittadino = pratica.getFirmatario().getEmail();

		if (emailCittadino != null) {
			// setto il flag che indica se e' una PEC
			comunicazioneMailInsertCittadino.setFlagPec(true);

			comunicazioneMailInsertCittadino.setDestinatari(emailCittadino);

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMailInsertCittadino.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertCittadino);
		}

		// costruisco comunicazione email per gli utenti
		if (!destinatari.isEmpty()) {
			ComunicazioneMailInsertDTO comunicazioneMailInsertMunicipio = new ComunicazioneMailInsertDTO(
					comunicazioneMailInsertCittadino);
			comunicazioneMailInsertMunicipio.setFlagPec(false);
			List<String> toMunicipio = new ArrayList<>();
			if (pratica.getUtenteAssegnatario() != null) {
				comunicazioneMailInsertMunicipio
						.setTesto(
								EmailUtils.getContentEmail(
										EmailUtils.getNotificaRicevutePagateToAttori(pratica,
												codiceProtocolloPratica, codiceProtocolloOperazione),
										"Comunicazione inserimento ricevute"));
			} else {
				comunicazioneMailInsertMunicipio
						.setTesto(
								EmailUtils.getContentEmail(
										EmailUtils.getNotificaRicevutePagateToAttori(pratica,
												codiceProtocolloPratica, codiceProtocolloOperazione),
										"Comunicazione inserimento ricevute"));
			}

			destinatari.stream().forEach(u -> toMunicipio.add(u.getEmail()));

			if (!toMunicipio.isEmpty()) {
				comunicazioneMailInsertMunicipio.setDestinatari(String.join(",", toMunicipio));
				comunicazioneMailInsertMunicipio.setDestinatariCc(utente.getEmail());
				comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertMunicipio);
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

	/**
	 * Costruisce le mail di notifica determina pratica e le invia al cittadino,
	 * agli utenti coinvolti, ai gruppi contrassegnati e al concessionario
	 *
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 */
	private void sendEmailNotificaDeterminaPratica(List<Utente> destinatari, Utente utente, Pratica pratica,
												   String codiceProtocolloPratica, String codiceProtocolloOperazione, LocalDate dataEmissioneDetermina) {
		// recupero file determina
		Integer idTipoAllegato = Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_CONCESSIONE;
		if (pratica.getTipoProcesso().getId().equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)) {
			idTipoAllegato = Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_PROROGA;
		}

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMailInsertCittadino = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsertCittadino.setPratica(pratica);
		comunicazioneMailInsertCittadino
				.setOggetto(buildOggettoEmail(pratica,"Comunicazione inserimento determina"));
		comunicazioneMailInsertCittadino
				.setTesto(EmailUtils.getContentEmail(EmailUtils.getMessageInserimentoDeterminaToCittadino(pratica,
						codiceProtocolloPratica, codiceProtocolloOperazione), "Comunicazione inserimento determina"));

		String emailCittadino = pratica.getFirmatario().getEmail();

		if (emailCittadino != null) {
			comunicazioneMailInsertCittadino.setDestinatari(emailCittadino);

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMailInsertCittadino.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			// setto il flag che indica che e' una PEC
			comunicazioneMailInsertCittadino.setFlagPec(true);

			setAllegatoDetermina(pratica, idTipoAllegato, comunicazioneMailInsertCittadino);

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertCittadino);
		}

		// costruisco comunicazione email per gli attori coinvolti

		List<Utente> utentiConcessionari = destinatari.stream()
				.filter(u -> u.getGruppo().getId().equals(Constants.ID_GRUPPO_CONCESSIONARIO))
				.collect(Collectors.toList());
		destinatari.removeAll(utentiConcessionari);

		List<String> toAttori = new ArrayList<>();
		destinatari.stream().forEach(u -> toAttori.add(u.getEmail()));

		ComunicazioneMailInsertDTO comunicazioneMailInsertAttori = new ComunicazioneMailInsertDTO(
				comunicazioneMailInsertCittadino);

		// setto il flag che indica se e' una PEC
		comunicazioneMailInsertAttori.setFlagPec(false);

		comunicazioneMailInsertAttori
				.setTesto(EmailUtils.getContentEmail(
						EmailUtils.getMessageInserimentoDeterminaToAttori(pratica, codiceProtocolloPratica,
								codiceProtocolloOperazione, pratica.getCodiceDetermina(), dataEmissioneDetermina),
						"Comunicazione inserimento determina"));

		if (!toAttori.isEmpty()) {
			comunicazioneMailInsertAttori.setDestinatari(String.join(",", toAttori));
			comunicazioneMailInsertAttori.setDestinatariCc(utente.getEmail());
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertAttori);
		}

		if (!utentiConcessionari.isEmpty()) {

			// costruisco comunicazione email PEC per il concessionario
			ComunicazioneMailInsertDTO comunicazioneMailInsertConcessionario = new ComunicazioneMailInsertDTO(
					comunicazioneMailInsertCittadino);

			List<String> toConcessionario = new ArrayList<>();

			comunicazioneMailInsertConcessionario.setTesto(EmailUtils.getContentEmail(
					EmailUtils.getMessageInserimentoDeterminaToAttori(pratica, codiceProtocolloPratica,
							codiceProtocolloOperazione, pratica.getCodiceDetermina(), dataEmissioneDetermina),
					"Comunicazione inserimento determina"));

			// setto il flag che indica che e' una PEC
			comunicazioneMailInsertConcessionario.setFlagPec(true);

			setAllegatoDetermina(pratica, idTipoAllegato, comunicazioneMailInsertConcessionario);

			utentiConcessionari.stream().forEach(u -> toConcessionario.add(u.getEmail()));

			if (!toConcessionario.isEmpty()) {
				comunicazioneMailInsertConcessionario.setDestinatari(String.join(",", toConcessionario));
				comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertConcessionario);
			}
		}
	}

	/**
	 * setta l'allegato dell'oggetto ComunicazioneMailInsertDTO
	 * @param pratica
	 * @param comunicazioneMail
	 */
	private void setAllegatoDetermina(Pratica pratica, Integer idTipoAllegato, ComunicazioneMailInsertDTO comunicazioneMail) {
		TipoAllegato tipoAllegatoDetermina = tipoAllegatoRepository.findById(idTipoAllegato)
				.orElseThrow(() -> new RuntimeException("Tipo allegato determina non trovato"));
		Allegato allegatoDetermina = allegatoService.getAllegatoPraticaByTipo(pratica, tipoAllegatoDetermina);

		if (allegatoDetermina != null) {
			if (allegatoDetermina.getMimeType().equals(Constants.MIME_TYPE_DOCX)) {
				comunicazioneMail.setNomeFileAllegato(allegatoDetermina.getNomeFile().replace(".docx", ".pdf"));
				comunicazioneMail.setMimeTypeFileAllegato(Constants.MIME_TYPE_PDF);
				try {
					comunicazioneMail.setFileAllegato(DocUtils.docxToPdf(allegatoDetermina.getFileAllegato()));
				} catch (Docx4JException e) {
					log.error("Errore durante la conversione della determina in pdf", e);
					throw new BusinessException(ErrorCode.E22, "Errore durante la conversione della determina in pdf");
				}
			} else {
				comunicazioneMail.setNomeFileAllegato(allegatoDetermina.getNomeFile());
				comunicazioneMail.setMimeTypeFileAllegato(allegatoDetermina.getMimeType());
				comunicazioneMail.setFileAllegato(allegatoDetermina.getFileAllegato());
			}
		}
	}

	/**
	 * Costruisce le mail di notifica determina di rinuncia e le invia al cittadino,
	 * agli utenti coinvolti, ai gruppi contrassegnati e al concessionario
	 *
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 */
	private void sendEmailNotificaDeterminaRinunciaPratica(List<Utente> destinatari, Utente utente, Pratica pratica,
														   String codiceProtocolloPratica, String codiceProtocolloOperazione, LocalDate dataEmissioneRinuncia) {

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMailInsertCittadino = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsertCittadino.setPratica(pratica);
		comunicazioneMailInsertCittadino
				.setOggetto(buildOggettoEmail(pratica,"Comunicazione inserimento determina di rinuncia"));
		comunicazioneMailInsertCittadino
				.setTesto(
						EmailUtils.getContentEmail(
								EmailUtils.getMessageInserimentoDeterminaRinunciaToCittadino(pratica,
										codiceProtocolloPratica, codiceProtocolloOperazione),
								"Comunicazione inserimento determina di rinuncia"));

		String emailCittadino = pratica.getFirmatario().getEmail();

		if (emailCittadino != null) {
			comunicazioneMailInsertCittadino.setDestinatari(emailCittadino);

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMailInsertCittadino.setDestinatariCc(pratica.getDestinatario().getEmail());
			}
			// setto il flag che indica che e' una PEC
			comunicazioneMailInsertCittadino.setFlagPec(true);

			setAllegatoDetermina(pratica, Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RINUNCIA, comunicazioneMailInsertCittadino);

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertCittadino);
		}

		// costruisco comunicazione email per gli attori coinvolti

		List<Utente> utentiConcessionari = destinatari.stream()
				.filter(u -> u.getGruppo().getId().equals(Constants.ID_GRUPPO_CONCESSIONARIO))
				.collect(Collectors.toList());
		destinatari.removeAll(utentiConcessionari);

		List<String> toAttori = new ArrayList<>();
		destinatari.stream().forEach(u -> toAttori.add(u.getEmail()));

		ComunicazioneMailInsertDTO comunicazioneMailInsertAttori = new ComunicazioneMailInsertDTO(
				comunicazioneMailInsertCittadino);

		// setto il flag che indica se e' una PEC
		comunicazioneMailInsertAttori.setFlagPec(false);

		comunicazioneMailInsertAttori.setTesto(EmailUtils.getContentEmail(
				EmailUtils.getMessageInserimentoDeterminaRinunciaToAttori(pratica, codiceProtocolloPratica,
						codiceProtocolloOperazione, pratica.getCodiceDeterminaRinuncia(), dataEmissioneRinuncia),
				"Comunicazione inserimento determina di rinuncia"));

		if (!toAttori.isEmpty()) {
			comunicazioneMailInsertAttori.setDestinatari(String.join(",", toAttori));
			comunicazioneMailInsertAttori.setDestinatariCc(utente.getEmail());
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertAttori);
		}

		if (!utentiConcessionari.isEmpty()) {

			// costruisco comunicazione email PEC per il concessionario
			ComunicazioneMailInsertDTO comunicazioneMailInsertConcessionario = new ComunicazioneMailInsertDTO(
					comunicazioneMailInsertCittadino);

			List<String> toConcessionario = new ArrayList<>();

			comunicazioneMailInsertConcessionario.setTesto(EmailUtils.getContentEmail(
					EmailUtils.getMessageInserimentoDeterminaRinunciaToAttori(pratica, codiceProtocolloPratica,
							codiceProtocolloOperazione, pratica.getCodiceDeterminaRinuncia(), dataEmissioneRinuncia),
					"Comunicazione inserimento determina di rinuncia"));

			// setto il flag che indica che e' una PEC
			comunicazioneMailInsertConcessionario.setFlagPec(true);

			setAllegatoDetermina(pratica, Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RINUNCIA, comunicazioneMailInsertConcessionario);

			utentiConcessionari.stream().forEach(u -> toConcessionario.add(u.getEmail()));

			if (!toConcessionario.isEmpty()) {
				comunicazioneMailInsertConcessionario.setDestinatari(String.join(",", toConcessionario));
				comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertConcessionario);
			}
		}
	}

	/**
	 * Costruisce le mail di notifica determina pratica e le invia al cittadino,
	 * agli utenti coinvolti, ai gruppi contrassegnati e al concessionario, con
	 * allegato la determina in pdf
	 *
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 */
	private void sendEmailNotificaDeterminaRda(List<Utente> destinatari, Utente utente, Pratica pratica,
											   String codiceProtocolloPratica, String codiceProtocolloOperazione, String notaAlCittadino,
											   Integer idTipoProcesso) {
		Integer idTipoAllegatoDetermina = idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_REVOCA_DELLA_CONCESSIONE)
				? Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_REVOCA
				: idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_DECADENZA_DELLA_CONCESSIONE)
				? Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_DECADENZA
				: idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_ANNULLAMENTO_DELLA_CONCESSIONE)
				? Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_ANNULLAMENTO
				: null;

		String evento = idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_REVOCA_DELLA_CONCESSIONE)
				? "Comunicazione concessione revocata"
				: idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_DECADENZA_DELLA_CONCESSIONE)
				? "Comunicazione concessione decaduta"
				: idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_ANNULLAMENTO_DELLA_CONCESSIONE)
				? "Comunicazione concessione annullata"
				: null;

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMailInsertCittadino = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsertCittadino.setPratica(pratica);
		comunicazioneMailInsertCittadino.setOggetto(buildOggettoEmail(pratica,evento));
		comunicazioneMailInsertCittadino
				.setTesto(EmailUtils.getContentEmail(EmailUtils.getMessageInserimentoDeterminaRdaToCittadino(pratica,
						codiceProtocolloPratica, codiceProtocolloOperazione, notaAlCittadino, idTipoProcesso), evento));

		String emailCittadino = pratica.getFirmatario().getEmail();

		if (emailCittadino != null) {
			comunicazioneMailInsertCittadino.setDestinatari(emailCittadino);

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMailInsertCittadino.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			// setto il flag che indica che e' una PEC
			comunicazioneMailInsertCittadino.setFlagPec(true);

			setAllegatoDetermina(pratica, idTipoAllegatoDetermina, comunicazioneMailInsertCittadino);

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertCittadino);
		}

		// costruisco comunicazione email per gli attori coinvolti

		List<Utente> utentiConcessionari = destinatari.stream()
				.filter(u -> u.getGruppo().getId().equals(Constants.ID_GRUPPO_CONCESSIONARIO))
				.collect(Collectors.toList());
		destinatari.removeAll(utentiConcessionari);

		List<String> toAttori = new ArrayList<>();
		destinatari.stream().forEach(u -> toAttori.add(u.getEmail()));

		ComunicazioneMailInsertDTO comunicazioneMailInsertAttori = new ComunicazioneMailInsertDTO(
				comunicazioneMailInsertCittadino);

		// setto il flag che indica se e' una PEC
		comunicazioneMailInsertAttori.setFlagPec(false);

		comunicazioneMailInsertAttori
				.setTesto(
						EmailUtils.getContentEmail(
								EmailUtils.getMessageInserimentoDeterminaRdaToAttori(pratica, codiceProtocolloPratica,
										codiceProtocolloOperazione, pratica.getCodiceDetermina(), idTipoProcesso),
								evento));

		if (!toAttori.isEmpty()) {
			comunicazioneMailInsertAttori.setDestinatari(String.join(",", toAttori));
			comunicazioneMailInsertAttori.setDestinatariCc(utente.getEmail());
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertAttori);
		}

		if (!utentiConcessionari.isEmpty()) {

			// costruisco comunicazione email PEC per il concessionario
			ComunicazioneMailInsertDTO comunicazioneMailInsertConcessionario = new ComunicazioneMailInsertDTO(
					comunicazioneMailInsertCittadino);

			List<String> toConcessionario = new ArrayList<>();

			comunicazioneMailInsertConcessionario.setTesto(EmailUtils.getContentEmail(
					EmailUtils.getMessageInserimentoDeterminaRdaToAttori(pratica, codiceProtocolloPratica,
							codiceProtocolloOperazione, pratica.getCodiceDetermina(), idTipoProcesso),
					evento));

			// setto il flag che indica che e' una PEC
			comunicazioneMailInsertConcessionario.setFlagPec(true);

			setAllegatoDetermina(pratica, idTipoAllegatoDetermina, comunicazioneMailInsertConcessionario);

			utentiConcessionari.stream().forEach(u -> toConcessionario.add(u.getEmail()));

			if (!toConcessionario.isEmpty()) {
				comunicazioneMailInsertConcessionario.setDestinatari(String.join(",", toConcessionario));
				comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertConcessionario);
			}
		}
	}

	/**
	 * Costruisce le mail di notifica determina rettifica pratica e le invia al
	 * cittadino, agli utenti coinvolti e ai gruppi contrassegnati
	 *
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 */
	private void sendEmailNotificaDeterminaRettificaPratica(List<Utente> destinatari, Utente utente, Pratica pratica, String codiceDetermina,
															String codiceProtocolloPratica, String codiceProtocolloOperazione, LocalDate dataEmissioneRettifica) {

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMailInsertCittadino = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsertCittadino.setPratica(pratica);
		comunicazioneMailInsertCittadino
				.setOggetto(buildOggettoEmail(pratica,"Comunicazione pratica rettificata"));
		comunicazioneMailInsertCittadino.setTesto(EmailUtils.getContentEmail(
				EmailUtils.getMessageRettificaToCittadino(pratica, codiceProtocolloPratica, codiceProtocolloOperazione),
				"Comunicazione pratica rettificata"));

		String emailCittadino = pratica.getFirmatario().getEmail();

		if (emailCittadino != null) {
			comunicazioneMailInsertCittadino.setDestinatari(emailCittadino);

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMailInsertCittadino.setDestinatariCc(pratica.getDestinatario().getEmail());
			}
			// setto il flag che indica che e' una PEC
			comunicazioneMailInsertCittadino.setFlagPec(true);

			setAllegatoDetermina(pratica, Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RETTIFICA, comunicazioneMailInsertCittadino);

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertCittadino);
		}

		// costruisco comunicazione email per gli attori coinvolti

		List<Utente> utentiConcessionari = destinatari.stream()
				.filter(u -> u.getGruppo().getId().equals(Constants.ID_GRUPPO_CONCESSIONARIO))
				.collect(Collectors.toList());
		destinatari.removeAll(utentiConcessionari);

		List<String> toAttori = new ArrayList<>();
		destinatari.stream().forEach(u -> toAttori.add(u.getEmail()));

		ComunicazioneMailInsertDTO comunicazioneMailInsertAttori = new ComunicazioneMailInsertDTO(
				comunicazioneMailInsertCittadino);

		// setto il flag che indica se e' una PEC
		comunicazioneMailInsertAttori.setFlagPec(false);

		comunicazioneMailInsertAttori
				.setTesto(EmailUtils.getContentEmail(
						EmailUtils.getMessageRettificaToAttori(pratica, codiceProtocolloPratica,
								codiceProtocolloOperazione, codiceDetermina, dataEmissioneRettifica),
						"Comunicazione pratica rettificata"));

		if (!toAttori.isEmpty()) {
			comunicazioneMailInsertAttori.setDestinatari(String.join(",", toAttori));
			comunicazioneMailInsertAttori.setDestinatariCc(utente.getEmail());
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertAttori);
		}

		if (!utentiConcessionari.isEmpty()) {

			// costruisco comunicazione email PEC per il concessionario
			ComunicazioneMailInsertDTO comunicazioneMailInsertConcessionario = new ComunicazioneMailInsertDTO(
					comunicazioneMailInsertCittadino);

			List<String> toConcessionario = new ArrayList<>();

			comunicazioneMailInsertConcessionario.setTesto(EmailUtils.getContentEmail(
					EmailUtils.getMessageRettificaToAttori(pratica, codiceProtocolloPratica,
							codiceProtocolloOperazione, codiceDetermina, dataEmissioneRettifica),
					"Comunicazione pratica rettificata"));

			// setto il flag che indica che e' una PEC
			comunicazioneMailInsertConcessionario.setFlagPec(true);

			setAllegatoDetermina(pratica, Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RETTIFICA, comunicazioneMailInsertConcessionario);

			utentiConcessionari.stream().forEach(u -> toConcessionario.add(u.getEmail()));

			if (!toConcessionario.isEmpty()) {
				comunicazioneMailInsertConcessionario.setDestinatari(String.join(",", toConcessionario));
				comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertConcessionario);
			}
		}
	}

	/**
	 * Costruisce le mail di notifica ritiro atto concessorio e le invia al
	 * cittadino e al direttore di municipio
	 *
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 */
	private void sendEmailNotificaRitiroAttoConcessorio(List<Utente> destinatari, Utente utente, Pratica pratica,
														String codiceProtocolloPratica, String codiceProtocolloOperazione) {

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMailInsertCittadino = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsertCittadino.setPratica(pratica);
		comunicazioneMailInsertCittadino
				.setOggetto(buildOggettoEmail(pratica,"Comunicazione atto concessorio rilasciato"));
		comunicazioneMailInsertCittadino
				.setTesto(EmailUtils.getContentEmail(EmailUtils.getMessageRitiroAttoConcessorioToCittadino(pratica,
						codiceProtocolloPratica, codiceProtocolloOperazione), "Comunicazione atto concessorio rilasciato"));

		String emailCittadino = pratica.getFirmatario().getEmail();

		if (emailCittadino != null) {
			// setto il flag che indica che e' una PEC
			comunicazioneMailInsertCittadino.setFlagPec(true);

			comunicazioneMailInsertCittadino.setDestinatari(emailCittadino);

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMailInsertCittadino.setDestinatariCc(pratica.getDestinatario().getEmail());
			}

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertCittadino);
		}

		// costruisco comunicazione email per il municipio
		ComunicazioneMailInsertDTO comunicazioneMailInsertMunicipio = new ComunicazioneMailInsertDTO(
				comunicazioneMailInsertCittadino);

		// setto il flag che indica che e' una PEC
		comunicazioneMailInsertMunicipio.setFlagPec(false);

		List<String> toMunicipio = new ArrayList<>();

		comunicazioneMailInsertMunicipio
				.setTesto(EmailUtils.getContentEmail(EmailUtils.getMessageRitiroAttoConcessorioToAttori(pratica,
						codiceProtocolloPratica, codiceProtocolloOperazione), "Comunicazione atto concessorio rilasciato"));

		destinatari.stream().forEach(u -> toMunicipio.add(u.getEmail()));

		if (!toMunicipio.isEmpty()) {
			comunicazioneMailInsertMunicipio.setDestinatari(String.join(",", toMunicipio));
			comunicazioneMailInsertMunicipio.setDestinatariCc(utente.getEmail());
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertMunicipio);
		}
	}

	/**
	 * Costruisce le mail di notifica rigeto pratica e le invia al cittadino e agli
	 * utenti coinvolti
	 *
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 */
	private void sendEmailNotificaRigettoPratica(List<Utente> destinatari, Utente utente, Pratica pratica,
												 String codiceProtocolloPratica, String codiceProtocolloOperazione) {

		// costruisco comunicazione email per il cittadino
		ComunicazioneMailInsertDTO comunicazioneMailInsertCittadino = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsertCittadino.setPratica(pratica);
		comunicazioneMailInsertCittadino
				.setOggetto(buildOggettoEmail(pratica,"Comunicazione pratica rigettata"));
		comunicazioneMailInsertCittadino.setTesto(EmailUtils.getContentEmail(
				EmailUtils.getMessageRigettoToCittadino(pratica, codiceProtocolloPratica, codiceProtocolloOperazione),
				"Comunicazione pratica rigettata"));

		String emailCittadino = pratica.getFirmatario().getEmail();

		if (emailCittadino != null) {
			comunicazioneMailInsertCittadino.setDestinatari(emailCittadino);

			if (pratica.getDestinatario() != null && pratica.getDestinatario().getEmail() != null) {
				comunicazioneMailInsertCittadino.setDestinatariCc(pratica.getDestinatario().getEmail());
			}
			// setto il flag che indica che e' una PEC
			comunicazioneMailInsertCittadino.setFlagPec(true);

			setAllegatoDetermina(pratica, Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RIGETTO, comunicazioneMailInsertCittadino);

			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertCittadino);
		}

		// costruisco comunicazione email per gli attori coinvolti
		ComunicazioneMailInsertDTO comunicazioneMailInsertAttori = new ComunicazioneMailInsertDTO(
				comunicazioneMailInsertCittadino);

		// setto il flag che indica che e' una PEC
		comunicazioneMailInsertAttori.setFlagPec(false);

		List<String> toAttori = new ArrayList<>();

		comunicazioneMailInsertAttori.setTesto(EmailUtils.getContentEmail(
				EmailUtils.getMessageRigettoToAttori(pratica, codiceProtocolloPratica, codiceProtocolloOperazione),
				"Comunicazione pratica rigettata"));

		destinatari.stream().forEach(u -> toAttori.add(u.getEmail()));

		if (!toAttori.isEmpty()) {
			comunicazioneMailInsertAttori.setDestinatari(String.join(",", toAttori));
			comunicazioneMailInsertAttori.setDestinatariCc(utente.getEmail());
			comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsertAttori);
		}
	}

//	/**
//	 * Controlla se tutti i filtri di ricerca sulla pratica sono blank
//	 *
//	 * @param idsMunicipi
//	 * @param idStatoPratica
//	 * @param numProtocollo
//	 * @param numProvvedimento
//	 * @return boolean
//	 */
//	private boolean checkIfBlankAllSearchFiltersPratica(List<Integer> idsMunicipi, List<Integer> idsStatiPratica,
//			String numProtocollo, String numProvvedimento) {
//		return CollectionUtils.isEmpty(idsMunicipi) && CollectionUtils.isEmpty(idsStatiPratica)
//				&& StringUtils.isBlank(numProtocollo) && StringUtils.isBlank(numProvvedimento);
//	}

	/**
	 * Controlla se tutti i filtri di ricerca sui richiedenti della pratica sono
	 * blank
	 *
	 * @param nome
	 * @param cognome
	 * @param denominazioneRagSoc
	 * @param codFiscalePIva
	 * @return boolean
	 */
	private boolean checkIfBlankAllSearchFiltersRichiedenti(String nome, String cognome, String denominazioneRagSoc,
															String codFiscalePIva) {
		return StringUtils.isBlank(nome) && StringUtils.isBlank(cognome) && StringUtils.isBlank(denominazioneRagSoc)
				&& StringUtils.isBlank(codFiscalePIva);
	}

	/**
	 * Ritorna gli utenti destinatari per l'invio delle mail e per la
	 * protocollazione per l'operazione di presa in carico
	 *
	 * @param pratica
	 * @return la lista degli utenti destinatari
	 */
	private List<Utente> getDestinatariPresaInCarico(Pratica pratica) {

		List<Utente> result = new ArrayList<>();

		// se è presente l'utente assegnatario, vuol dire che la pratica e' stata
		// assegnata dal direttore ed invio quindi la email all'istruttore del
		// municipio,
		// altrimenti la invio al direttore, in quanto la pratica e' stata presa in
		// carico direttamente da un istruttore
		if (pratica.getUtenteAssegnatario() != null) {
			result.add(pratica.getUtentePresaInCarico());
		} else {
			// ricerco utenti direttori di municipio
			Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
					.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
			List<Gruppo> listaGruppi = new ArrayList<>();
			listaGruppi.add(gruppoDirettoreMunicipio);

			List<Municipio> listaMunicipi = new ArrayList<>();
			listaMunicipi.add(pratica.getMunicipio());

			result.addAll(utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi));
		}

		return result;
	}

	/**
	 * Ritorna gli utenti destinatari per l'invio delle mail per il pronto rilascio
	 *
	 * @param pratica
	 * @return la lista degli utenti destinatari
	 */
	private List<Utente> getDestinatariProntoRilascio(Pratica pratica) {
		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDirettoreMunicipio);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		return utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);
	}
	/**
	 * Ritorna gli utenti destinatari per l'invio delle mail e per la
	 * protocollazione per l'operazione di inserimento determina
	 *
	 * @param pratica
	 * @return la lista degli utenti destinatari
	 */
	private List<Utente> getDestinatariDetermina(Pratica pratica) {

		Set<Utente> result = new HashSet<>();

		// ricerco utenti direttori di municipio
		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
		// ricerco utenti concessionari
		Gruppo gruppoConcessionari = gruppoRepository.findById(Constants.ID_GRUPPO_CONCESSIONARIO)
				.orElseThrow(() -> new RuntimeException("Gruppo Concessionario non trovato"));

		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDirettoreMunicipio);
		listaGruppi.add(gruppoConcessionari);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		result.addAll(utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi));

		// aggiungo le mail degli utenti coinvolti nelle richieste pareri
		pratica.getRichiestePareri().stream().forEach(
			r -> {
				result.add(r.getUtenteRichiedente());
				if (r.isFlagInseritaRisposta() && r.getParere() != null) {
					result.add(r.getParere().getUtenteParere());
				}
			}
		);

		// ricerco utenti i cui gruppi sono impostati per l'invio della mail di notifica
		// determina
		List<Gruppo> listaGruppiDestinatari = gruppoRepository.findByFlagGestInvioMailDeterminaTrue();
		List<Utente> listaDestinatariDetermina = utenteRepository
				.findDistinctByGruppoInAndEnabledTrueAndFlagEliminatoFalse(listaGruppiDestinatari);

		// filtro escludendo gli utenti che fanno parte di altri municipi
		result.addAll(listaDestinatariDetermina.stream()
				.filter(u -> u.getMunicipi().isEmpty() || u.getMunicipi().contains(pratica.getMunicipio()))
				.collect(Collectors.toList()));

		return new ArrayList<>(result);
	}
	private List<Utente> getDestinatariDeterminaConPolizia(Pratica pratica) {

		Set<Utente> result = new HashSet<>();

		// ricerco utenti direttori di municipio
		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
		// ricerco utenti concessionari
		Gruppo gruppoConcessionari = gruppoRepository.findById(Constants.ID_GRUPPO_CONCESSIONARIO)
				.orElseThrow(() -> new RuntimeException("Gruppo Polizia non trovato"));
		Gruppo gruppoPolizia = gruppoRepository.findById(Constants.ID_GRUPPO_POLIZIA_LOCALE)
				.orElseThrow(() -> new RuntimeException("Gruppo Concessionario non trovato"));

		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDirettoreMunicipio);
		listaGruppi.add(gruppoConcessionari);
		listaGruppi.add(gruppoPolizia);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		result.addAll(utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi));

		// aggiungo le mail degli utenti coinvolti nelle richieste pareri
		pratica.getRichiestePareri().stream().forEach(r -> result.add(r.getUtenteRichiedente()));

		// ricerco utenti i cui gruppi sono impostati per l'invio della mail di notifica
		// determina
		List<Gruppo> listaGruppiDestinatari = gruppoRepository.findByFlagGestInvioMailDeterminaTrue();
		List<Utente> listaDestinatariDetermina = utenteRepository
				.findDistinctByGruppoInAndEnabledTrueAndFlagEliminatoFalse(listaGruppiDestinatari);

		// filtro escludendo gli utenti che fanno parte di altri municipi
		result.addAll(listaDestinatariDetermina.stream()
				.filter(u -> u.getMunicipi().isEmpty() || u.getMunicipi().contains(pratica.getMunicipio()))
				.collect(Collectors.toList()));

		return new ArrayList<>(result);
	}
	private List<Utente> getDestinatariDeterminaConPoliziaERagioneria(Pratica pratica) {

		Set<Utente> result = new HashSet<>();

		// ricerco utenti direttori di municipio
		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
		// ricerco utenti concessionari
		Gruppo gruppoConcessionari = gruppoRepository.findById(Constants.ID_GRUPPO_CONCESSIONARIO)
				.orElseThrow(() -> new RuntimeException("Gruppo Concessionario non trovato"));
		Gruppo gruppoPolizia = gruppoRepository.findById(Constants.ID_GRUPPO_POLIZIA_LOCALE)
				.orElseThrow(() -> new RuntimeException("Gruppo Polizia non trovato"));
		Gruppo gruppoRagioneria = gruppoRepository.findById(Constants.ID_GRUPPO_RIP_RAGIONERIA)
				.orElseThrow(() -> new RuntimeException("Gruppo Ragioneria non trovato"));

		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDirettoreMunicipio);
		listaGruppi.add(gruppoConcessionari);
		listaGruppi.add(gruppoPolizia);
		listaGruppi.add(gruppoRagioneria);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		result.addAll(utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi));

		// aggiungo le mail degli utenti coinvolti nelle richieste pareri
		pratica.getRichiestePareri().stream().forEach(r -> result.add(r.getUtenteRichiedente()));

		// ricerco utenti i cui gruppi sono impostati per l'invio della mail di notifica
		// determina
		List<Gruppo> listaGruppiDestinatari = gruppoRepository.findByFlagGestInvioMailDeterminaTrue();
		List<Utente> listaDestinatariDetermina = utenteRepository
				.findDistinctByGruppoInAndEnabledTrueAndFlagEliminatoFalse(listaGruppiDestinatari);

		// filtro escludendo gli utenti che fanno parte di altri municipi
		result.addAll(listaDestinatariDetermina.stream()
				.filter(u -> u.getMunicipi().isEmpty() || u.getMunicipi().contains(pratica.getMunicipio()))
				.collect(Collectors.toList()));

		return new ArrayList<>(result);
	}

	/**
	 * Ritorna gli utenti destinatari per l'invio delle mail e per la
	 * protocollazione per l'operazione di ritiro atto concessorio
	 *
	 * @param pratica
	 * @return la lista degli utenti destinatari
	 */
	private List<Utente> getDestinatariRitiroAttoConcessorio(Pratica pratica) {

		// ricerco utenti direttori di municipio
		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDirettoreMunicipio);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		return utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi);

	}

	/**
	 * Ritorna gli utenti destinatari per l'invio delle mail e per la
	 * protocollazione per l'operazione di determina rigetto
	 *
	 * @param pratica
	 * @return List<Utente>
	 */
	private List<Utente> getDestinatariDeterminaRigetto(Pratica pratica) {

		List<Utente> result = new ArrayList<>();

		// ricerco utenti direttori di municipio
		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));

		// ricerco utenti concessionari
		// Gruppo gruppoConcessionari = gruppoRepository.findById(Constants.ID_GRUPPO_CONCESSIONARIO)
		// 		.orElseThrow(() -> new RuntimeException("Gruppo Concessionario non trovato"));

		List<Gruppo> listaGruppi = new ArrayList<>();
		listaGruppi.add(gruppoDirettoreMunicipio);
		// listaGruppi.add(gruppoConcessionari);

		List<Municipio> listaMunicipi = new ArrayList<>();
		listaMunicipi.add(pratica.getMunicipio());

		result.addAll(utenteRepository.findUtentiAttiviByGruppiAndMunicipi(listaGruppi, listaMunicipi));

		// aggiungo le mail degli utenti coinvolti nelle richieste pareri
		pratica.getRichiestePareri().stream().forEach(r -> result.add(r.getUtenteRichiedente()));

		return result;
	}

	/**
	 * Si occupa di costruire la request per l'inserimento della pratica per il
	 * processo di proroga da EGOV
	 *
	 * @param praticaRequest
	 * @return la request per l'inserimento della pratica
	 */
	private PraticaInsertEditRequest buildInsertRequestProrogaFromEgov(PraticaProrogaEgovInsertEditRequest praticaRequest) {

		if (StringUtils.isBlank(praticaRequest.getNomeCittadinoEgov())
				|| StringUtils.isBlank(praticaRequest.getCognomeCittadinoEgov())
				|| StringUtils.isBlank(praticaRequest.getCfCittadinoEgov())) {
			throw new BusinessException(ErrorCode.E30,
					"Il nome, il cognome e il codice fiscale del cittadino sono obbligatori");
		}

		// gestione recupero utente EGOV
		Gruppo gruppoEgov = gruppoRepository.findById(Constants.ID_GRUPPO_EGOV).orElseThrow(() -> new RuntimeException("Errore: ID gruppo egov non presente nel sistema"));
		List<Utente> utentiEgov = utenteRepository.findByGruppo(gruppoEgov);
		if (utentiEgov.isEmpty()) {
			throw new RuntimeException("Nessun utente EGOV impostato nel sistema");
		}

		Pratica praticaOriginaria = null ;
		if (praticaRequest.getIdProrogaPrecedente() != null) {
			praticaOriginaria = praticaRepository.findById(praticaRequest.getIdProrogaPrecedente())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID pratica originaria non presente nel sistema",
							praticaRequest.getIdPraticaOriginaria()));
		} else {
			praticaOriginaria = praticaRepository.findById(praticaRequest.getIdPraticaOriginaria())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID pratica originaria non presente nel sistema",
							praticaRequest.getIdPraticaOriginaria()));
		}

		PraticaInsertEditRequest result = new PraticaInsertEditRequest();

		result.setIdPraticaOriginaria(praticaOriginaria.getId());
		result.setIdUtente(utentiEgov.get(0).getId());
		result.setIdMunicipio(praticaRequest.getIdMunicipio());
		result.setFirmatario(praticaRequest.getFirmatario());

		result.setMarcaBolloPratica(praticaRequest.getMarcaBolloPratica());
		result.setOriginEgov(praticaRequest.isOriginEgov());

		if (praticaOriginaria.getDestinatario() != null) {
			RichiedenteDTO destinatario = richiedenteMapper.entityToDto(praticaOriginaria.getDestinatario());
			destinatario.setId(null);
			result.setDestinatario(destinatario);
		}

		// build dati richiesta
		DatiRichiestaDTO datiRichiesta = datiRichiestaMapper.entityToDto(praticaOriginaria.getDatiRichiesta());
		datiRichiesta.setId(null);

		/*this.checkDateProroga(
			praticaOriginaria.getDatiRichiesta().getDataScadenzaOccupazione(),
			praticaOriginaria.getDatiRichiesta().getOraScadenzaOccupazione(),
			praticaRequest.getDatiRichiesta().getDataInizioOccupazione(),
			praticaRequest.getDatiRichiesta().getOraInizioOccupazione()
		);*/

		datiRichiesta.setDataInizioOccupazione(praticaRequest.getDatiRichiesta().getDataInizioOccupazione());
		datiRichiesta.setOraInizioOccupazione(praticaOriginaria.getDatiRichiesta().getOraInizioOccupazione());
		datiRichiesta.setDataScadenzaOccupazione(praticaRequest.getDatiRichiesta().getDataScadenzaOccupazione());
		datiRichiesta.setOraScadenzaOccupazione(praticaRequest.getDatiRichiesta().getOraScadenzaOccupazione());

		datiRichiesta.setFlagAccettazioneRegSuoloPubblico(
				praticaRequest.getDatiRichiesta().getFlagAccettazioneRegSuoloPubblico());
		datiRichiesta.setFlagConoscenzaTassaOccupazione(
				praticaRequest.getDatiRichiesta().getFlagConoscenzaTassaOccupazione());
		datiRichiesta.setFlagNonModificheRispettoConcessione(
				praticaRequest.getDatiRichiesta().getFlagNonModificheRispettoConcessione());
		datiRichiesta
				.setFlagObbligoRiparazioneDanni(praticaRequest.getDatiRichiesta().getFlagObbligoRiparazioneDanni());
		datiRichiesta.setFlagRispettoDisposizioniRegolamento(
				praticaRequest.getDatiRichiesta().getFlagRispettoDisposizioniRegolamento());
		datiRichiesta.setFlagRispettoInteresseTerzi(praticaRequest.getDatiRichiesta().getFlagRispettoInteresseTerzi());

		datiRichiesta.setFlagEsenzioneMarcaDaBollo(praticaRequest.getDatiRichiesta().isFlagEsenzioneMarcaDaBollo());
		datiRichiesta.setMotivazioneEsenzioneMarcaBollo(praticaRequest.getDatiRichiesta().getMotivazioneEsenzioneMarcaBollo());

		result.setDatiRichiesta(datiRichiesta);

		result.setIdTipoProcesso(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA);

		result.setNomeCittadinoEgov(praticaRequest.getNomeCittadinoEgov());
		result.setCognomeCittadinoEgov(praticaRequest.getCognomeCittadinoEgov());
		result.setCfCittadinoEgov(praticaRequest.getCfCittadinoEgov());

		return result;
	}

	private void checkDateProroga(
		LocalDate dataScadenzaOccupazioneOriginariaParam,
		LocalTime oraScadenzaOccupazioneOriginariaParam,
		LocalDate dataInizioOccupazioneRequestParam,
		LocalTime oraInizioOccupazioneRequestParam
	) throws BusinessException {
		LocalDate dataScadenzaOccupazioneOriginaria = dataScadenzaOccupazioneOriginariaParam;
		LocalTime oraScadenzaOccupazioneOriginaria = (oraScadenzaOccupazioneOriginariaParam == null) ?
				LocalTime.of(23, 59) : oraScadenzaOccupazioneOriginariaParam;

		LocalDate dataInizioOccupazioneRequest = dataInizioOccupazioneRequestParam;
		LocalTime oraInizioOccupazioneRequest = (oraInizioOccupazioneRequestParam == null) ?
				LocalTime.of(0, 0) : oraInizioOccupazioneRequestParam;


		LocalDateTime dataFineConcessioneCompletaOriginaria =
				LocalDateTime.of(
						dataScadenzaOccupazioneOriginaria,
						oraScadenzaOccupazioneOriginaria
				);
		LocalDateTime dataInizioConcessioneCompletaRequest =
				LocalDateTime.of(
						dataInizioOccupazioneRequest,
						oraInizioOccupazioneRequest
				);

		if (!dataInizioConcessioneCompletaRequest.minus(1, ChronoUnit.MINUTES).equals(dataFineConcessioneCompletaOriginaria)) {
			throw new BusinessException(ErrorCode.E35, ErrorCode.E35.getMessage());
		}
	}
	/**
	 * Si occupa di costruire la request per l'inserimento della pratica per il
	 * processo di rinuncia da EGOV
	 *
	 * @param praticaRequest
	 * @return la request per l'inserimento della pratica
	 */
	private PraticaInsertEditRequest buildInsertRequestRinunciaFromEgov(
			PraticaRinunciaEgovInsertEditRequest praticaRequest) {

		if (StringUtils.isBlank(praticaRequest.getNomeCittadinoEgov())
				|| StringUtils.isBlank(praticaRequest.getCognomeCittadinoEgov())
				|| StringUtils.isBlank(praticaRequest.getCfCittadinoEgov())) {
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

		Pratica praticaOriginaria = null ;
		if (praticaRequest.getIdProrogaPrecedente() != null) {
			praticaOriginaria = praticaRepository.findById(praticaRequest.getIdProrogaPrecedente())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID pratica originaria non presente nel sistema",
							praticaRequest.getIdPraticaOriginaria()));
		} else {
			praticaOriginaria = praticaRepository.findById(praticaRequest.getIdPraticaOriginaria())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID pratica originaria non presente nel sistema",
							praticaRequest.getIdPraticaOriginaria()));
		}

		PraticaInsertEditRequest result = new PraticaInsertEditRequest();

		result.setIdPraticaOriginaria(praticaOriginaria.getId());
		result.setIdUtente(utentiEgov.get(0).getId());
		result.setIdMunicipio(praticaOriginaria.getMunicipio().getId());
		result.setMotivazioneRichiesta(praticaRequest.getMotivazioneRichiesta());

		RichiedenteDTO firmatario = richiedenteMapper.entityToDto(praticaOriginaria.getFirmatario());
		firmatario.setId(null);
		result.setFirmatario(firmatario);

		if (praticaOriginaria.getDestinatario() != null) {
			RichiedenteDTO destinatario = richiedenteMapper.entityToDto(praticaOriginaria.getDestinatario());
			destinatario.setId(null);
			result.setDestinatario(destinatario);
		}

		// build dati richiesta
		DatiRichiestaDTO datiRichiesta = datiRichiestaMapper.entityToDto(praticaOriginaria.getDatiRichiesta());
		datiRichiesta.setId(null);

		datiRichiesta.setFlagAccettazioneRegSuoloPubblico(
				praticaRequest.getDatiRichiesta().getFlagAccettazioneRegSuoloPubblico());
		datiRichiesta.setFlagConoscenzaTassaOccupazione(
				praticaRequest.getDatiRichiesta().getFlagConoscenzaTassaOccupazione());
		datiRichiesta
				.setFlagObbligoRiparazioneDanni(praticaRequest.getDatiRichiesta().getFlagObbligoRiparazioneDanni());
		datiRichiesta.setFlagRispettoDisposizioniRegolamento(
				praticaRequest.getDatiRichiesta().getFlagRispettoDisposizioniRegolamento());
		datiRichiesta.setFlagRispettoInteresseTerzi(praticaRequest.getDatiRichiesta().getFlagRispettoInteresseTerzi());
		result.setDatiRichiesta(datiRichiesta);

		result.setIdTipoProcesso(Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA);

		result.setNomeCittadinoEgov(praticaRequest.getNomeCittadinoEgov());
		result.setCognomeCittadinoEgov(praticaRequest.getCognomeCittadinoEgov());
		result.setCfCittadinoEgov(praticaRequest.getCfCittadinoEgov());

		result.setOriginEgov(praticaRequest.isOriginEgov());

		return result;
	}

	/**
	 * Verifica se e' possibile aprire una pratica di rinuncia per una determinata
	 * pratica
	 *
	 * @param idPraticaOriginaria
	 */
	private void rinunciaVerificheApertura(Long idPraticaOriginaria) {
		Pratica praticaOriginaria = this.praticaRepository.findById(idPraticaOriginaria).orElseThrow(
			() -> new BusinessException(ErrorCode.E1,
				"Errore: ID pratica originaria non presente nel sistema",
				idPraticaOriginaria)
		);
		// controllo se la pratica sia nello stato corretto per chiedere una rinuncia
		if (!praticaOriginaria.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_CONCESSIONE_VALIDA)) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: la pratica di riferimento non è nello stato 'Concessione Valida', pertanto non può essere richiesta una rinuncia");
		}


		if (!this.checkRinunciaAttiva(praticaOriginaria)) {
			throw new BusinessException(ErrorCode.E7, "Errore: esiste già una pratica di rinuncia avviata");
		}

		if (!this.checkProrogaAttiva(praticaOriginaria)) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: non è possibile inserire una rinuncia in quanto esiste già una pratica di proroga avviata");
		}
	}

	/**
	 * Verifica se e' possibile effettuare un processo di rinuncia, decadenza o
	 * annullamento per una determinata pratica
	 *
	 * @param pratica
	 */
	private void rdaVerificheInserimento(Pratica pratica) {
		// controllo se la pratica sia nello stato corretto per chiedere una rinuncia,
		// decadenza o annullamento
		if (!pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_CONCESSIONE_VALIDA)) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: la pratica non è nello stato 'Concessione Valida', pertanto non può essere effettuato un processo di rinuncia, decadenza o annullamento");
		}

		if (!this.checkRinunciaAttiva(pratica)) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: non può essere effettuato un processo di revoca, decadenza o annullamento in quanto esiste già una pratica di rinuncia avviata");
		}

		if (!this.checkProrogaAttiva(pratica)) {
			throw new BusinessException(ErrorCode.E7,
				"Errore: non può essere effettuato un processo di revoca, decadenza o annullamento in quanto esiste già una pratica di proroga avviata");
		}
	}

	public Pratica switchToStatoTerminata(Long idPratica) {
		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		Utente user = pratica.getUtentePresaInCarico();
		StatoPratica statoPraticaTerminata = statoPraticaRepository
				.findById(Constants.ID_STATO_PRATICA_TERMINATA)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica TERMINATA non trovato"));

		pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_TERMINATA));

		pratica.setStatoPratica(statoPraticaTerminata);
		this.richiestaParereService.insertRichiestaVerificaRipristinoLuoghi(pratica, user);
		return pratica;
	}
}
