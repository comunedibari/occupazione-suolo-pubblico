package it.fincons.osp.services.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.ValidationException;

import it.fincons.osp.payload.request.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.AllegatoDTO;
import it.fincons.osp.dto.AllegatoSimplifiedDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.AllegatoMapper;
import it.fincons.osp.model.Allegato;
import it.fincons.osp.model.Gruppo;
import it.fincons.osp.model.Integrazione;
import it.fincons.osp.model.Parere;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.Protocollo;
import it.fincons.osp.model.RichiestaIntegrazione;
import it.fincons.osp.model.RichiestaParere;
import it.fincons.osp.model.StatoPratica;
import it.fincons.osp.model.TipoAllegato;
import it.fincons.osp.model.TipoAllegatoGruppoStatoProcesso;
import it.fincons.osp.model.TipoProcesso;
import it.fincons.osp.model.Utente;
import it.fincons.osp.repository.AllegatoRepository;
import it.fincons.osp.repository.ParereRepository;
import it.fincons.osp.repository.PraticaRepository;
import it.fincons.osp.repository.RichiestaIntegrazioneRepository;
import it.fincons.osp.repository.RichiestaParereRepository;
import it.fincons.osp.repository.StatoPraticaRepository;
import it.fincons.osp.repository.TipoAllegatoGruppoStatoProcessoRepository;
import it.fincons.osp.repository.TipoAllegatoRepository;
import it.fincons.osp.repository.TipoProcessoRepository;
import it.fincons.osp.repository.UtenteRepository;
import it.fincons.osp.services.AllegatoService;
import it.fincons.osp.services.ProtocolloService;
import it.fincons.osp.utils.Constants;
import it.fincons.osp.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AllegatoServiceImpl implements AllegatoService {

	@Autowired
	AllegatoRepository allegatoRepository;

	@Autowired
	TipoAllegatoRepository tipoAllegatoRepository;

	@Autowired
	TipoAllegatoGruppoStatoProcessoRepository tipoAllegatoGruppoStatoProcessoRepository;

	@Autowired
	PraticaRepository praticaRepository;

	@Autowired
	ParereRepository parereRepository;

	@Autowired
	RichiestaParereRepository richiestaParereRepository;

	@Autowired
	RichiestaIntegrazioneRepository richiestaIntegrazioneRepository;

	@Autowired
	StatoPraticaRepository statoPraticaRepository;

	@Autowired
	TipoProcessoRepository tipoProcessoRepository;

	@Autowired
	AllegatoMapper allegatoMapper;

	@Autowired
	ProtocolloService protocolloService;

	@Autowired
	UtenteRepository utenteRepository;

	@Override
	@LogEntryExit
	public AllegatoDTO insertAllegatoPratica(AllegatoPraticaInsertRequest allegatoPraticaInsertRequest) {

		Pratica pratica = praticaRepository.findById(allegatoPraticaInsertRequest.getIdPratica())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
						allegatoPraticaInsertRequest.getIdPratica()));

		return this.performInsertAllegato(allegatoPraticaInsertRequest.getAllegato(), pratica, null, null, null);
	}

	@Override
	@LogEntryExit
	public AllegatoDTO insertAllegatoParere(AllegatoParereInsertRequest allegatoParereInsertRequest,
			String usernameUtente) {

		RichiestaParere richiestaParere = richiestaParereRepository
				.findById(allegatoParereInsertRequest.getIdRichiestaParere())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID richiesta parere non presente nel sistema",
						allegatoParereInsertRequest.getIdRichiestaParere()));

		Utente utente = utenteRepository.findByUsernameAndFlagEliminatoFalse(usernameUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E3, "Errore: username non presente nel sistema", usernameUtente));

		return this.performInsertAllegato(allegatoParereInsertRequest.getAllegato(), null, richiestaParere.getId(),
				null, utente);
	}

	@Override
	@LogEntryExit
	public AllegatoDTO insertAllegatoIntegrazione(AllegatoIntegrazioneInsertRequest allegatoIntegrazioneInsertRequest) {

		RichiestaIntegrazione richiestaIntegrazione = richiestaIntegrazioneRepository
				.findById(allegatoIntegrazioneInsertRequest.getIdRichiestaIntegrazione())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID richiesta integrazione non presente nel sistema",
						allegatoIntegrazioneInsertRequest.getIdRichiestaIntegrazione()));

		return this.performInsertAllegato(allegatoIntegrazioneInsertRequest.getAllegato(),
				richiestaIntegrazione.getPratica(), null, richiestaIntegrazione.getId(), null);
	}

	@Override
	public AllegatoDTO insertAllegatoRichiestaIntegrazione(AllegatoRichiestaIntegrazioneRequest allegatoRichiestaIntegrazioneRequest) {
		return this.performInsertAllegato(allegatoRichiestaIntegrazioneRequest.getAllegato(), null, null, null, null);
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<AllegatoSimplifiedDTO> getAllegati(Long idPratica, Integer idStatoPratica, Integer idTipoProcesso) {

		if (idStatoPratica == null && idTipoProcesso == null) {
			throw new BusinessException(ErrorCode.E4, "Errore: inserire almeno uno fra Stato Pratica e Tipo Processo");
		}

		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		StatoPratica statoPratica = null;
		if (idStatoPratica != null) {
			statoPratica = statoPraticaRepository.findById(idStatoPratica)
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID stato pratica non presente nel sistema", idPratica));
		}

		TipoProcesso tipoProcesso = null;
		if (idTipoProcesso != null) {
			tipoProcesso = tipoProcessoRepository.findById(idTipoProcesso)
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID tipo processo non presente nel sistema", idPratica));
		}

		List<TipoAllegatoGruppoStatoProcesso> listaTipiAllegatiGruppoStatoProcesso;

		if (statoPratica != null && tipoProcesso != null) {
			listaTipiAllegatiGruppoStatoProcesso = tipoAllegatoGruppoStatoProcessoRepository
					.findByStatoPraticaAndTipoProcesso(statoPratica, tipoProcesso);
		} else if (statoPratica != null) {
			listaTipiAllegatiGruppoStatoProcesso = tipoAllegatoGruppoStatoProcessoRepository
					.findByStatoPratica(statoPratica);
		} else {
			listaTipiAllegatiGruppoStatoProcesso = tipoAllegatoGruppoStatoProcessoRepository
					.findByTipoProcesso(tipoProcesso);
		}

		List<Allegato> result = allegatoRepository
				.findByPraticaAndTipoAllegatoInOrderByDataInserimentoDescTipoAllegatoAscRevisioneDesc(pratica,
						listaTipiAllegatiGruppoStatoProcesso.stream()
								.map(TipoAllegatoGruppoStatoProcesso::getTipoAllegato).collect(Collectors.toList()));

		return result.stream().map(a -> allegatoMapper.entityToSimplifiedDto(a)).collect(Collectors.toList());
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<AllegatoSimplifiedDTO> getAllegatiRichiestaParere(Long idRichiestaParere) {

		RichiestaParere richiestaParere = richiestaParereRepository.findById(idRichiestaParere)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID richiesta parere non presente nel sistema", idRichiestaParere));

		return allegatoRepository.findByIdRichiestaParere(richiestaParere.getId()).stream()
				.map(a -> allegatoMapper.entityToSimplifiedDto(a)).collect(Collectors.toList());
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<AllegatoSimplifiedDTO> getAllegatiParere(Long idParere) {
		Parere parere = parereRepository.findById(idParere).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID parere non presente nel sistema", idParere));

		return allegatoRepository.findByParere(parere).stream().map(a -> allegatoMapper.entityToSimplifiedDto(a))
				.collect(Collectors.toList());
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<AllegatoSimplifiedDTO> getAllegatiRichiestaIntegrazione(Long idRichiestaIntegrazione) {
		RichiestaIntegrazione richiestaIntegrazione = richiestaIntegrazioneRepository.findById(idRichiestaIntegrazione)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID richiesta integrazione non presente nel sistema", idRichiestaIntegrazione));

		return allegatoRepository.findByIdRichiestaIntegrazione(richiestaIntegrazione.getId()).stream()
				.map(a -> allegatoMapper.entityToSimplifiedDto(a)).collect(Collectors.toList());
	}

	@Override
	@LogEntryExit(showArgs = true)
	public AllegatoDTO getAllegato(Long id) {
		Allegato allegato = allegatoRepository.findById(id).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: id allegato non presente nel sistema", id));

		return allegatoMapper.entityToDto(allegato);
	}

	@Override
	@LogEntryExit(showArgs = true)
	public void deleteAllegato(Long id) {
		Allegato allegato = allegatoRepository.findById(id).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: id allegato non presente nel sistema", id));

		allegatoRepository.delete(allegato);

		log.info("Eliminazione allegato avvenuta correttamente");
	}

	@Override
	@LogEntryExit
	public boolean checkAllegatiObbligatoriInserimento(Pratica pratica, Gruppo gruppo) {

		List<TipoAllegatoGruppoStatoProcesso> tipiAllegatiGruppoStatoProcessoObbligatori = tipoAllegatoGruppoStatoProcessoRepository
				.findByStatoPraticaAndTipoProcessoAndGruppoAndFlagObbligatorioTrue(pratica.getStatoPratica(),
						pratica.getTipoProcesso(), gruppo);

		List<TipoAllegato> tipiAllegatiObbligatori = tipiAllegatiGruppoStatoProcessoObbligatori.stream()
				.map(TipoAllegatoGruppoStatoProcesso::getTipoAllegato).collect(Collectors.toList());

		// se il firmatario e' anche il destinatario, allora rimuovo dalla lista degli
		// allegati obbligatori il documento del destinatario
		if (pratica.getFirmatario().getTipoRuoloRichiedente().getId()
				.equals(Constants.ID_TIP_RUOLO_RICHIEDENTE_DESTINATARIO)) {
			tipiAllegatiObbligatori.removeIf(t -> t.getId().equals(Constants.ID_TIPO_ALLEGATO_DOCUMENTO_DESTINATARIO));
		}

		//Per tenere traccia dell’avvenuto pagamento della marca da bollo anche lato OSP,
		//il json della pratica sarà aggiornato con i campi relativi al pagamento e non sarà richiesto il documento di attestazione del pagamento della marca da bollo.
		if(pratica.getMarcaBolloPratica()!=null||pratica.getDatiRichiesta().isFlagEsenzioneMarcaDaBollo()){
			tipiAllegatiObbligatori.removeIf(t -> t.getId().equals(Constants.ID_TIPO_ALLEGATO_ATTESTAZIONE_PAGAMENTO_MARCA_DA_BOLLO));
		}

		List<Allegato> listaAllegati = allegatoRepository.findByPraticaAndTipoAllegatoWithMaxRevisione(pratica,
				tipiAllegatiObbligatori);

		return tipiAllegatiObbligatori.size() <= listaAllegati.size();
	}

	@Override
	@LogEntryExit
	public boolean checkAllegatiObbligatoriAggiornamento(Pratica pratica, PraticaEgovEditRequest praticaInsertRequest) {

		if(
			(
				!praticaInsertRequest.getPratica().getDatiRichiesta().isFlagEsenzioneMarcaDaBollo() ||
				!pratica.isFlagEsenzionePagamentoCUP()
			)
			&&
			(
				praticaInsertRequest.getAllegati()==null ||
				praticaInsertRequest.getAllegati().size()==0
			)
		) {
			return false;
		}

		//Per tenere traccia dell’avvenuto pagamento della marca da bollo anche lato OSP,
		//il json della pratica sarà aggiornato con i campi relativi al pagamento e non sarà richiesto il documento di attestazione del pagamento della marca da bollo.
		if(!praticaInsertRequest.getPratica().getDatiRichiesta().isFlagEsenzioneMarcaDaBollo()) {
			if(praticaInsertRequest.getPratica().getMarcaBolloDetermina()==null){
				AllegatoDTO allegato = praticaInsertRequest.getAllegati().stream().filter(t -> t.getId().equals(Constants.ID_TIPO_ALLEGATO_ATTESTAZIONE_PAGAMENTO_MARCA_DA_BOLLO))
						.findAny()
						.orElse(null);

				if(allegato==null){
					return false;
				}
			}
		}

		if(!pratica.isFlagEsenzionePagamentoCUP()) {
			AllegatoDTO allegato = praticaInsertRequest.getAllegati().stream().filter(t -> t.getTipoAllegato().getId().equals(Constants.ID_TIPO_ALLEGATO_RICEVUTA_DI_PAGAMENTO_CUP))
					.findAny()
					.orElse(null);

			if(allegato==null){
				return false;
			}
		}

		return true;
	}

	@Override
	@LogEntryExit
	public boolean checkAllegatiObbligatori(Pratica pratica, StatoPratica statoPratica, TipoProcesso tipoProcesso,
			Gruppo gruppo) {

		List<TipoAllegatoGruppoStatoProcesso> tipiAllegatiGruppoStatoProcessoObbligatori = tipoAllegatoGruppoStatoProcessoRepository
				.findByStatoPraticaAndTipoProcessoAndGruppoAndFlagObbligatorioTrue(statoPratica, tipoProcesso, gruppo);

		List<TipoAllegato> tipiAllegatiObbligatori = tipiAllegatiGruppoStatoProcessoObbligatori.stream()
				.map(TipoAllegatoGruppoStatoProcesso::getTipoAllegato).collect(Collectors.toList());

		List<Allegato> listaAllegati = allegatoRepository.findByPraticaAndTipoAllegatoWithMaxRevisione(pratica,
				tipiAllegatiObbligatori);

		if(pratica.getMarcaBolloPratica()!=null||pratica.getDatiRichiesta().isFlagEsenzioneMarcaDaBollo()){
			tipiAllegatiObbligatori.removeIf(t -> t.getId().equals(Constants.ID_TIPO_ALLEGATO_ATTESTAZIONE_PAGAMENTO_MARCA_DA_BOLLO));
			tipiAllegatiObbligatori.removeIf(t -> t.getId().equals(Constants.ID_TIPO_ALLEGATO_BOLLO));
		}
		if(pratica.isFlagEsenzionePagamentoCUP()){
			tipiAllegatiObbligatori.removeIf(t -> t.getId().equals(Constants.ID_TIPO_ALLEGATO_RICEVUTA_DI_PAGAMENTO_CUP));
		}

		return tipiAllegatiObbligatori.size() <= listaAllegati.size();
	}

	@Override
	@LogEntryExit
	public void updateAllegatiIntegrazione(Integrazione integrazione) {
		List<Allegato> listaAllegatiIntegrazione = allegatoRepository
				.findByIdRichiestaIntegrazione(integrazione.getRichiestaIntegrazione().getId());
		listaAllegatiIntegrazione = listaAllegatiIntegrazione.stream().filter((el) -> el.getCodiceProtocollo() == null).collect(Collectors.toList());

		for (Allegato allegato : listaAllegatiIntegrazione) {
			allegato.setIntegrazione(integrazione);
			allegato.setIdRichiestaIntegrazione(null);
			allegato.setCodiceProtocollo(integrazione.getCodiceProtocollo());

			allegatoRepository.save(allegato);
		}
	}

	@Override
	@LogEntryExit
	public void updateAllegatiParere(Parere parere) {
		List<Allegato> listaAllegatiParere = allegatoRepository
				.findByIdRichiestaParere(parere.getRichiestaParere().getId());

		// ricerco utenti dello stesso gruppo dell'utente del parere
		List<Utente> listaUtenti = utenteRepository.findByGruppo(parere.getUtenteParere().getGruppo());

		// recupero pareri già espressi dallo stesso gruppo
		List<Parere> listaPareri = new ArrayList<>();
		for (RichiestaParere richiestaParere : parere.getRichiestaParere().getPratica().getRichiestePareri()) {
			if (richiestaParere.getParere() != null && listaUtenti.contains(richiestaParere.getParere().getUtenteParere())) {
				listaPareri.add(richiestaParere.getParere());
			}
		}

		for (Allegato allegato : listaAllegatiParere) {
			// gestione revisione
			List<Allegato> allegatiTrovati = allegatoRepository.findByParereInAndTipoAllegatoAndNota(listaPareri,
					allegato.getTipoAllegato(), allegato.getNota());

			allegato.setParere(parere);
			allegato.setIdRichiestaParere(null);
			allegato.setCodiceProtocollo(parere.getCodiceProtocollo());

			if (allegatiTrovati.isEmpty()) {
				allegato.setRevisione(1);
			} else {
				Allegato allegatoMaxRevisione = allegatiTrovati.stream()
						.max(Comparator.comparing(Allegato::getRevisione)).orElseThrow(NoSuchElementException::new);
				allegato.setRevisione(allegatoMaxRevisione.getRevisione() + 1);
			}

			allegatoRepository.save(allegato);
		}
	}

	@Override
	@LogEntryExit
	public Allegato getAllegatoParereByTipo(Parere parere, TipoAllegato tipoAllegato) {
		List<Allegato> listaAllegati = allegatoRepository.findByParereAndTipoAllegatoOrderByRevisioneDesc(parere,
				tipoAllegato);

		if (!listaAllegati.isEmpty()) {
			return listaAllegati.get(0);
		}

		return null;
	}

	@Override
	@LogEntryExit
	public Allegato getAllegatoPraticaByTipo(Pratica pratica, TipoAllegato tipoAllegato) {
		List<Allegato> listaAllegati = allegatoRepository.findByPraticaAndTipoAllegatoOrderByRevisioneDesc(pratica,
				tipoAllegato);

		if (!listaAllegati.isEmpty()) {
			return listaAllegati.get(0);
		}

		return null;
	}

	@Override
	@LogEntryExit
	public AllegatoDTO insertAllegatoIntegrazioneEgov(
			AllegatoIntegrazioneEgovInsertRequest allegatoIntegrazioneEgovInsertRequest) {
		if (allegatoIntegrazioneEgovInsertRequest.getIdPratica() == null
				&& StringUtils.isBlank(allegatoIntegrazioneEgovInsertRequest.getCodiceProtocollo())) {
			throw new ValidationException("Errore: è obbligatorio almeno uno fra Id Pratica e Codice Protocollo");
		}

		// recupero pratica
		Pratica pratica;
		if (allegatoIntegrazioneEgovInsertRequest.getIdPratica() != null) {
			pratica = praticaRepository.findById(allegatoIntegrazioneEgovInsertRequest.getIdPratica()).orElseThrow(
					() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema",
							allegatoIntegrazioneEgovInsertRequest.getIdPratica()));
		} else {
			Protocollo protocollo = protocolloService
					.getProtocolloByCodice(allegatoIntegrazioneEgovInsertRequest.getCodiceProtocollo());
			if (protocollo == null) {
				throw new BusinessException(ErrorCode.E21, "Errore: codice protocollo non presente nel sistema",
						allegatoIntegrazioneEgovInsertRequest.getCodiceProtocollo());
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

		// inserisco allegato
		AllegatoIntegrazioneInsertRequest allegatoIntegrazioneInsertRequest = new AllegatoIntegrazioneInsertRequest();
		allegatoIntegrazioneInsertRequest.setIdRichiestaIntegrazione(richiestaIntegrazione.getId());
		allegatoIntegrazioneInsertRequest.setAllegato(allegatoIntegrazioneEgovInsertRequest.getAllegato());

		AllegatoDTO result = this.insertAllegatoIntegrazione(allegatoIntegrazioneInsertRequest);

		log.info("Inserimento allegato integrazione da EGOV avvenuto con successo");

		return result;
	}

	@Override
	@LogEntryExit
	@Transactional
	public void insertCodiceProtocolloAllegatiPratica(Pratica pratica, StatoPratica statoPraticaAllegati,
			String codiceProtocollo) {
		List<TipoAllegatoGruppoStatoProcesso> tipiAllegatoGruppoStatoProcesso = tipoAllegatoGruppoStatoProcessoRepository
				.findByStatoPraticaAndTipoProcesso(statoPraticaAllegati, pratica.getTipoProcesso());
		List<TipoAllegato> listaTipiAllegati = tipiAllegatoGruppoStatoProcesso.stream()
				.map(TipoAllegatoGruppoStatoProcesso::getTipoAllegato).collect(Collectors.toList());

		// rimuovo gli eventuali tipi allegati delle determine di rinuncia e rettifica
		// che sono gestiti separatamente
		listaTipiAllegati.removeIf(t -> List
				.of(Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RINUNCIA, Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RETTIFICA)
				.contains(t.getId()));

		List<Allegato> listaAllegati = allegatoRepository.findByPraticaAndTipoAllegatoWithMaxRevisione(pratica,
				listaTipiAllegati);

		for (Allegato allegato : listaAllegati) {
			allegato.setCodiceProtocollo(codiceProtocollo);
			allegatoRepository.save(allegato);
		}
	}

	@Override
	@LogEntryExit
	@Transactional
	public void insertCodiceProtocolloAllegatiPraticaByIdTipo(Pratica pratica, Integer idTipoAllegato, String codiceProtocollo) {

		TipoAllegato tipoAllegato = tipoAllegatoRepository.findById(idTipoAllegato)
				.orElseThrow(() -> new RuntimeException("Tipo allegato non trovato"));

		List<Allegato> listaAllegati = allegatoRepository.findByPraticaAndTipoAllegatoWithMaxRevisione(pratica,
				List.of(tipoAllegato));

		for (Allegato allegato : listaAllegati) {
			allegato.setCodiceProtocollo(codiceProtocollo);
			allegatoRepository.save(allegato);
		}
	}

	@Override
	@LogEntryExit
	public boolean checkAllegatoDeterminaRettifica(Pratica pratica) {

		TipoAllegato tipoAllegato = tipoAllegatoRepository.findById(Constants.ID_TIPO_ALLEGATO_DETERMINA_DI_RETTIFICA)
				.orElseThrow(() -> new RuntimeException("Tipo allegato DETERMINA DI RETTIFICA non trovato"));

		List<Allegato> listaAllegati = allegatoRepository.findByPraticaAndTipoAllegatoWithMaxRevisione(pratica,
				List.of(tipoAllegato));

		return !listaAllegati.isEmpty();
	}

	/**
	 * Effettua l'operazione di inserimento di un allegato gestendo la decodifica da
	 * Base64 e gestendo le revisioni.
	 * 
	 * @param allegatoRequest
	 * @param pratica
	 * @param idRichiestaIntegrazione
	 * @param utente
	 * @return le informazioni sull'allegato inserito
	 */
	@LogEntryExit(showArgs = true)
	private AllegatoDTO performInsertAllegato(AllegatoDTO allegatoRequest, Pratica pratica, Long idRichiestaParere,
			Long idRichiestaIntegrazione, Utente utente) {

		TipoAllegato tipoAllegato = tipoAllegatoRepository.findById(allegatoRequest.getTipoAllegato().getId())
				.orElseThrow(
						() -> new BusinessException(ErrorCode.E1, "Errore: ID tipo allegato non presente nel sistema",
								allegatoRequest.getTipoAllegato().getId()));

		if (tipoAllegato.getId().equals(Constants.ID_TIPO_ALLEGATO_ALTRO_DOCUMENTO)
				&& StringUtils.isBlank(allegatoRequest.getNota())) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: per il tipo di allegato 'Altro documento' è obbligatorio inserire le note");
		}

		if (!Utils.isMimeTypeValid(allegatoRequest.getMimeType())) {
			throw new BusinessException(ErrorCode.E29, "Errore: tipo di file non consentito: "+allegatoRequest.getMimeType());
		}

		Allegato allegato = new Allegato();

		allegato.setPratica(pratica);
		allegato.setIdRichiestaParere(idRichiestaParere);
		allegato.setIdRichiestaIntegrazione(idRichiestaIntegrazione);
		allegato.setTipoAllegato(tipoAllegato);
		allegato.setDataInserimento(LocalDateTime.now().withNano(0));
		allegato.setNota(allegatoRequest.getNota());
		allegato.setCodiceProtocollo(allegatoRequest.getCodiceProtocollo());

		allegato.setNomeFile(allegatoRequest.getNomeFile());
		allegato.setMimeType(allegatoRequest.getMimeType());
		allegato.setFileAllegato(
				Base64.getDecoder().decode(allegatoRequest.getFileAllegato().getBytes(StandardCharsets.UTF_8)));

		// gestione revisione
		List<Allegato> allegatiTrovati = new ArrayList<>();
		if (pratica != null) {
			allegatiTrovati = allegatoRepository.findByPraticaAndTipoAllegatoAndNota(pratica, tipoAllegato,
					allegato.getNota());
		} else if (idRichiestaParere != null) {

			RichiestaParere richiestaParere = richiestaParereRepository.findById(idRichiestaParere)
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID richiesta parere non presente nel sistema", idRichiestaParere));

			// ricerco utenti dello stesso gruppo dell'utente del parere
			List<Utente> listaUtenti = utenteRepository.findByGruppo(utente.getGruppo());

			// recupero pareri degli allegati già inseriti dallo stesso gruppo
			Set<Parere> listaPareri = new HashSet<>();
			for (RichiestaParere currentRichiestaParere : richiestaParere.getPratica().getRichiestePareri()) {
				if (currentRichiestaParere.getParere() != null && listaUtenti.contains(currentRichiestaParere.getParere().getUtenteParere())) {
					listaPareri.add(currentRichiestaParere.getParere());
				}
			}

			allegatiTrovati = allegatoRepository.findByParereInAndTipoAllegatoAndNota(
					new ArrayList<>(listaPareri), tipoAllegato, allegato.getNota());
			
			// aggiungo anche eventuali allegati della stessa richiesta parere
			allegatiTrovati.addAll(allegatoRepository.findByIdRichiestaParereAndTipoAllegatoAndNota(
					idRichiestaParere, tipoAllegato, allegato.getNota()));
		}

		if (pratica == null || allegatiTrovati.isEmpty()) {
			allegato.setRevisione(1);
		} else {
			if (pratica.getStatoPratica().getId().equals(Constants.ID_STATO_PRATICA_BOZZA)) {
				if (allegatiTrovati.size() == 1) {
					Allegato allFromDb = allegatiTrovati.get(0);
					allegato.setId(allFromDb.getId());
				}
				allegato.setRevisione(1);
			} else {
				Allegato allegatoMaxRevisione = allegatiTrovati.stream().max(Comparator.comparing(Allegato::getRevisione))
						.orElseThrow(NoSuchElementException::new);
				allegato.setRevisione(allegatoMaxRevisione.getRevisione() + 1);
			}
		}

		allegatoRepository.save(allegato);

		log.info("Allegato inserito correttamente");

		return allegatoMapper.entityToDto(allegato);
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<AllegatoSimplifiedDTO> getAllegatiDocumentalePratica(Long idPratica) {

		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		List<Parere> pareri = new ArrayList<>();

		pratica.getRichiestePareri().stream().filter(RichiestaParere::isFlagInseritaRisposta)
				.forEach(r -> pareri.add(r.getParere()));

		List<Allegato> result = allegatoRepository
				.findByPraticaOrParereInOrderByDataInserimentoDescTipoAllegatoAscRevisioneDesc(pratica, pareri);

		return result.stream().map(a -> allegatoMapper.entityToSimplifiedDto(a)).collect(Collectors.toList());
	}

}
