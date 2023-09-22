package it.fincons.osp.services.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.RichiedenteDTO;
import it.fincons.osp.dto.RichiedenteRettificaDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.RichiedenteMapper;
import it.fincons.osp.model.Richiedente;
import it.fincons.osp.model.TipoDocumentoAllegato;
import it.fincons.osp.model.TipoRuoloRichiedente;
import it.fincons.osp.repository.RichiedenteRepository;
import it.fincons.osp.repository.TipoDocumentoAllegatoRepository;
import it.fincons.osp.repository.TipoRuoloRichiedenteRepository;
import it.fincons.osp.repository.specification.RichiedenteSpecification;
import it.fincons.osp.services.RichiedenteService;
import it.fincons.osp.utils.Constants;

@Service
public class RichiedenteServiceImpl implements RichiedenteService {

	@Autowired
	RichiedenteRepository richiedenteRepository;

	@Autowired
	TipoRuoloRichiedenteRepository tipoRuoloRichiedenteRepository;

	@Autowired
	TipoDocumentoAllegatoRepository tipoDocumentoAllegatoRepository;

	@Autowired
	RichiedenteMapper richiedenteMapper;

	@Autowired
	RichiedenteSpecification richiedenteSpecification;

	@Override
	@LogEntryExit(showResult = true)
	public Richiedente insertRichiedente(RichiedenteDTO richiedenteRequest) {

		Richiedente richiedente = richiedenteMapper.dtoToEntity(richiedenteRequest);
		richiedente.setProvincia(
				StringUtils.isNotBlank(richiedente.getProvincia()) ? richiedente.getProvincia().toUpperCase() : null);
		richiedente.setProvinciaDiNascita(StringUtils.isNotBlank(richiedente.getProvinciaDiNascita())
				? richiedente.getProvinciaDiNascita().toUpperCase()
				: null);
		this.performInsertEditRichiedente(richiedente, richiedenteRequest);

		return richiedente;
	}

	@Override
	@LogEntryExit
	public void editRichiedente(Richiedente richiedente, RichiedenteDTO richiedenteRequest) {

		this.buildRichiedente(richiedente, richiedenteRequest);
		this.performInsertEditRichiedente(richiedente, richiedenteRequest);
	}

	@Override
	@LogEntryExit
	public List<Richiedente> searchDestinatari(String nome, String cognome, String denominazioneRagSoc,
			String codFiscalePIva) {

		// il destinatario può essere il firmatario che ha il tipo ruolo richiedente
		// "Destinatario" oppure il destinatario che ha il flag
		// "flagDestinatario" a true

//		TipoRuoloRichiedente tipoRuoloRichiedenteDestinatario = tipoRuoloRichiedenteRepository
//				.findById(Constants.ID_TIP_RUOLO_RICHIEDENTE_DESTINATARIO)
//				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
//						"Errore: ID tipo ruolo richiedente non presente nel sistema"));
//
//		Specification<Richiedente> conditionsDestinatarioOr = Specification
//				.where(richiedenteSpecification.isDestinatario());
//
//		conditionsDestinatarioOr = richiedenteSpecification.addSpecificationToConditionListOr(
//				richiedenteSpecification.equals(tipoRuoloRichiedenteDestinatario, "tipoRuoloRichiedente"),
//				conditionsDestinatarioOr);

		Specification<Richiedente> conditionsAnd = Specification.where(richiedenteSpecification.inizialize());
//		conditionsAnd = conditionsAnd.and(conditionsDestinatarioOr);

		conditionsAnd = richiedenteSpecification
				.addSpecificationToConditionListAnd(richiedenteSpecification.contains(nome, "nome"), conditionsAnd);

		conditionsAnd = richiedenteSpecification.addSpecificationToConditionListAnd(
				richiedenteSpecification.contains(cognome, "cognome"), conditionsAnd);

		conditionsAnd = richiedenteSpecification.addSpecificationToConditionListAnd(
				richiedenteSpecification.contains(denominazioneRagSoc, "denominazione"), conditionsAnd);

		conditionsAnd = richiedenteSpecification.addSpecificationToConditionListAnd(
				richiedenteSpecification.contains(codFiscalePIva, "codiceFiscalePartitaIva"), conditionsAnd);

		return richiedenteRepository.findAll(conditionsAnd);
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<Richiedente> searchRichiedentiByCodiceFiscale(String codFiscalePIva) {

		Specification<Richiedente> conditions = Specification.where(richiedenteSpecification.inizialize());

		conditions = richiedenteSpecification.addSpecificationToConditionListAnd(
				richiedenteSpecification.contains(codFiscalePIva, "codiceFiscalePartitaIva"), conditions);

		return richiedenteRepository.findAll(conditions);
	}

	@Override
	public void updateRichiedenteRettifica(Richiedente richiedente, RichiedenteRettificaDTO richiedenteRettifica) {

		richiedente.setNome(richiedenteRettifica.getNome());
		richiedente.setCognome(richiedenteRettifica.getCognome());
		richiedente.setDenominazione(richiedenteRettifica.getDenominazione());
		richiedente.setCodiceFiscalePartitaIva(richiedenteRettifica.getCodiceFiscalePartitaIva());
		richiedente.setDataDiNascita(richiedenteRettifica.getDataDiNascita());
		richiedente.setComuneDiNascita(richiedenteRettifica.getComuneDiNascita());
		richiedente.setProvinciaDiNascita(StringUtils.isNotBlank(richiedenteRettifica.getProvinciaDiNascita())
				? richiedenteRettifica.getProvinciaDiNascita().toUpperCase()
				: null);
		richiedente.setNazionalita(richiedenteRettifica.getNazionalita());
		richiedente.setCitta(richiedenteRettifica.getCitta());
		richiedente.setIndirizzo(richiedenteRettifica.getIndirizzo());
		richiedente.setCivico(richiedenteRettifica.getCivico());
		richiedente.setProvincia(StringUtils.isNotBlank(richiedenteRettifica.getProvincia())
				? richiedenteRettifica.getProvincia().toUpperCase()
				: null);
		richiedente.setCap(richiedenteRettifica.getCap());
		richiedente.setRecapitoTelefonico(richiedenteRettifica.getRecapitoTelefonico());
		richiedente.setEmail(richiedenteRettifica.getEmail());
		richiedente.setNumeroDocumentoAllegato(richiedenteRettifica.getNumeroDocumentoAllegato());
		richiedente.setAmministrazioneDocumentoAllegato(richiedenteRettifica.getAmministrazioneDocumentoAllegato());

		if (richiedenteRettifica.getIdTipoDocumentoAllegato() != null) {
			TipoDocumentoAllegato tipoDocumentoAllegato = tipoDocumentoAllegatoRepository
					.findById(richiedenteRettifica.getIdTipoDocumentoAllegato())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID tipo documento allegato non presente nel sistema",
							richiedenteRettifica.getIdTipoDocumentoAllegato()));
			richiedente.setTipoDocumentoAllegato(tipoDocumentoAllegato);
		}

		richiedenteRepository.save(richiedente);
	}

	/**
	 * Effettua le operazioni di inserimento/modifica Richiedente
	 * 
	 * @param richiedente
	 * @param richiedenteRequest
	 */
	private void performInsertEditRichiedente(Richiedente richiedente, RichiedenteDTO richiedenteRequest) {

		// quando viene inserita la denominazione, i dati del documento di
		// riconoscimento non devono essere inseriti
		if (StringUtils.isNotBlank(richiedente.getDenominazione())
				&& (richiedenteRequest.getIdTipoDocumentoAllegato() != null
						|| richiedente.getNumeroDocumentoAllegato() != null
						|| richiedente.getAmministrazioneDocumentoAllegato() != null)) {
			throw new BusinessException(ErrorCode.E7,
					"Errore: quando la regione sociale/denominazione è presente non vanno inseriti i dati del documento di riconoscimento");
		}

		TipoRuoloRichiedente tipoRuoloRichiedente = tipoRuoloRichiedenteRepository
				.findById(richiedenteRequest.getIdTipoRuoloRichiedente())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID tipo ruolo richiedente non presente nel sistema",
						richiedenteRequest.getIdTipoRuoloRichiedente()));

		richiedente.setTipoRuoloRichiedente(tipoRuoloRichiedente);

		if (richiedenteRequest.getIdTipoDocumentoAllegato() != null) {
			TipoDocumentoAllegato tipoDocumentoAllegato = tipoDocumentoAllegatoRepository
					.findById(richiedenteRequest.getIdTipoDocumentoAllegato())
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID tipo documento allegato non presente nel sistema",
							richiedenteRequest.getIdTipoDocumentoAllegato()));
			richiedente.setTipoDocumentoAllegato(tipoDocumentoAllegato);
		}

		richiedenteRepository.save(richiedente);
	}

	/**
	 * Effettua il set dei campi del richiedente aggiornati
	 * 
	 * @param richiedente
	 * @param dto
	 */
	private void buildRichiedente(Richiedente richiedente, RichiedenteDTO dto) {

		richiedente.setNome(dto.getNome());
		richiedente.setCognome(dto.getCognome());
		richiedente.setDenominazione(dto.getDenominazione());
		richiedente.setCodiceFiscalePartitaIva(dto.getCodiceFiscalePartitaIva());
		richiedente.setDataDiNascita(dto.getDataDiNascita());
		richiedente.setComuneDiNascita(dto.getComuneDiNascita());
		richiedente.setProvinciaDiNascita(
				StringUtils.isNotBlank(dto.getProvinciaDiNascita()) ? dto.getProvinciaDiNascita().toUpperCase() : null);
		richiedente.setNazionalita(dto.getNazionalita());
		richiedente.setCitta(dto.getCitta());
		richiedente.setIndirizzo(dto.getIndirizzo());
		richiedente.setCivico(dto.getCivico());
		richiedente.setProvincia(StringUtils.isNotBlank(dto.getProvincia()) ? dto.getProvincia().toUpperCase() : null);
		richiedente.setCap(dto.getCap());
		richiedente.setRecapitoTelefonico(dto.getRecapitoTelefonico());
		richiedente.setEmail(dto.getEmail());
		richiedente.setNumeroDocumentoAllegato(dto.getNumeroDocumentoAllegato());
		richiedente.setAmministrazioneDocumentoAllegato(dto.getAmministrazioneDocumentoAllegato());
		richiedente.setQualitaRuolo(dto.getQualitaRuolo());
		richiedente.setDescrizioneRuolo(dto.getDescrizioneRuolo());
		if (dto.getFlagFirmatario() != null) {
			richiedente.setFlagFirmatario(dto.getFlagFirmatario());
		}
		if (dto.getFlagDestinatario() != null) {
			richiedente.setFlagDestinatario(dto.getFlagDestinatario());
		}
	}

}
