package it.fincons.osp.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import it.fincons.osp.model.*;
import it.fincons.osp.repository.*;
import it.fincons.osp.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.NotificaScadenzarioDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.NotificaScadenzarioMapper;
import it.fincons.osp.payload.protocollazione.response.ProtocolloResponse;
import it.fincons.osp.utils.Constants;
import it.fincons.osp.utils.Utils;

@Service
public class NotificaScadenzarioServiceImpl implements NotificaScadenzarioService {
	@Value("${osp.app.protocollo.callNewEndpoint}")
	private boolean callNewEndpoint;

	@Value("${osp.app.scadenzario.termineProcedimentale.preavviso.giorni}")
	private Integer numGiorniPreavvisoTermineProcedimentale;

	@Value("${osp.app.scadenzario.concessioneTemporanea.scadenza.pravviso.giorni}")
	private Integer numGiorniPreavvisoScadenzaOccupazioneTemporanea;

	@Value("${osp.app.scadenzario.preavvisoDiniego.giorni}")
	private Integer numGiorniScadenzaPreavvisoDiniego;

	@Value("${osp.app.scadenzario.alertRettificaDate.giorni}")
	private Integer numGiorniScadenzaRettificaDate;

	@Autowired
	NotificaScadenzarioRepository notificaScadenzarioRepository;

	@Autowired
	TipoNotificaScadenzarioRepository tipoNotificaScadenzarioRepository;

	@Autowired
	PraticaRepository praticaRepository;

	@Autowired
	MunicipioRepository municipioRepository;

	@Autowired
	UtenteRepository utenteRepository;

	@Autowired
	GruppoRepository gruppoRepository;

	@Autowired
	StatoPraticaRepository statoPraticaRepository;

	@Autowired
	AllegatoRepository allegatoRepository;

	@Autowired
	NotificaScadenzarioMapper notificaScadenzarioMapper;

	@Autowired
	RichiestaIntegrazioneService richiestaIntegrazioneService;
	
	@Autowired
	RichiestaIntegrazioneRepository richiestaIntegrazioneRepository;

	@Autowired
	ProtocollazioneService protocollazioneService;

	@Autowired
	ProtocolloService protocolloService;
	@Autowired
	private PraticaService praticaService;

	@LogEntryExit
	@Transactional
	@Override
	public void inserimentoNotifiche() {

		// elimino tutte le precedenti notifiche
		notificaScadenzarioRepository.deleteAll();

		Map<Integer, List<Utente>> mapUtentiMunicipi = this.populateMapUtentiMunicipi();

		// inserimento notifiche delle diverse tipolgie
		this.inserimentoNotificheTempiProcedimentaliScaduti(mapUtentiMunicipi);
		this.inserimentoNotificheScadenzaTempisticheIntegrazione(mapUtentiMunicipi);
		this.inserimentoNotificheScadenzaRettificaDate(mapUtentiMunicipi);
		this.inserimentoNotificheScadenzaTempistichePagamento(mapUtentiMunicipi);
		this.inserimentoNotificheTempiProcedimentaliInScadenza(mapUtentiMunicipi);
		this.inserimentoNotificheConcessioneTemporaneaInScadenza(mapUtentiMunicipi);
		this.inserimentoNotificheConcessioneTemporaneaScaduta(mapUtentiMunicipi);

		// verifico pratica con il termine del preavviso di diniego scaduto
		// in questo caso effettuo solo il passaggio di stato senza mandare notifiche
		this.verificaScadenzaPreavvisoDiniego();
		this.verificaScadenzaRettificaDate();
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<NotificaScadenzarioDTO> getNotificheScadenzario(Long idUtente) {
		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		List<NotificaScadenzario> result = notificaScadenzarioRepository.findByUtenteAndPratica_UtentePresaInCaricoNotNull(utente);

		return result.stream().map(notificaScadenzarioMapper::entityToDto).collect(Collectors.toList());
	}

	@Override
	@LogEntryExit(showArgs = true)
	public long countNotificheScadenzario(Long idUtente) {
		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		return notificaScadenzarioRepository.countByUtenteAndPratica_UtentePresaInCaricoNotNull(utente);
	}

	/**
	 * Effettua l'inserimento delle notifiche per le pratiche che hanno superato il
	 * tempo massimo previsto per la conclusione
	 * 
	 * @param mapUtentiMunicipi
	 */
	@LogEntryExit
	private void inserimentoNotificheTempiProcedimentaliScaduti(Map<Integer, List<Utente>> mapUtentiMunicipi) {

		List<Pratica> listaPratiche = praticaRepository
				.findByStatoPraticaIdInAndCodiceDeterminaIsNull(Utils.getIdStatiPraticaControlloTempiProcedimentali());

		for (Pratica pratica : listaPratiche) {
			if (LocalDate.now().isAfter(pratica.getDataScadenzaPratica().toLocalDate())) {
				for (Utente utente : mapUtentiMunicipi.get(pratica.getMunicipio().getId())) {
					this.insertNotifica(pratica, utente,
							Constants.ID_TIPO_NOTIFICA_SCADENZARIO_TEMPI_PROCEDIMENTALI_SCADUTI);
				}
			}
		}
	}

	/**
	 * Effettua l'inserimento delle notifiche per le pratiche che sono nello stato
	 * "Necessaria integrazione" e non hanno ricevuto risposta alla richiesta entro
	 * il tempo massimo previsto per l'integrazione
	 * 
	 * @param mapUtentiMunicipi
	 */
	@LogEntryExit
	private void inserimentoNotificheScadenzaTempisticheIntegrazione(Map<Integer, List<Utente>> mapUtentiMunicipi) {
		List<Pratica> listaPratiche = praticaRepository
				.findByStatoPraticaIdIn(List.of(Constants.ID_STATO_PRATICA_NECESSARIA_INTEGRAZIONE));

		for (Pratica pratica : listaPratiche) {
			Optional<RichiestaIntegrazione> richiestaIntegrazioneOptional = pratica.getRichiesteIntegrazioni().stream()
					.filter(r -> !r.isFlagInseritaRisposta()).findFirst();
			if (richiestaIntegrazioneOptional.isPresent()) {
				RichiestaIntegrazione richiestaIntegrazione = richiestaIntegrazioneOptional.get();

				if (LocalDate.now().isAfter(richiestaIntegrazione.getDataScadenza().toLocalDate())) {
					for (Utente utente : mapUtentiMunicipi.get(pratica.getMunicipio().getId())) {
						this.insertNotifica(pratica, utente,
								Constants.ID_TIPO_NOTIFICA_SCADENZARIO_SCADENZA_TEMPISTICHE_INTEGRAZIONE);
					}
				}
			}
		}

	}
	/**
	 * Effettua l'inserimento delle notifiche per le pratiche che sono nello stato
	 * "Rettifica Date" e non hanno ricevuto risposta alla richiesta entro
	 * il tempo massimo previsto per l'integrazione (2 giorni prima)
	 *
	 * @param mapUtentiMunicipi
	 */
	@LogEntryExit
	private void inserimentoNotificheScadenzaRettificaDate(Map<Integer, List<Utente>> mapUtentiMunicipi) {
		List<Pratica> listaPratiche = praticaRepository
				.findByStatoPraticaIdIn(List.of(Constants.ID_STATO_PRATICA_RETTIFICA_DATE));

		for (Pratica pratica : listaPratiche) {
			Optional<RichiestaIntegrazione> richiestaIntegrazioneOptional = pratica.getRichiesteIntegrazioni().stream()
					.filter(r -> !r.isFlagInseritaRisposta()).findFirst();
			if (richiestaIntegrazioneOptional.isPresent()) {
				RichiestaIntegrazione richiestaIntegrazione = richiestaIntegrazioneOptional.get();

				if (LocalDate.now().isAfter(richiestaIntegrazione.getDataScadenza().toLocalDate().minusDays(this.numGiorniScadenzaRettificaDate))) {
					for (Utente utente : mapUtentiMunicipi.get(pratica.getMunicipio().getId())) {
						this.insertNotifica(pratica, utente,
								Constants.ID_TIPO_NOTIFICA_SCADENZARIO_SCADENZA_RETTIFICA_DATE);
					}
				}
			}
		}
	}

	/**
	 * Effettua l'inserimento delle notifiche per le pratiche che sono nello stato
	 * "Attesa di pagamento" che hanno superato il tempo massimo previsto per
	 * l'upload delle ricevute di pagamento
	 * 
	 * @param mapUtentiMunicipi
	 */
	@LogEntryExit
	private void inserimentoNotificheScadenzaTempistichePagamento(Map<Integer, List<Utente>> mapUtentiMunicipi) {
		List<Pratica> listaPratiche = praticaRepository
				.findByStatoPraticaIdIn(List.of(Constants.ID_STATO_PRATICA_ATTESA_DI_PAGAMENTO));

		for (Pratica pratica : listaPratiche) {
			if (LocalDate.now().isAfter(pratica.getDataScadenzaPagamento().toLocalDate())) {
				for (Utente utente : mapUtentiMunicipi.get(pratica.getMunicipio().getId())) {
					this.insertNotifica(pratica, utente,
							Constants.ID_TIPO_NOTIFICA_SCADENZARIO_SCADENZA_TEMPISTICHE_PAGAMENTO);
				}
			}
		}
	}

	/**
	 * Effettua l'inserimento delle notifiche per le pratiche che si stanno
	 * avvicinando al tempo massimo previsto per la conclusione
	 * 
	 * @param mapUtentiMunicipi
	 */
	@LogEntryExit
	private void inserimentoNotificheTempiProcedimentaliInScadenza(Map<Integer, List<Utente>> mapUtentiMunicipi) {

		List<Pratica> listaPratiche = praticaRepository
				.findByStatoPraticaIdInAndCodiceDeterminaIsNull(Utils.getIdStatiPraticaControlloTempiProcedimentali());

		for (Pratica pratica : listaPratiche) {
			if (LocalDate.now().isAfter(
					pratica.getDataScadenzaPratica().minusDays(numGiorniPreavvisoTermineProcedimentale).toLocalDate())
					&& !LocalDate.now().isAfter(pratica.getDataScadenzaPratica().toLocalDate())) {

				for (Utente utente : mapUtentiMunicipi.get(pratica.getMunicipio().getId())) {
					this.insertNotifica(pratica, utente,
							Constants.ID_TIPO_NOTIFICA_SCADENZARIO_TEMPI_PROCEDIMENTALI_IN_SCADENZA);
				}

				// recupero utenti dei gruppi ai quali e' stato richiesto un parere ed ancora
				// non e' stato inserito
				List<RichiestaParere> listaRichiestePareri = pratica.getRichiestePareri().stream()
						.filter(r -> !r.isFlagInseritaRisposta()).collect(Collectors.toList());

				if (!listaRichiestePareri.isEmpty()) {
					List<Gruppo> listaGruppiDestinatariRichiestePareriAperte = new ArrayList<>();
					listaRichiestePareri.forEach(
							r -> listaGruppiDestinatariRichiestePareriAperte.add(r.getGruppoDestinatarioParere()));
					List<Municipio> municipi = List.of(pratica.getMunicipio());

					Set<Utente> utentiDestinatariRichiestePareriAperte = new HashSet<>();
					utentiDestinatariRichiestePareriAperte.addAll(utenteRepository.findUtentiAttiviByGruppiAndMunicipi(
							listaGruppiDestinatariRichiestePareriAperte, municipi));

					// inserisco notifiche anche per questi utenti
					for (Utente utente : utentiDestinatariRichiestePareriAperte) {
						this.insertNotifica(pratica, utente,
								Constants.ID_TIPO_NOTIFICA_SCADENZARIO_TEMPI_PROCEDIMENTALI_IN_SCADENZA);
					}
				}
			}
		}
	}

	/**
	 * Effettua l'inserimento delle notifiche per le pratiche che si stanno
	 * avvicinando alla scadenza della concessione temporanea
	 * 
	 * @param mapUtentiMunicipi
	 */
	@LogEntryExit
	private void inserimentoNotificheConcessioneTemporaneaInScadenza(Map<Integer, List<Utente>> mapUtentiMunicipi) {

		List<Pratica> listaPratiche = praticaRepository
				.findByStatoPraticaIdIn(List.of(Constants.ID_STATO_PRATICA_CONCESSIONE_VALIDA));

		for (Pratica pratica : listaPratiche) {
			// eseguo il controllo sull'avvicinamento della scadenza solo se l'occupazione
			// non e' di un solo giorno
			if (pratica.getDatiRichiesta().getDataInizioOccupazione() != pratica.getDatiRichiesta()
					.getDataScadenzaOccupazione()
					&& LocalDate.now()
							.isAfter(pratica.getDatiRichiesta().getDataScadenzaOccupazione()
									.minusDays(numGiorniPreavvisoScadenzaOccupazioneTemporanea))
					&& !LocalDate.now().isAfter(pratica.getDatiRichiesta().getDataScadenzaOccupazione())) {
				for (Utente utente : mapUtentiMunicipi.get(pratica.getMunicipio().getId())) {
					this.insertNotifica(pratica, utente,
							Constants.ID_TIPO_NOTIFICA_SCADENZARIO_CONCESSIONE_TEMPORANEA_IN_SCADENZA);
				}
			}
		}
	}

	/**
	 * Effettua l'inserimento delle notifiche per le pratiche per le quali e'
	 * scaduta la concessione temporanea e non e' stata richiesta una proroga.
	 * Inoltre cambia lo stato della pratica in "Terminata" se non è stata richiesta
	 * proroga, altrimenti in "Archiviata"
	 * 
	 * @param mapUtentiMunicipi
	 */
	@LogEntryExit
	private void inserimentoNotificheConcessioneTemporaneaScaduta(Map<Integer, List<Utente>> mapUtentiMunicipi) {

		List<Pratica> listaPratiche = praticaRepository
				.findByStatoPraticaIdIn(List.of(Constants.ID_STATO_PRATICA_CONCESSIONE_VALIDA));

		for (Pratica pratica : listaPratiche) {
			if (LocalDate.now().isAfter(pratica.getDatiRichiesta().getDataScadenzaOccupazione())) {

				if (pratica.getTipoProcesso().getId().equals(Constants.ID_TIPO_PROCESSO_CONCESSIONE_TEMPORANEA)) {
					List<Pratica> proroghe = praticaRepository.findByIdPraticaOriginariaOrderByIdDesc(pratica.getId());

					if (proroghe.isEmpty()) {
						// inserisco notifiche
						for (Utente utente : mapUtentiMunicipi.get(pratica.getMunicipio().getId())) {
							this.insertNotifica(pratica, utente,
									Constants.ID_TIPO_NOTIFICA_SCADENZARIO_CONCESSIONE_SCADUTA);
						}

						pratica = this.praticaService.switchToStatoTerminata(pratica.getId());
						/*StatoPratica statoPraticaTerminata = statoPraticaRepository
								.findById(Constants.ID_STATO_PRATICA_TERMINATA)
								.orElseThrow(() -> new RuntimeException("Errore: stato pratica TERMINATA non trovato"));

						pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_TERMINATA));

						pratica.setStatoPratica(statoPraticaTerminata);*/
					} else {
						StatoPratica statoPraticaArchiviata = statoPraticaRepository
								.findById(Constants.ID_STATO_PRATICA_ARCHIVIATA).orElseThrow(
										() -> new RuntimeException("Errore: stato pratica ARCHIVIATA non trovato"));

						pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_ARCHIVIATA));

						pratica.setStatoPratica(statoPraticaArchiviata);
					}
				} else if (pratica.getTipoProcesso().getId()
						.equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)) {
					List<Pratica> proroghePrecedenti = this.praticaRepository.findPraticheByIdOrIdPraticaOriginariaAndStatoPraticaNotInAndTipoProcesso(
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
					if (!proroghePrecedenti.isEmpty()) {
						StatoPratica statoPraticaArchiviata = statoPraticaRepository
								.findById(Constants.ID_STATO_PRATICA_ARCHIVIATA).orElseThrow(
										() -> new RuntimeException("Errore: stato pratica ARCHIVIATA non trovato"));

						pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_ARCHIVIATA));

						pratica.setStatoPratica(statoPraticaArchiviata);
					} else {
						// inserisco notifiche
						for (Utente utente : mapUtentiMunicipi.get(pratica.getMunicipio().getId())) {
							this.insertNotifica(pratica, utente,
									Constants.ID_TIPO_NOTIFICA_SCADENZARIO_CONCESSIONE_SCADUTA);
						}

						StatoPratica statoPraticaTerminata = statoPraticaRepository
								.findById(Constants.ID_STATO_PRATICA_TERMINATA)
								.orElseThrow(() -> new RuntimeException("Errore: stato pratica TERMINATA non trovato"));

						pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_TERMINATA));

						pratica.setStatoPratica(statoPraticaTerminata);
					}
				}

				pratica.setDataModifica(LocalDateTime.now().withNano(0));
				praticaRepository.save(pratica);
			}
		}
	}

	/**
	 * Verifica se e' scaduto il termine per l'inserimento della risposta al
	 * preavviso diniego per tutte le pratiche che sono in quello stato. Se e'
	 * scaduto il termine effettua il passaggio di stato della pratica in "Pratica
	 * da rigettare".
	 */
	@LogEntryExit
	private void verificaScadenzaPreavvisoDiniego() {
		List<Pratica> listaPratiche = praticaRepository
				.findByStatoPraticaIdIn(List.of(Constants.ID_STATO_PRATICA_PREAVVISO_DINIEGO));

		for (Pratica pratica : listaPratiche) {
			if (LocalDateTime.now().withNano(0).isAfter(pratica.getDataScadenzaPreavvisoDiniego())) {

				StatoPratica statoPraticaDaRigettare = statoPraticaRepository
						.findById(Constants.ID_STATO_PRATICA_DA_RIGETTARE)
						.orElseThrow(() -> new RuntimeException("Errore: stato pratica DA RIGETTARE non trovato"));

				// aggiornamento pratica
				pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_DA_RIGETTARE));
				pratica.setStatoPratica(statoPraticaDaRigettare);
				pratica.setDataModifica(LocalDateTime.now().withNano(0));

				praticaRepository.save(pratica);

				// inserimento comunicazioni mail
				Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);
				richiestaIntegrazioneService.sendEmailNotificaPraticaDaRigettare(null, pratica,
						protocolloInserimento.getCodiceProtocollo());
			}
		}
	}
 
	/**
	 * Verifica se e' scaduto il termine per l'inserimento della risposta alla
	 * richiesta di rettifica date per tutte le pratiche che sono in quello stato.
	 * Se e' scaduto il termine effettua il passaggio di stato della pratica in
	 * "Preavviso diniego" e isnerisce le mail da inviare al cittadino e
	 * all'istruttore
	 */
	@LogEntryExit
	private void verificaScadenzaRettificaDate() {
		List<Pratica> listaPratiche = praticaRepository
				.findByStatoPraticaIdIn(List.of(Constants.ID_STATO_PRATICA_RETTIFICA_DATE));

		for (Pratica pratica : listaPratiche) {

			RichiestaIntegrazione richiestaIntegrazione = pratica.getRichiesteIntegrazioni().stream()
					.filter(r -> !r.isFlagInseritaRisposta()).findFirst()
					.orElseThrow(() -> new RuntimeException("Errore: nessuna richiesta rettifica date attiva"));

			if (LocalDateTime.now().withNano(0).isAfter(richiestaIntegrazione.getDataScadenza())) {

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
				} else {
					richiestaIntegrazione.setDataScadenza(
							richiestaIntegrazione.getDataInserimento().plusDays(numGiorniScadenzaPreavvisoDiniego));

					richiestaIntegrazioneRepository.save(richiestaIntegrazione);

					//recupero allegati pratica
					List<Allegato> allegati=allegatoRepository.findByPraticaAndStatoPratica(pratica.getId(), pratica.getStatoPratica().getId());

					StatoPratica statoPraticaPreavvisoDiniego = statoPraticaRepository
							.findById(Constants.ID_STATO_PRATICA_PREAVVISO_DINIEGO).orElseThrow(
									() -> new RuntimeException("Errore: stato pratica PREAVVISO DINIEGO non trovato"));

					pratica.setFlagProceduraDiniego(true);
					pratica.setInfoPassaggioStato(PassaggioStato.getDescStatoPratica(pratica.getStatoPratica().getId(), Constants.ID_STATO_PRATICA_PREAVVISO_DINIEGO));
					pratica.setStatoPratica(statoPraticaPreavvisoDiniego);
					pratica.setDataScadenzaPreavvisoDiniego(
							LocalDateTime.now().withNano(0).plusDays(numGiorniScadenzaPreavvisoDiniego));
					pratica.setDataModifica(LocalDateTime.now().withNano(0));

					praticaRepository.save(pratica);

					List<Utente> listaDestinatari = getDestinatariPreavvisoDiniego(pratica);

					ProtocolloResponse protocolloResponse = null;

					if(callNewEndpoint){
						protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(
								pratica,
								richiestaIntegrazione.getUtenteRichiedente(),
								Constants.TIPO_EVENTO_PROTOCOLLAZIONE_PREAVVISO_DINIEGO,
								listaDestinatari,
								true,
								Constants.TEMPLATE_NAME_PROTOCOLLO_PRATICA,
								allegati,
								PassaggioStato.RICHIESTA_PARERI_TO_PREAVVISO_DINIEGO,
								null);

					}else{
						protocolloResponse = protocollazioneService.getNumeroProtocolloUscita(pratica,
								richiestaIntegrazione.getUtenteRichiedente(),
								Constants.TIPO_EVENTO_PROTOCOLLAZIONE_PREAVVISO_DINIEGO, listaDestinatari, true);
					}

					// inserimento protocollo sulla pratica
					protocolloService.insertProtocollo(pratica, statoPraticaPreavvisoDiniego, protocolloResponse, TipoOperazioneProtocollo.PREAVVISO_DINIEGO_RICHIESTA_RETTIFICA_DATE);
					
					// inserimento comunicazioni mail
					Protocollo protocolloInserimento = protocolloService.getProtocolloInserimento(pratica);
					richiestaIntegrazioneService.sendEmailNotificaPreavvisoDiniego(listaDestinatari, richiestaIntegrazione.getUtenteRichiedente(), pratica,
							protocolloInserimento.getCodiceProtocollo(), protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno(),
							richiestaIntegrazione.getMotivoRichiesta());
				}

				praticaRepository.save(pratica);
			}
		}
	}

	/**
	 * Inserisce una notifica scadenzario
	 * 
	 * @param pratica
	 * @param utente
	 * @param idTipoNotificaScadenzario
	 */
	@LogEntryExit(showArgs = true)
	private void insertNotifica(Pratica pratica, Utente utente, Integer idTipoNotificaScadenzario) {

		TipoNotificaScadenzario tipoNotificaScadenzario = tipoNotificaScadenzarioRepository
				.findById(idTipoNotificaScadenzario)
				.orElseThrow(() -> new RuntimeException("Errore: nessun Tipo Notifica Scadenzario trovato per l'id ["
						+ idTipoNotificaScadenzario + "]"));

		NotificaScadenzario notificaScadenzario = new NotificaScadenzario();
		notificaScadenzario.setPratica(pratica);
		notificaScadenzario.setUtente(utente);
		notificaScadenzario.setTipoNotificaScadenzario(tipoNotificaScadenzario);
		notificaScadenzario.setDataNotifica(LocalDateTime.now().withNano(0));

		notificaScadenzarioRepository.save(notificaScadenzario);
	}

	/**
	 * Costruisce una mappa con la lista di utenti istruttori e direttori per ogni
	 * municipio
	 * 
	 * @return la mappa con chiave l'id del municipio e valore la lista di utenti
	 */
	private Map<Integer, List<Utente>> populateMapUtentiMunicipi() {
		Map<Integer, List<Utente>> result = new HashMap<>();

		// ricerco gruppi per i quali cercare gli utenti, ovvero istruttori e direttori
		// di municipio
		List<Gruppo> gruppi = new ArrayList<>();

		Gruppo gruppoIstruttoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_ISTRUTTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo istruttore municipio non trovato"));
		gruppi.add(gruppoIstruttoreMunicipio);

		Gruppo gruppoDirettoreMunicipio = gruppoRepository.findById(Constants.ID_GRUPPO_DIRETTORE_MUNICIPIO)
				.orElseThrow(() -> new RuntimeException("Gruppo direttore municipio non trovato"));
		gruppi.add(gruppoDirettoreMunicipio);

		for (Municipio municipio : municipioRepository.findAll()) {
			List<Municipio> municipi = List.of(municipio);
			result.put(municipio.getId(), utenteRepository.findUtentiAttiviByGruppiAndMunicipi(gruppi, municipi));
		}

		return result;
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
		pratica.getRichiestePareri().stream().forEach(r -> listaUtenti.add(r.getUtenteRichiedente()));

		return listaUtenti;
	}
}
