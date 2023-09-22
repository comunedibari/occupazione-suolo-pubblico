package it.fincons.osp.services.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.DatiRichiestaDTO;
import it.fincons.osp.dto.DatiRichiestaRettificaDTO;
import it.fincons.osp.dto.GeoMultiPointDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.DatiRichiestaMapper;
import it.fincons.osp.model.DatiRichiesta;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.TipoAttivitaDaSvolgere;
import it.fincons.osp.model.TipoManufatto;
import it.fincons.osp.model.TipoOperazioneVerificaOccupazione;
import it.fincons.osp.model.TipoProcesso;
import it.fincons.osp.model.TipologiaTitoloEdilizio;
import it.fincons.osp.payload.request.DateOccupazioneEditRequest;
import it.fincons.osp.repository.DatiRichiestaRepository;
import it.fincons.osp.repository.PraticaRepository;
import it.fincons.osp.repository.TipoAttivitaDaSvolgereRepository;
import it.fincons.osp.repository.TipoManufattoRepository;
import it.fincons.osp.repository.TipologiaTitoloEdilizioRepository;
import it.fincons.osp.services.DatiRichiestaService;
import it.fincons.osp.utils.Constants;
import it.fincons.osp.utils.GeometryUtil;
import it.fincons.osp.utils.Utils;

@Service
public class DatiRichiestaServiceImpl implements DatiRichiestaService {

	@Value("${osp.app.scadenzario.dateOccupazione.dataInizio.distanza.min.giorni}")
	private Integer numGiorniMinDistanzaDataInizioOccupazione;

	@Value("${osp.app.scadenzario.dateOccupazione.max.giorni}")
	private Integer numGiorniMaxDateOccupazione;

	@Value("${osp.app.richiesta.ore.min}")
	private Integer oreMinRichiesta;

	@Value("${osp.app.proroga.altro.giorniTotMax}")
	private Integer giorniTotMaxProrogheAltro;

	@Autowired
	DatiRichiestaRepository datiRichiestaRepository;

	@Autowired
	TipoAttivitaDaSvolgereRepository tipoAttivitaDaSvolgereRepository;

	@Autowired
	TipologiaTitoloEdilizioRepository tipologiaTitoloEdilizioRepository;

	@Autowired
	TipoManufattoRepository tipoManufattoRepository;

	@Autowired
	DatiRichiestaMapper datiRichiestaMapper;

	@Autowired
	PraticaRepository praticaRepository;

	@Override
	@LogEntryExit(showResult = true)
	public DatiRichiesta insertDatiRichiesta(DatiRichiestaDTO datiRichiestaRequest, TipoProcesso tipoProcesso,
			Pratica praticaOriginaria) {

		// in caso si tratta di una proroga setto le coordinate dell'ubicazione
		// definitive come quelle temporanee in quanto non è prevista la verifica
		// dell'occupazione
		if (tipoProcesso.getId().equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)) {
			datiRichiestaRequest.setCoordUbicazioneDefinitiva(datiRichiestaRequest.getCoordUbicazioneTemporanea());

			// in caso di proroga controllo obbligatorieta' flag "non modifiche rispetto
			// alla concessione"
			if (datiRichiestaRequest.getFlagNonModificheRispettoConcessione() == null
					|| datiRichiestaRequest.getFlagNonModificheRispettoConcessione().equals(Boolean.FALSE)) {
				throw new BusinessException(ErrorCode.E7,
						"Errore: il flag 'non modifiche rispetto alla concessione' è obbligatorio e deve essere settato a true");
			}
		}

		if (tipoProcesso.getId().equals(Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA)) {
			datiRichiestaRequest.setCoordUbicazioneDefinitiva(datiRichiestaRequest.getCoordUbicazioneTemporanea());
		}

		DatiRichiesta datiRichiesta = datiRichiestaMapper.dtoToEntity(datiRichiestaRequest);
		this.performInsertEditDatiRichiesta(datiRichiesta, datiRichiestaRequest, tipoProcesso, praticaOriginaria);

		return datiRichiesta;
	}

	@Override
	@LogEntryExit
	public void editDatiRichiesta(DatiRichiesta datiRichiesta, DatiRichiestaDTO datiRichiestaRequest,
			TipoProcesso tipoProcesso, Pratica praticaOriginaria) {
		this.buildDatiRichiesta(datiRichiesta, datiRichiestaRequest, tipoProcesso);
		this.performInsertEditDatiRichiesta(datiRichiesta, datiRichiestaRequest, tipoProcesso, praticaOriginaria);
	}

	@Override
	@LogEntryExit
	public List<String> existSovrapposizioneCoordUbicazione(DatiRichiesta datiRichiesta,
			TipoOperazioneVerificaOccupazione tipoOperazione, GeoMultiPointDTO coordUbicazioneDefinitiva) {
		String numeroVia = StringUtils.isBlank(datiRichiesta.getNumeroVia()) ? "" : datiRichiesta.getNumeroVia();

		switch (tipoOperazione) {
		case VERIFICA_OCCUPAZIONE:
			if (coordUbicazioneDefinitiva == null || coordUbicazioneDefinitiva.getPoints().isEmpty()) {
				throw new BusinessException(ErrorCode.E7,
						"Errore: in caso di 'Verifica occupazione', le coordinate dell'ubicazione definitiva sono obbligatorie");
			}

			List<DatiRichiesta> stessaUbicazione = new ArrayList<>();
			Geometry ubicazioneDefinitivaParsed = GeometryUtil.parseGeoGeomerty(coordUbicazioneDefinitiva);
			if(ubicazioneDefinitivaParsed != null && coordUbicazioneDefinitiva.getPoints().size() == 1) {
				stessaUbicazione = datiRichiestaRepository.findStessaUbicazione(Utils.getIdStatiPraticaConclusivi(),
						datiRichiesta.getNomeVia(), numeroVia,
						datiRichiesta.getLocalita(), datiRichiesta.getDataInizioOccupazione(),
						datiRichiesta.getDataScadenzaOccupazione(),
						datiRichiesta.getCoordUbicazioneDefinitiva(),
						datiRichiesta.getCoordUbicazioneDefinitiva());
			}

			List<DatiRichiesta> sovrapposizione = datiRichiestaRepository.countOverlappingUbicazione(Utils.getIdStatiPraticaConclusivi(),
					datiRichiesta.getDataInizioOccupazione(), datiRichiesta.getDataScadenzaOccupazione(),
					GeometryUtil.parseGeoGeomerty(coordUbicazioneDefinitiva));

			if(stessaUbicazione.size() > 0  || sovrapposizione.size() > 0) {
				stessaUbicazione.addAll(sovrapposizione);
				return getListCodiceFiscaleOverlap(datiRichiesta, stessaUbicazione);
			} else {
				return new ArrayList<>();
			}
		case SALTA_VERIFICA_OCCUPAZIONE:
			// non faccio nulla
			return new ArrayList<>();

		case OCCUPAZIONE_CORRETTA:
			List<DatiRichiesta> stessaUbicazioneCorretta = datiRichiestaRepository.findStessaUbicazione(Utils.getIdStatiPraticaConclusivi(),
					datiRichiesta.getNomeVia(), numeroVia,
					datiRichiesta.getLocalita(), datiRichiesta.getDataInizioOccupazione(),
					datiRichiesta.getDataScadenzaOccupazione(),
					datiRichiesta.getCoordUbicazioneTemporanea(),
					datiRichiesta.getCoordUbicazioneTemporanea());
			List<DatiRichiesta> sovrapposizioneCorretta = datiRichiestaRepository.countOverlappingUbicazione(Utils.getIdStatiPraticaConclusivi(),
					datiRichiesta.getDataInizioOccupazione(), datiRichiesta.getDataScadenzaOccupazione(),
					datiRichiesta.getCoordUbicazioneTemporanea());

			if(stessaUbicazioneCorretta.size() > 0 || sovrapposizioneCorretta.size() > 0) {
				stessaUbicazioneCorretta.addAll(sovrapposizioneCorretta);
				return getListCodiceFiscaleOverlap(datiRichiesta, stessaUbicazioneCorretta);
			} else {
				return new ArrayList<>();
			}
		default:
			throw new BusinessException(ErrorCode.E7, "Tipo operazione non valido");

		}
	}

	private List<String> getListCodiceFiscaleOverlap(DatiRichiesta datiRichiesta, List<DatiRichiesta> listaDatiRichiesta) {
		return listaDatiRichiesta.stream()
				.filter(datiRichiesta1 -> !datiRichiesta1.getPratica().getId().equals(datiRichiesta.getPratica().getId()))
				.map(datiRichiesta1 -> datiRichiesta1.getPratica().getFirmatario().getCodiceFiscalePartitaIva())
				.distinct().collect(Collectors.toList());
	}

	@Override
	@LogEntryExit
	public void verificaOccupazione(DatiRichiesta datiRichiesta, TipoOperazioneVerificaOccupazione tipoOperazione,
			GeoMultiPointDTO coordUbicazioneDefinitiva) {

		switch (tipoOperazione) {
			case VERIFICA_OCCUPAZIONE:
				datiRichiesta.setCoordUbicazioneDefinitiva(GeometryUtil.parseGeoGeomerty(coordUbicazioneDefinitiva));
				break;

			case SALTA_VERIFICA_OCCUPAZIONE:
				// non faccio nulla
				break;

			case OCCUPAZIONE_CORRETTA:
				datiRichiesta.setCoordUbicazioneDefinitiva(datiRichiesta.getCoordUbicazioneTemporanea());
				break;
			default:
				throw new BusinessException(ErrorCode.E7, "Tipo operazione non valido");
		}

		datiRichiesta.setTipoOperazioneVerificaOccupazione(tipoOperazione);
		datiRichiestaRepository.save(datiRichiesta);
	}

	@Override
	@LogEntryExit
	public void editDateOccupazione(Pratica pratica, DateOccupazioneEditRequest dateOccupazioneEditRequest) {

		DatiRichiesta datiRichiesta = pratica.getDatiRichiesta();

		Pratica praticaOriginaria = null;

		if (pratica.getIdPraticaOriginaria() != null) {
			praticaOriginaria = praticaRepository.findById(pratica.getIdPraticaOriginaria())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID pratica originaria non presente nel sistema",
							pratica.getIdPraticaOriginaria()));
		}

		DatiRichiestaDTO datiRichiestaRequest = new DatiRichiestaDTO();
		datiRichiestaRequest.setId(datiRichiesta.getId());
		datiRichiestaRequest.setNomeVia(datiRichiesta.getNomeVia());
		datiRichiestaRequest.setNumeroVia(datiRichiesta.getNumeroVia());
		datiRichiestaRequest.setLocalita(datiRichiesta.getLocalita());
		datiRichiestaRequest.setDataInizioOccupazione(dateOccupazioneEditRequest.getDataInizioOccupazione());
		datiRichiestaRequest.setDataScadenzaOccupazione(dateOccupazioneEditRequest.getDataScadenzaOccupazione());
		datiRichiestaRequest.setCoordUbicazioneTemporanea(
				GeometryUtil.parseGeometryToDto(datiRichiesta.getCoordUbicazioneTemporanea()));
//		datiRichiestaRequest.setCoordUbicazioneDefinitiva(
//				GeometryUtil.parseGeometryToDto(datiRichiesta.getCoordUbicazioneDefinitiva()));

		// controllo esistenza pratica con stessa ubicazione
//		if (!StringUtils.isBlank(datiRichiestaRequest.getNumeroVia())
//				&& this.existPraticaStessaUbicazione(datiRichiestaRequest, praticaOriginaria)) {
//			throw new BusinessException(ErrorCode.E10,
//					"Errore: esiste già una pratica in elaborazione con la stessa ubicazione");
//		}

		if (!pratica.getTipoProcesso().getId().equals(Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA)) {
			// controllo distanza data inizio occupazione
			if (!this.checkDataInizioOccupazione(dateOccupazioneEditRequest.getDataInizioOccupazione())) {
				throw new BusinessException(ErrorCode.E2,
						"Errore: la distanza da oggi alla data di inizio occupazione è inferiore al limite minimo di "
								+ numGiorniMinDistanzaDataInizioOccupazione + " giorni");
			}
		}

		// controllo numero giorni occupazione
		if (!this.checkDateOccupazione(dateOccupazioneEditRequest.getDataInizioOccupazione(),
				dateOccupazioneEditRequest.getDataScadenzaOccupazione())) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: il numero totale dei giorni di occupazione supera il limite massimo di "
							+ numGiorniMaxDateOccupazione + " giorni");
		}

		// in caso di occupazione di un solo giorno, gli orari sono obbligatori
		if (dateOccupazioneEditRequest.getDataInizioOccupazione()
				.equals(dateOccupazioneEditRequest.getDataScadenzaOccupazione())
				&& (dateOccupazioneEditRequest.getOraInizioOccupazione() == null
						|| dateOccupazioneEditRequest.getOraScadenzaOccupazione() == null)) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: in caso di occupazione di un solo giorno, gli orari di inizio e fine occupazione sono obbligatori");
		}

		if (dateOccupazioneEditRequest.getDataInizioOccupazione()
				.isAfter(dateOccupazioneEditRequest.getDataScadenzaOccupazione())) {
			throw new BusinessException(ErrorCode.E2,
					"Errore: la data scadenza occupazione deve essere uguale o successiva alla data inizio occupazione");
		}

		// controllo numero ore minimo di occupazione
		if (!checkOrariOccupazine(dateOccupazioneEditRequest.getDataInizioOccupazione(),
				dateOccupazioneEditRequest.getOraInizioOccupazione(),
				dateOccupazioneEditRequest.getDataScadenzaOccupazione(),
				dateOccupazioneEditRequest.getOraScadenzaOccupazione())) {
			throw new BusinessException(ErrorCode.E17,
					"Errore: l'orario minimo di occupazione è di " + oreMinRichiesta + "ora");
		}

		if (pratica.getTipoProcesso().getId().equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)) {

			this.controlloDateProcessoProroga(praticaOriginaria, pratica.getDatiRichiesta().getAttivitaDaSvolgere(),
					dateOccupazioneEditRequest.getDataInizioOccupazione(),
					dateOccupazioneEditRequest.getDataScadenzaOccupazione());
		}

		datiRichiesta.setDataInizioOccupazione(dateOccupazioneEditRequest.getDataInizioOccupazione());
		datiRichiesta.setOraInizioOccupazione(dateOccupazioneEditRequest.getOraInizioOccupazione());
		datiRichiesta.setDataScadenzaOccupazione(dateOccupazioneEditRequest.getDataScadenzaOccupazione());
		datiRichiesta.setOraScadenzaOccupazione(dateOccupazioneEditRequest.getOraScadenzaOccupazione());

		datiRichiestaRepository.save(datiRichiesta);
	}

	@Override
	public void updateDatiRichiestaRettifica(DatiRichiesta datiRichiesta,
			DatiRichiestaRettificaDTO datiRichiestaRettifica) {
		
		datiRichiesta.setNoteUbicazione(datiRichiestaRettifica.getNoteUbicazione());
		datiRichiesta.setSuperficieAreaMq(datiRichiestaRettifica.getSuperficieAreaMq());
		datiRichiesta.setLarghezzaM(datiRichiestaRettifica.getLarghezzaM());
		datiRichiesta.setLunghezzaM(datiRichiestaRettifica.getLunghezzaM());
		datiRichiesta.setSuperficieMarciapiedeMq(datiRichiestaRettifica.getSuperficieMarciapiedeMq());
		datiRichiesta.setLarghezzaMarciapiedeM(datiRichiestaRettifica.getLarghezzaMarciapiedeM());
		datiRichiesta.setLunghezzaMarciapiedeM(datiRichiestaRettifica.getLunghezzaMarciapiedeM());
		datiRichiesta.setLarghezzaCarreggiataM(datiRichiestaRettifica.getLarghezzaCarreggiataM());
		datiRichiesta.setLunghezzaCarreggiataM(datiRichiestaRettifica.getLunghezzaCarreggiataM());
		datiRichiesta.setStalloDiSosta(datiRichiestaRettifica.getStalloDiSosta());
		datiRichiesta.setPresScivoliDiversamenteAbili(datiRichiestaRettifica.getPresScivoliDiversamenteAbili());
		datiRichiesta.setPresPassiCarrabiliDiversamenteAbili(datiRichiestaRettifica.getPresPassiCarrabiliDiversamenteAbili());
		datiRichiesta.setDescrizioneTitoloEdilizio(datiRichiestaRettifica.getDescrizioneTitoloEdilizio());
		datiRichiesta.setRiferimentoTitoloEdilizio(datiRichiestaRettifica.getRiferimentoTitoloEdilizio());
		datiRichiesta.setDescrizioneAttivitaDaSvolgere(datiRichiestaRettifica.getDescrizioneAttivitaDaSvolgere());
		datiRichiesta.setDescrizioneManufatto(datiRichiestaRettifica.getDescrizioneManufatto());

		if(datiRichiesta.isFlagEsenzioneMarcaDaBollo() != datiRichiestaRettifica.isFlagEsenzioneMarcaDaBollo()) {
			datiRichiesta.setFlagEsenzioneMarcaDaBollo(datiRichiestaRettifica.isFlagEsenzioneMarcaDaBollo());
			datiRichiesta.setFlagEsenzioneMarcaDaBolloModificato(true);
		}
		if(datiRichiestaRettifica.getMotivazioneEsenzioneMarcaBollo() != null) {
			datiRichiesta.setMotivazioneEsenzioneMarcaBollo(datiRichiestaRettifica.getMotivazioneEsenzioneMarcaBollo());
		}

		if (datiRichiestaRettifica.getIdTipologiaTitoloEdilizio() != null) {
			TipologiaTitoloEdilizio tipologiaTitoloEdilizio = tipologiaTitoloEdilizioRepository
					.findById(datiRichiestaRettifica.getIdTipologiaTitoloEdilizio())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID tipologia titolo edilizio non presente nel sistema",
							datiRichiestaRettifica.getIdTipologiaTitoloEdilizio()));
			datiRichiesta.setTipologiaTitoloEdilizio(tipologiaTitoloEdilizio);
		}

		if (datiRichiestaRettifica.getIdManufatto() != null) {
			TipoManufatto tipoManufatto = tipoManufattoRepository.findById(datiRichiestaRettifica.getIdManufatto())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID tipo manufatto non presente nel sistema",
							datiRichiestaRettifica.getIdManufatto()));
			datiRichiesta.setManufatto(tipoManufatto);
		}
		
		datiRichiestaRepository.save(datiRichiesta);
	}

	/**
	 * Effettua le operazioni di inserimento/modifica Dati Richiesta
	 * 
	 * @param datiRichiesta
	 * @param datiRichiestaRequest
	 */
	private void performInsertEditDatiRichiesta(DatiRichiesta datiRichiesta, DatiRichiestaDTO datiRichiestaRequest,
			TipoProcesso tipoProcesso, Pratica praticaOriginaria) {

		TipoAttivitaDaSvolgere tipoAttivitaDaSvolgere = tipoAttivitaDaSvolgereRepository
				.findById(datiRichiestaRequest.getIdAttivitaDaSvolgere())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID attività da svolgere non presente nel sistema",
						datiRichiestaRequest.getIdAttivitaDaSvolgere()));
		datiRichiesta.setAttivitaDaSvolgere(tipoAttivitaDaSvolgere);

		if (datiRichiestaRequest.getIdTipologiaTitoloEdilizio() != null) {
			TipologiaTitoloEdilizio tipologiaTitoloEdilizio = tipologiaTitoloEdilizioRepository
					.findById(datiRichiestaRequest.getIdTipologiaTitoloEdilizio())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID tipologia titolo edilizio non presente nel sistema",
							datiRichiestaRequest.getIdTipologiaTitoloEdilizio()));
			datiRichiesta.setTipologiaTitoloEdilizio(tipologiaTitoloEdilizio);
		}

		if (datiRichiestaRequest.getIdManufatto() != null) {
			TipoManufatto tipoManufatto = tipoManufattoRepository.findById(datiRichiestaRequest.getIdManufatto())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID tipo manufatto non presente nel sistema",
							datiRichiestaRequest.getIdManufatto()));
			datiRichiesta.setManufatto(tipoManufatto);
		}

		// controllo esistenza pratica con stessa ubicazione
//		if (!StringUtils.isBlank(datiRichiestaRequest.getNumeroVia())
//				&& this.existPraticaStessaUbicazione(datiRichiestaRequest, praticaOriginaria)) {
//			throw new BusinessException(ErrorCode.E10,
//					"Errore: esiste già una pratica in elaborazione con la stessa ubicazione");
//		}

		if (!tipoProcesso.getId().equals(Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA)) {
			// controllo distanza data inizio occupazione
			if (!this.checkDataInizioOccupazione(datiRichiestaRequest.getDataInizioOccupazione())) {
				throw new BusinessException(ErrorCode.E7,
						"Errore: la distanza da oggi alla data di inizio occupazione è inferiore al limite minimo di "
								+ numGiorniMinDistanzaDataInizioOccupazione + " giorni");
			}
		}

		// controllo numero giorni occupazione
		if (!this.checkDateOccupazione(datiRichiestaRequest.getDataInizioOccupazione(),
				datiRichiestaRequest.getDataScadenzaOccupazione())) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: il numero totale dei giorni di occupazione supera il limite massimo di "
							+ numGiorniMaxDateOccupazione + " giorni");
		}

		// in caso di occupazione di un solo giorno, gli orari sono obbligatori
		if (datiRichiestaRequest.getDataInizioOccupazione().equals(datiRichiestaRequest.getDataScadenzaOccupazione())
				&& (datiRichiestaRequest.getOraInizioOccupazione() == null
						|| datiRichiestaRequest.getOraScadenzaOccupazione() == null)) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: in caso di occupazione di un solo giorno, gli orari di inizio e fine occupazione sono obbligatori");
		}

		if (datiRichiestaRequest.getDataInizioOccupazione()
				.isAfter(datiRichiestaRequest.getDataScadenzaOccupazione())) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: la data scadenza occupazione deve essere uguale o successiva alla data inizio occupazione");
		}

		// controllo numero ore minimo di occupazione
		if (!checkOrariOccupazine(datiRichiestaRequest.getDataInizioOccupazione(),
				datiRichiestaRequest.getOraInizioOccupazione(), datiRichiestaRequest.getDataScadenzaOccupazione(),
				datiRichiestaRequest.getOraScadenzaOccupazione())) {
			throw new BusinessException(ErrorCode.E17,
					"Errore: l'orario minimo di occupazione è di " + oreMinRichiesta + "ora");
		}

		if (tipoProcesso.getId().equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)) {
			this.controlloDateProcessoProroga(praticaOriginaria, tipoAttivitaDaSvolgere,
					datiRichiestaRequest.getDataInizioOccupazione(), datiRichiestaRequest.getDataScadenzaOccupazione());
		}

		// controllo obbligatorieta' numero via
		if (!datiRichiestaRequest.isFlagNumeroCivicoAssente()
				&& StringUtils.isBlank(datiRichiestaRequest.getNumeroVia())) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: in caso di numero civico presente, il campo numero via è obbligatorio");
		}

		// controllo obbligatorieta' note ubicazione
		if (datiRichiestaRequest.isFlagNumeroCivicoAssente()
				&& StringUtils.isBlank(datiRichiestaRequest.getNoteUbicazione())) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: in caso di numero civico assente, il campo note ubicazione è obbligatorio");
		}

		if ( praticaOriginaria != null &&
				praticaOriginaria.getDatiRichiesta().isFlagEsenzioneMarcaDaBollo() !=
						datiRichiesta.isFlagEsenzioneMarcaDaBollo() &&
				!datiRichiesta.isFlagEsenzioneMarcaDaBolloModificato()
		) {
			datiRichiesta.setFlagEsenzioneMarcaDaBolloModificato(true);
		}

		datiRichiestaRepository.save(datiRichiesta);
	}

	/**
	 * Effettua il set dei campi dei dati richiesta aggiornati
	 * 
	 * @param datiRichiesta
	 * @param dto
	 */
	private void buildDatiRichiesta(DatiRichiesta datiRichiesta, DatiRichiestaDTO dto, TipoProcesso tipoProcesso) {

		datiRichiesta.setUbicazioneOccupazione(dto.getUbicazioneOccupazione());
		datiRichiesta.setNomeVia(dto.getNomeVia());
		datiRichiesta.setNumeroVia(dto.getNumeroVia());
		datiRichiesta.setCodVia(dto.getCodVia());
		datiRichiesta.setIdMunicipio(dto.getIdMunicipio());
		datiRichiesta.setLocalita(dto.getLocalita());
		datiRichiesta.setFlagNumeroCivicoAssente(dto.isFlagNumeroCivicoAssente());
		datiRichiesta.setNoteUbicazione(dto.getNoteUbicazione());
		datiRichiesta.setSuperficieAreaMq(dto.getSuperficieAreaMq());
		datiRichiesta.setLarghezzaM(dto.getLarghezzaM());
		datiRichiesta.setLunghezzaM(dto.getLunghezzaM());
		datiRichiesta.setSuperficieMarciapiedeMq(dto.getSuperficieMarciapiedeMq());
		datiRichiesta.setLarghezzaMarciapiedeM(dto.getLarghezzaMarciapiedeM());
		datiRichiesta.setLunghezzaMarciapiedeM(dto.getLunghezzaMarciapiedeM());
		datiRichiesta.setLarghezzaCarreggiataM(dto.getLarghezzaCarreggiataM());
		datiRichiesta.setLunghezzaCarreggiataM(dto.getLunghezzaCarreggiataM());
		datiRichiesta.setStalloDiSosta(dto.getStalloDiSosta());
		datiRichiesta.setPresScivoliDiversamenteAbili(dto.getPresScivoliDiversamenteAbili());
		datiRichiesta.setPresPassiCarrabiliDiversamenteAbili(dto.getPresPassiCarrabiliDiversamenteAbili());
		datiRichiesta.setDataInizioOccupazione(dto.getDataInizioOccupazione());
		datiRichiesta.setOraInizioOccupazione(dto.getOraInizioOccupazione());
		datiRichiesta.setDataScadenzaOccupazione(dto.getDataScadenzaOccupazione());
		datiRichiesta.setOraScadenzaOccupazione(dto.getOraScadenzaOccupazione());
		datiRichiesta.setDescrizioneTitoloEdilizio(dto.getDescrizioneTitoloEdilizio());
		datiRichiesta.setRiferimentoTitoloEdilizio(dto.getRiferimentoTitoloEdilizio());
		datiRichiesta.setDescrizioneAttivitaDaSvolgere(dto.getDescrizioneAttivitaDaSvolgere());
		datiRichiesta.setDescrizioneManufatto(dto.getDescrizioneManufatto());
		if (dto.getFlagAccettazioneRegSuoloPubblico() != null) {
			datiRichiesta.setFlagAccettazioneRegSuoloPubblico(dto.getFlagAccettazioneRegSuoloPubblico());
		}
		if (dto.getFlagRispettoInteresseTerzi() != null) {
			datiRichiesta.setFlagRispettoInteresseTerzi(dto.getFlagRispettoInteresseTerzi());
		}
		if (dto.getFlagObbligoRiparazioneDanni() != null) {
			datiRichiesta.setFlagObbligoRiparazioneDanni(dto.getFlagObbligoRiparazioneDanni());
		}
		if (dto.getFlagRispettoDisposizioniRegolamento() != null) {
			datiRichiesta.setFlagRispettoDisposizioniRegolamento(dto.getFlagRispettoDisposizioniRegolamento());
		}
		if (dto.getFlagConoscenzaTassaOccupazione() != null) {
			datiRichiesta.setFlagConoscenzaTassaOccupazione(dto.getFlagConoscenzaTassaOccupazione());
		}
		datiRichiesta.setFlagEsenzioneMarcaDaBollo(dto.isFlagEsenzioneMarcaDaBollo());
		if (dto.isFlagEsenzioneMarcaDaBollo()) {
			datiRichiesta.setMotivazioneEsenzioneMarcaBollo(dto.getMotivazioneEsenzioneMarcaBollo());
		}
		if (tipoProcesso.getId().equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)) {
			if (dto.getFlagNonModificheRispettoConcessione() == null
					|| dto.getFlagNonModificheRispettoConcessione().equals(Boolean.FALSE)) {
				throw new BusinessException(ErrorCode.E7,
						"Errore: il flag 'non modifiche rispetto alla concessione' è obbligatorio e deve essere settato a true");
			} else {
				datiRichiesta.setFlagNonModificheRispettoConcessione(dto.getFlagNonModificheRispettoConcessione());
			}
		} else {
			datiRichiesta.setFlagNonModificheRispettoConcessione(false);
		}

		datiRichiesta.setCoordUbicazioneTemporanea(GeometryUtil.parseGeoGeomerty(dto.getCoordUbicazioneTemporanea()));
		datiRichiesta.setCoordUbicazioneDefinitiva(GeometryUtil.parseGeoGeomerty(dto.getCoordUbicazioneDefinitiva()));
	}

	/**
	 * Controlla se esiste una pratica con gli stessi dati di ubicazione che non sia
	 * in uno stato conclusivo
	 * 
	 * @param datiRichiestaRequest
	 * @return boolean
	 */
	private boolean existPraticaStessaUbicazione(DatiRichiestaDTO datiRichiestaRequest, Pratica praticaOrigina) {

		List<DatiRichiesta> result = datiRichiestaRepository.findStessaUbicazione(Utils.getIdStatiPraticaConclusivi(),
				datiRichiestaRequest.getNomeVia(), datiRichiestaRequest.getNumeroVia(),
				datiRichiestaRequest.getLocalita(), datiRichiestaRequest.getDataInizioOccupazione(),
				datiRichiestaRequest.getDataScadenzaOccupazione(),
				GeometryUtil.parseGeoGeomerty(datiRichiestaRequest.getCoordUbicazioneTemporanea()),
				GeometryUtil.parseGeoGeomerty(datiRichiestaRequest.getCoordUbicazioneDefinitiva()));

		if (praticaOrigina != null) {
			// in caso di proroga o rinuncia rimuovo la pratica originaria
			result.remove(praticaOrigina.getDatiRichiesta());

			// in caso la pratica originaria e' gia' una proroga, rimuovo anche la pratica
			// padre
			if (praticaOrigina.getTipoProcesso().getId()
					.equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)) {
				result.remove(praticaRepository.findById(praticaOrigina.getIdPraticaOriginaria())
						.orElseThrow(() -> new BusinessException(ErrorCode.E1,
								"Errore: ID pratica originaria non presente nel sistema"))
						.getDatiRichiesta());
			}
		}

		if (result.isEmpty()) {
			return false;
		} else {
			// se è presente l'id dei dati richiesta, vuol dire che sono in modifica e devo
			// escludere dai risultati la richiesta attuale
			if (datiRichiestaRequest.getId() != null) {
				result.removeIf(d -> d.getId().equals(datiRichiestaRequest.getId()));
			}

			return !result.isEmpty();
		}
	}

	/**
	 * Controlla che i giorni dalla data odierna alla data inizio occupazione siano
	 * superiori o uguali al minimo consentitio
	 * 
	 * @param dataInizioOccupazione
	 * @return true se le data inizio occupazione e' corretta, false altrimenti
	 */
	private boolean checkDataInizioOccupazione(LocalDate dataInizioOccupazione) {
		return ChronoUnit.DAYS.between(LocalDate.now(),
				dataInizioOccupazione) >= numGiorniMinDistanzaDataInizioOccupazione;
	}

	/**
	 * Controlla che i giorni fra le date di occupazione non superino il numero
	 * massimo stabilito
	 * @param dataInizioOccupazione
	 * @param dataScadenzaOccupazione
	 * @return
	 */
	private boolean checkDateOccupazione(LocalDate dataInizioOccupazione, LocalDate dataScadenzaOccupazione) {
		return ChronoUnit.DAYS.between(dataInizioOccupazione, dataScadenzaOccupazione) <= numGiorniMaxDateOccupazione;
	}

	/**
	 * Controlla, in caso di occupazione di un solo giorno, che ci sia almeno il
	 * numero di ore minimo richiesto
	 * @param dataInizioOccupazione
	 * @param oraInizioOccupazione
	 * @param dataScadenzaOccupazione
	 * @param oraScadenzaOccupazione
	 * @return
	 */
	private boolean checkOrariOccupazine(LocalDate dataInizioOccupazione, LocalTime oraInizioOccupazione,
			LocalDate dataScadenzaOccupazione, LocalTime oraScadenzaOccupazione) {
		boolean ret = true;
		long days = ChronoUnit.DAYS.between(dataInizioOccupazione, dataScadenzaOccupazione);

		if (days == 0) {
			long hours = ChronoUnit.HOURS.between(oraInizioOccupazione, oraScadenzaOccupazione);
			if (hours < oreMinRichiesta) {
				ret = false;
			}
		}

		return ret;
	}

	/**
	 * in caso di processo di proroga, per le richieste di attività diverse da
	 * ediliza, faccio ulteriori controlli: la richiesta di proroga deve essere di
	 * durata minore della richiesta originaria. Per i cantieri edili e stradali la
	 * proroga può essere richiesta di un ulteriore anno. Inoltre, eccetto per i
	 * cantieri edili e stradali, la somma della durata della concessione e della
	 * proroga non deve superare l’anno. Di conseguenza, nel caso venga richiesta la
	 * concessione temporanea della durata di un anno non sarà possibile richiedere
	 * una proroga (eccetto per i cantieri edili e stradali).
	 * 
	 * @param praticaOriginaria
	 * @param tipoAttivitaDaSvolgere
	 * @param dataInizioOccupazione
	 * @param dataScadenzaOccupazione
	 */
	private void controlloDateProcessoProroga(Pratica praticaOriginaria, TipoAttivitaDaSvolgere tipoAttivitaDaSvolgere,
			LocalDate dataInizioOccupazione, LocalDate dataScadenzaOccupazione) {

		// controllo coerenza data fine occupazione pratica originaria con nuova
		// richiesta di proroga occupazione
		if (praticaOriginaria.getDatiRichiesta().getOraScadenzaOccupazione() == null
				&& ChronoUnit.DAYS.between(dataInizioOccupazione, dataScadenzaOccupazione) == 0L) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: in caso di processo di proroga, se la pratica originaria non aveva l'ora di scadenza occupazione, la nuova richiesta di occupazione deve essere almeno di un giorno");
		}

		if (!tipoAttivitaDaSvolgere.getId().equals(Constants.ID_TIPO_ATTIVITA_EDILIZIA)) {
			long numeroGiorniRichiesta = ChronoUnit.DAYS.between(dataInizioOccupazione, dataScadenzaOccupazione);

			long numeroGiorniRichiestaOriginaria = ChronoUnit.DAYS.between(
					praticaOriginaria.getDatiRichiesta().getDataInizioOccupazione(),
					praticaOriginaria.getDatiRichiesta().getDataScadenzaOccupazione());

			if (numeroGiorniRichiesta > numeroGiorniRichiestaOriginaria) {
				throw new BusinessException(ErrorCode.E7,
						"Per questa pratica non è possibile effettuare richieste di proroga: la durata della richiesta di proroga non può essere superiore alla durata della concessone a cui fa riferimento");
			}

			// in caso la pratica originaria sia gia' una proroga, utilizzo l'id della
			// pratica padre per il conteggio del numero di proroghe
			Long idPraticaOriginariaRicercaProroghe = praticaOriginaria.getId();
			if (praticaOriginaria.getTipoProcesso().getId()
					.equals(Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA)) {
				idPraticaOriginariaRicercaProroghe = praticaOriginaria.getIdPraticaOriginaria();
			}

			List<Pratica> proroghe = praticaRepository
					.findByIdPraticaOriginariaOrderByIdDesc(idPraticaOriginariaRicercaProroghe);

			if (!proroghe.isEmpty()) {
				for (Pratica proroga : proroghe) {
					numeroGiorniRichiesta += ChronoUnit.DAYS.between(
							proroga.getDatiRichiesta().getDataInizioOccupazione(),
							proroga.getDatiRichiesta().getDataScadenzaOccupazione());
				}
			}
			if (numeroGiorniRichiesta >= giorniTotMaxProrogheAltro) {
				throw new BusinessException(ErrorCode.E16,
						"Errore: è stato superato il numero massimo di giorni di concessione (pratica di concessione ed eventuali proroghe)");
			}
		}
	}

}
