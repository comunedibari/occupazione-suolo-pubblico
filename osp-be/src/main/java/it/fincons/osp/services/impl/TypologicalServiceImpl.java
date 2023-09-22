package it.fincons.osp.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.GruppoDTO;
import it.fincons.osp.dto.TipoAllegatoDTO;
import it.fincons.osp.dto.TipoAllegatoGruppoStatoProcessoDTO;
import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.dto.TypologicalFlagTestoLiberoDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.GruppoMapper;
import it.fincons.osp.mapper.MunicipioMapper;
import it.fincons.osp.mapper.StatoPraticaMapper;
import it.fincons.osp.mapper.TipoAllegatoGruppoStatoProcessoMapper;
import it.fincons.osp.mapper.TipoAllegatoMapper;
import it.fincons.osp.mapper.TipoAttivitaDaSvolgereMapper;
import it.fincons.osp.mapper.TipoDocumentoAllegatoMapper;
import it.fincons.osp.mapper.TipoManufattoMapper;
import it.fincons.osp.mapper.TipoNotificaScadenzarioMapper;
import it.fincons.osp.mapper.TipoProcessoMapper;
import it.fincons.osp.mapper.TipoRuoloRichiedenteMapper;
import it.fincons.osp.mapper.TipoTemplateMapper;
import it.fincons.osp.mapper.TipologiaTitoloEdilizioMapper;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.StatoPratica;
import it.fincons.osp.model.TipoAllegatoGruppoStatoProcesso;
import it.fincons.osp.model.TipoProcesso;
import it.fincons.osp.model.Utente;
import it.fincons.osp.repository.GruppoRepository;
import it.fincons.osp.repository.MunicipioRepository;
import it.fincons.osp.repository.PraticaRepository;
import it.fincons.osp.repository.StatoPraticaRepository;
import it.fincons.osp.repository.TipoAllegatoGruppoStatoProcessoRepository;
import it.fincons.osp.repository.TipoAllegatoRepository;
import it.fincons.osp.repository.TipoAttivitaDaSvolgereRepository;
import it.fincons.osp.repository.TipoDocumentoAllegatoRepository;
import it.fincons.osp.repository.TipoManufattoRepository;
import it.fincons.osp.repository.TipoNotificaScadenzarioRepository;
import it.fincons.osp.repository.TipoProcessoRepository;
import it.fincons.osp.repository.TipoRuoloRichiedenteRepository;
import it.fincons.osp.repository.TipoTemplateRepository;
import it.fincons.osp.repository.TipologiaTitoloEdilizioRepository;
import it.fincons.osp.repository.UtenteRepository;
import it.fincons.osp.services.TypologicalService;
import it.fincons.osp.utils.Constants;

@Service
public class TypologicalServiceImpl implements TypologicalService {

	@Autowired
	GruppoRepository gruppoRepository;

	@Autowired
	GruppoMapper gruppoMapper;

	@Autowired
	MunicipioRepository municipioRepository;

	@Autowired
	MunicipioMapper municipioMapper;

	@Autowired
	StatoPraticaRepository statoPraticaRepository;

	@Autowired
	StatoPraticaMapper statoPraticaMapper;

	@Autowired
	TipoProcessoRepository tipoProcessoRepository;

	@Autowired
	TipoProcessoMapper tipoProcessoMapper;

	@Autowired
	TipoAllegatoRepository tipoAllegatoRepository;

	@Autowired
	TipoAllegatoGruppoStatoProcessoRepository tipoAllegatoGruppoStatoProcessoRepository;

	@Autowired
	TipoAllegatoMapper tipoAllegatoMapper;

	@Autowired
	TipoAllegatoGruppoStatoProcessoMapper tipoAllegatoGruppoStatoProcessoMapper;

	@Autowired
	TipoRuoloRichiedenteRepository tipoRuoloRichiedenteRepository;

	@Autowired
	TipoRuoloRichiedenteMapper tipoRuoloRichiedenteMapper;

	@Autowired
	TipoAttivitaDaSvolgereRepository tipoAttivitaDaSvolgereRepository;

	@Autowired
	TipoAttivitaDaSvolgereMapper tipoAttivitaDaSvolgereMapper;

	@Autowired
	TipoDocumentoAllegatoRepository tipoDocumentoAllegatoRepository;

	@Autowired
	TipoDocumentoAllegatoMapper tipoDocumentoAllegatoMapper;

	@Autowired
	TipologiaTitoloEdilizioRepository tipologiaTitoloEdilizioRepository;

	@Autowired
	TipologiaTitoloEdilizioMapper tipologiaTitoloEdilizioMapper;

	@Autowired
	TipoManufattoRepository tipoManufattoRepository;

	@Autowired
	TipoManufattoMapper tipoManufattoMapper;

	@Autowired
	UtenteRepository utenteRepository;

	@Autowired
	PraticaRepository praticaRepository;

	@Autowired
	TipoTemplateRepository tipoTemplateRepository;

	@Autowired
	TipoTemplateMapper tipoTemplateMapper;

	@Autowired
	TipoNotificaScadenzarioRepository tipoNotificaScadenzarioRepository;

	@Autowired
	TipoNotificaScadenzarioMapper tipoNotificaScadenzarioMapper;

	@Override
	public List<GruppoDTO> getGruppi() {
		return gruppoRepository.findAll().stream().map(gruppoMapper::entityToDto).collect(Collectors.toList());
	}

	@Override
	public List<TypologicalDTO> getMunicipi() {
		return municipioRepository.findAll().stream().map(municipioMapper::entityToDto).collect(Collectors.toList());
	}

	@Override
	public List<TypologicalDTO> getStatiPratiche() {
		return statoPraticaRepository.findAll().stream().filter(
			el -> !el.getId().equals(Constants.ID_STATO_PRATICA_INSERITA_MODIFICA_DATE)
		).map(statoPraticaMapper::entityToDto)
			.collect(Collectors.toList());
	}

	@Override
	public List<TypologicalDTO> getTipiProcessi() {
		return tipoProcessoRepository.findAll().stream().map(tipoProcessoMapper::entityToDto)
				.collect(Collectors.toList());
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<TipoAllegatoGruppoStatoProcessoDTO> getTipiAllegati(Long idUtente, Long idPratica) {

		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		List<TipoAllegatoGruppoStatoProcesso> result = tipoAllegatoGruppoStatoProcessoRepository
				.findByStatoPraticaAndTipoProcessoAndGruppo(pratica.getStatoPratica(), pratica.getTipoProcesso(),
						utente.getGruppo());

		// se il firmatario e' anche il destinatario, allora rimuovo dalla lista degli
		// allegati il documento del destinatario
		if (pratica.getFirmatario().getTipoRuoloRichiedente().getId()
				.equals(Constants.ID_TIP_RUOLO_RICHIEDENTE_DESTINATARIO)) {
			result.removeIf(t -> t.getTipoAllegato().getId().equals(Constants.ID_TIPO_ALLEGATO_DOCUMENTO_DESTINATARIO));
		}

/*
		if (pratica.getFlagEsenzionePagamentoCUP()) {
			result.removeIf(t -> t.getTipoAllegato().getId().equals(Constants.ID_TIPO_ALLEGATO_ATTESTAZIONE_PAGAMENTO_MARCA_DA_BOLLO));
		}
*/


		//se flagEsenzioneMarcaDaBollo == true allora l'Attestazione pagamento marca da bollo non deve essere obbligatoria
		if(pratica.getDatiRichiesta().isFlagEsenzioneMarcaDaBollo()||pratica.isFlagEsenzionePagamentoCUP()){
			for(TipoAllegatoGruppoStatoProcesso tipoAllegatoGruppoStatoProcesso:result){

				if(pratica.getDatiRichiesta().isFlagEsenzioneMarcaDaBollo()) {
					if (Constants.ID_TIPO_ALLEGATO_ATTESTAZIONE_PAGAMENTO_MARCA_DA_BOLLO.intValue() == tipoAllegatoGruppoStatoProcesso.getTipoAllegato().getId().intValue()) {
						tipoAllegatoGruppoStatoProcesso.setFlagObbligatorio(false);
					}
					if (Constants.ID_TIPO_ALLEGATO_BOLLO.intValue() == tipoAllegatoGruppoStatoProcesso.getTipoAllegato().getId().intValue()) {
						tipoAllegatoGruppoStatoProcesso.setFlagObbligatorio(false);
					}
				}

				if(pratica.isFlagEsenzionePagamentoCUP()) {
					if (Constants.ID_TIPO_ALLEGATO_RICEVUTA_DI_PAGAMENTO_CUP.intValue() == tipoAllegatoGruppoStatoProcesso.getTipoAllegato().getId().intValue()) {
						tipoAllegatoGruppoStatoProcesso.setFlagObbligatorio(false);
					}
					if (Constants.ID_TIPO_ALLEGATO_ATTESTAZIONE_PAGAMENTO_CUP_CONCESSIONE.intValue() == tipoAllegatoGruppoStatoProcesso.getTipoAllegato().getId().intValue()) {
						tipoAllegatoGruppoStatoProcesso.setFlagObbligatorio(false);
					}

				}
			}
		}

		return result.stream().map(tipoAllegatoGruppoStatoProcessoMapper::entityToDto).collect(Collectors.toList());
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<TipoAllegatoGruppoStatoProcessoDTO> getTipiAllegatiByStatoAndProcesso(Long idUtente,
			Integer idStatoPratica, Integer idTipoProcesso) {

		Utente utente = utenteRepository.findById(idUtente).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", idUtente));

		StatoPratica statoPratica = statoPraticaRepository.findById(idStatoPratica)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID stato pratica non presente nel sistema", idStatoPratica));

		TipoProcesso tipoProcesso = tipoProcessoRepository.findById(idTipoProcesso)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID tipo processo non presente nel sistema", idTipoProcesso));

		List<TipoAllegatoGruppoStatoProcesso> result = tipoAllegatoGruppoStatoProcessoRepository
				.findByStatoPraticaAndTipoProcessoAndGruppo(statoPratica, tipoProcesso, utente.getGruppo());

		return result.stream().map(tipoAllegatoGruppoStatoProcessoMapper::entityToDto).collect(Collectors.toList());
	}

	@Override
	public List<TipoAllegatoDTO> getAllTipiAllegati() {
		return tipoAllegatoRepository.findAll().stream().map(tipoAllegatoMapper::entityToDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<TypologicalDTO> getTipiRuoliRichiedenti() {
		return tipoRuoloRichiedenteRepository.findAll(Sort.by("id")).stream().map(tipoRuoloRichiedenteMapper::entityToDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<TypologicalFlagTestoLiberoDTO> getTipiAttivitaDaSvolgere() {
		return tipoAttivitaDaSvolgereRepository.findAll().stream().map(tipoAttivitaDaSvolgereMapper::entityToDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<TypologicalDTO> getTipiDocumentiAllegati() {
		return tipoDocumentoAllegatoRepository.findAll().stream().map(tipoDocumentoAllegatoMapper::entityToDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<TypologicalFlagTestoLiberoDTO> getTipologieTitoliEdilizi() {
		return tipologiaTitoloEdilizioRepository.findAll().stream().map(tipologiaTitoloEdilizioMapper::entityToDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<TypologicalFlagTestoLiberoDTO> getTipiManufatti() {
		return tipoManufattoRepository.findAll().stream().map(tipoManufattoMapper::entityToDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<TypologicalDTO> getTipiTemplate() {
		return tipoTemplateRepository.findAll().stream().map(tipoTemplateMapper::entityToDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<TypologicalDTO> getTipiNotificheScadenzario() {
		return tipoNotificaScadenzarioRepository.findAll().stream().map(tipoNotificaScadenzarioMapper::entityToDto)
				.collect(Collectors.toList());
	}

}
