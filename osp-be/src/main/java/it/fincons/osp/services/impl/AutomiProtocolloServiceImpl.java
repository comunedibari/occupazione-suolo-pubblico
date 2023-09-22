package it.fincons.osp.services.impl;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.AutomiProtocolloDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.AutomiProtocolloMapper;
import it.fincons.osp.model.AutomiProtocollo;
import it.fincons.osp.model.Municipio;
import it.fincons.osp.repository.AutomiProtocolloRepository;
import it.fincons.osp.repository.MunicipioRepository;
import it.fincons.osp.repository.UtenteRepository;
import it.fincons.osp.services.AutomiProtocolloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AutomiProtocolloServiceImpl implements AutomiProtocolloService {

	@Autowired
	private AutomiProtocolloRepository automiProtocolloRepository;

	@Autowired
	private MunicipioRepository municipioRepository;

	@Autowired
	UtenteRepository utenteRepository;

	@Autowired
	private AutomiProtocolloMapper automiProtocolloMapper;

	@Override
	@LogEntryExit
	public List<AutomiProtocolloDTO> getAllAutomiProtocolloDTO() {
		return automiProtocolloRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream().map(automiProtocolloMapper::entityToDto)
				.collect(Collectors.toList());
	}

	@Override
	public AutomiProtocolloDTO getAutomaProtocolloDTOByMunicipioId(Integer municipioId) {
		Municipio municipio = municipioRepository.findById(municipioId)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID municipio non presente nel sistema",
						municipioId));

		AutomiProtocollo automa= automiProtocolloRepository.findAutomaProtocolloByMunicipioId(municipioId)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,"Errore: nessun automa presente nel sistema per il municipio ", municipioId));


		return automiProtocolloMapper.entityToDto(automa);

	}

	@Override
	public AutomiProtocolloDTO getAutomaProtocolloDTOByid(Long automaId) {
		AutomiProtocollo automa= automiProtocolloRepository.findById(automaId)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,"Errore: nessun automa presente nel sistema per l'id ", automaId));

		return automiProtocolloMapper.entityToDto(automa);
	}

	@LogEntryExit(showArgs = true)
	@Transactional
	@Override
	public AutomiProtocolloDTO update(AutomiProtocolloDTO automiProtocolloDTO) {
		AutomiProtocollo automa= automiProtocolloRepository.findById(automiProtocolloDTO.getId())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,"Errore: nessun automa presente nel sistema per l'id ", automiProtocolloDTO.getId()));

		AutomiProtocollo alreadyPresentAutoma = automiProtocolloRepository.findAutomaProtocolloByUoId(automiProtocolloDTO.getUoId()).orElseGet(() -> null);

		if(alreadyPresentAutoma != null && alreadyPresentAutoma.getId() != automiProtocolloDTO.getId() ) {
			throw new BusinessException(ErrorCode.E1, "Errore: L’U.O. è già utilizzato");
		}

		if(!automa.getUoId().equals(automiProtocolloDTO.getUoId())) {

			utenteRepository.findAll().forEach(utente ->{
				if (!utente.isFlagEliminato()&&automa.getUoId().equals(utente.getUoId())) {
					utente.setUoId(automiProtocolloDTO.getUoId());
					utenteRepository.save(utente);
				}
			});
		}

		automa.setUoId(automiProtocolloDTO.getUoId());
		automa.setDenominazione(automiProtocolloDTO.getDenominazione());
		automa.setLabel(automiProtocolloDTO.getLabel());
		automa.setDataModifica(LocalDateTime.now());

		automiProtocolloRepository.save(automa);

		return automiProtocolloMapper.entityToDto(automa);
	}

	@Override
	public AutomiProtocolloDTO getAutomaProtocolloDTOByUoid(String uoid) {

		AutomiProtocollo automa= automiProtocolloRepository.findAutomaProtocolloByUoId(uoid)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,"Errore: nessun automa presente nel sistema per l'uoid ", uoid));


		return automiProtocolloMapper.entityToDto(automa);
	}
}
