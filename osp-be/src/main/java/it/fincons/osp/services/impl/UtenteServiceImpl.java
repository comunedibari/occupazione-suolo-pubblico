package it.fincons.osp.services.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import it.fincons.osp.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.UtenteDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.UtenteMapper;
import it.fincons.osp.payload.request.ModificaPasswordRequest;
import it.fincons.osp.payload.request.UtenteCommonRequest;
import it.fincons.osp.payload.request.UtenteEditRequest;
import it.fincons.osp.payload.request.UtenteInsertRequest;
import it.fincons.osp.repository.GruppoRepository;
import it.fincons.osp.repository.IntegrazioneRepository;
import it.fincons.osp.repository.MunicipioRepository;
import it.fincons.osp.repository.ParereRepository;
import it.fincons.osp.repository.PraticaRepository;
import it.fincons.osp.repository.RichiestaIntegrazioneRepository;
import it.fincons.osp.repository.RichiestaParereRepository;
import it.fincons.osp.repository.UtenteRepository;
import it.fincons.osp.repository.specification.UtenteSpecification;
import it.fincons.osp.services.UtenteService;
import it.fincons.osp.utils.Constants;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UtenteServiceImpl implements UtenteService {

	@Autowired
	UtenteRepository utenteRepository;

	@Autowired
	GruppoRepository gruppoRepository;

	@Autowired
	MunicipioRepository municipioRepository;

	@Autowired
	PraticaRepository praticaRepository;

	@Autowired
	IntegrazioneRepository integrazioneRepository;

	@Autowired
	ParereRepository parereRepository;

	@Autowired
	RichiestaIntegrazioneRepository richiestaIntegrazioneRepository;

	@Autowired
	RichiestaParereRepository richiestaParereRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	UtenteMapper userMapper;

	@Autowired
	UtenteSpecification utenteSpecification;

	@Override
	@LogEntryExit
	public Utente updateLastLoginByUsername(String username) {
		Utente utente = utenteRepository.findByUsernameAndFlagEliminatoFalse(username).orElseThrow(() -> new BusinessException(ErrorCode.E3,
				"Errore: username utente non presente nel sistema", username));

		utente.setLastLogin(LocalDateTime.now().withNano(0));
		utenteRepository.save(utente);

		return utente;
	}

	@Override
	@LogEntryExit
	public String generateNewPassword(Utente utente) {

		Double rand = Math.random();
		String password = rand.toString().substring(2);

		utente.setPassword(encoder.encode(password));
		utenteRepository.save(utente);

		return password;
	}

	@Override
	@LogEntryExit
	public Page<UtenteDTO> getUtenti(Long id, String username, String nome, String cognome, Pageable pageable) {
		Specification<Utente> conditions = Specification.where(utenteSpecification.isNotEliminato());

		conditions = utenteSpecification.addSpecificationToConditionListAnd(utenteSpecification.equals(id, "id"),
				conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.contains(utenteRicerca.getGruppo(), "gruppo"), conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.contains(utenteRicerca.getMunicipio(), "municipio"), conditions);
		conditions = utenteSpecification
				.addSpecificationToConditionListAnd(utenteSpecification.contains(username, "username"), conditions);
		conditions = utenteSpecification.addSpecificationToConditionListAnd(utenteSpecification.contains(nome, "nome"),
				conditions);
		conditions = utenteSpecification
				.addSpecificationToConditionListAnd(utenteSpecification.contains(cognome, "cognome"), conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.contains(utenteRicerca.getSesso(), "sesso"), conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.equals(utenteRicerca.getDataDiNascita(), "dataDiNascita"), conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.contains(utenteRicerca.getLuogoDiNascita(), "luogoDiNascita"), conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.contains(utenteRicerca.getProvinciaDiNascita(), "provinciaDiNascita"), conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.contains(utenteRicerca.getCodiceFiscale(), "codiceFiscale"), conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.contains(utenteRicerca.getRagioneSociale(), "ragioneSociale"), conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.contains(utenteRicerca.getEmail(), "email"), conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.contains(utenteRicerca.getNumTel(), "numTel"), conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.equals(utenteRicerca.getEnabled(), "enabled"), conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.equalsDateTimeWithoutSecondsAndMilli(utenteRicerca.getDateCreated(), "dateCreated"),
//				conditions);
//		conditions = utenteSpecification.addSpecificationToConditionList(
//				utenteSpecification.equalsDateTimeWithoutSecondsAndMilli(utenteRicerca.getLastLogin(), "lastLogin"),
//				conditions);

		return utenteRepository.findAll(conditions, pageable).map(userMapper::entityToDto);
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<UtenteDTO> getUtentiGruppoMunicipio(Integer idGruppo, Integer idMunicipio) {
		Gruppo gruppo = gruppoRepository.findById(idGruppo).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: id gruppo non presente nel sistema", idGruppo));

		Municipio municipio = municipioRepository.findById(idMunicipio)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: id municipio non presente nel sistema",
						idMunicipio));

		List<Gruppo> listaGruppi = List.of(gruppo);
		List<Municipio> listaMunicipo = List.of(municipio);

		return utenteRepository
				.findDistinctByGruppoInAndMunicipiInAndEnabledTrueAndFlagEliminatoFalse(listaGruppi, listaMunicipo)
				.stream().map(userMapper::entityToDto).collect(Collectors.toList());
	}

	@Override
	@LogEntryExit(showArgs = true)
	public List<UtenteDTO> getUtentiGruppo(Integer idGruppo) {
		Gruppo gruppo = gruppoRepository.findById(idGruppo).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: id gruppo non presente nel sistema", idGruppo));

		List<Gruppo> listaGruppi = List.of(gruppo);

		return utenteRepository.findDistinctByGruppoInAndEnabledTrueAndFlagEliminatoFalse(listaGruppi).stream()
				.map(userMapper::entityToDto).collect(Collectors.toList());
	}

	@Override
	@LogEntryExit
	public UtenteDTO getUtente(Long id) {
		Utente utente = utenteRepository.findById(id).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: id utente non presente nel sistema", id));

		if (utente.isFlagEliminato()) {
			throw new BusinessException(ErrorCode.E4,
					"Errore: recupero dati non consentito in quanto l'utente risulta eliminato");
		}

		return userMapper.entityToDto(utente);
	}

	@Override
	@LogEntryExit(showArgs = true)
	public UtenteDTO insertUtente(UtenteInsertRequest utenteRequest) {
		boolean skipCheckConcessionario=utenteRequest.getSkipCheckConcessionario()==null||"".equals(utenteRequest.getSkipCheckConcessionario())?false:Boolean.valueOf(utenteRequest.getSkipCheckConcessionario());

		if(
				Constants.ID_GRUPPO_CONCESSIONARIO.intValue()==utenteRequest.getIdGruppo().intValue()
				&& !skipCheckConcessionario
		){

			Gruppo gruppo = gruppoRepository.findById(utenteRequest.getIdGruppo()).orElseThrow(
					() -> new BusinessException(ErrorCode.E1, "Errore: id gruppo non presente nel sistema", utenteRequest.getIdGruppo()));

			List<Utente> listaUtenti = utenteRepository.findByGruppoAndEnabledTrueAndFlagEliminatoFalse(gruppo);

			if(listaUtenti!=null&&listaUtenti.size()>0){
				List<String> usernemeList = listaUtenti.stream().map(u -> u.getUsername()).collect(Collectors.toList());
				throw new BusinessException(ErrorCode.E32, usernemeList.stream().collect(Collectors.joining(", ")));
			}
		} else if (Constants.ID_GRUPPO_CONCESSIONARIO.intValue()==utenteRequest.getIdGruppo().intValue()
				&& skipCheckConcessionario) {
			Gruppo gruppo = gruppoRepository.findById(utenteRequest.getIdGruppo()).orElseThrow(
					() -> new BusinessException(ErrorCode.E1, "Errore: id gruppo non presente nel sistema", utenteRequest.getIdGruppo()));

			List<Utente> listaUtenti = utenteRepository.findByGruppoAndEnabledTrueAndFlagEliminatoFalse(gruppo);

			if(listaUtenti != null && listaUtenti.size() > 0) {
				return this.performInserisciModificaUtente(utenteRequest, true, utenteRequest.getPassword(), null, true, listaUtenti);
			}
		}

		return this.performInserisciModificaUtente(utenteRequest, true, utenteRequest.getPassword(), null, true, null);
	}

	@Override
	@LogEntryExit(showArgs = true)
	public void editUtente(UtenteEditRequest utenteRequest) {
		this.performInserisciModificaUtente(utenteRequest, false, null, utenteRequest.getId(),
				utenteRequest.getEnabled(), null);
	}

	@Override
	@LogEntryExit
	public void editPassword(ModificaPasswordRequest modificaPasswordRequest) {
		Utente utente = utenteRepository.findByUsernameAndFlagEliminatoFalse(modificaPasswordRequest.getUsername())
				.orElseThrow(() -> new BusinessException(ErrorCode.E3,
						"Errore: username utente non presente nel sistema", modificaPasswordRequest.getUsername()));

		if (!encoder.matches(modificaPasswordRequest.getOldPassword(), utente.getPassword())) {
			throw new BusinessException(ErrorCode.E5, "La vecchia password inserita è errata");
		}

		utente.setPassword(encoder.encode(modificaPasswordRequest.getPassword()));
		utenteRepository.save(utente);

		log.info("Password aggiornata correttamente");
	}

	/**
	 * Effettua le operazioni di inserimento/modifica utente
	 * 
	 * @param utenteRequest
	 * @param isNew
	 * @param password
	 * @param id
	 * @param enabled
	 * @return le informazioni sull'utente inserito/modificato
	 */
	@Transactional
	@LogEntryExit
	private UtenteDTO performInserisciModificaUtente(UtenteCommonRequest utenteRequest, boolean isNew, String password,
			Long id, boolean enabled, List<Utente> listaUtentiDaDisabilitare) {

		// Creazione nuovo utente
		Utente utente;
		if (isNew) {
			utente = new Utente();
			
			if(password.length() < Constants.PASSWORD_LENGTH_MIN) {
				throw new BusinessException(ErrorCode.E23, "La password deve essere lunga almeno " + Constants.PASSWORD_LENGTH_MIN + " caratteri.");
			} else if(password.length() > Constants.PASSWORD_LENGTH_MAX) {
				throw new BusinessException(ErrorCode.E24, "La password non deve essere lunga più di " + Constants.PASSWORD_LENGTH_MAX + " caratteri.");
			}
			
			utente.setPassword(encoder.encode(password));
			utente.setDateCreated(LocalDateTime.now().withNano(0));
		} else {
			utente = utenteRepository.findById(id).orElseThrow(
					() -> new BusinessException(ErrorCode.E1, "Errore: id utente non presente nel sistema", id));

			if (utente.isFlagEliminato()) {
				throw new BusinessException(ErrorCode.E2,
						"Errore: modifica non consentita in quanto l'utente risulta eliminato");
			}
		}

		if ((isNew || !utente.getUsername().equals(utenteRequest.getUsername()))
				&& utenteRepository.existsByUsernameAndFlagEliminatoFalse(utenteRequest.getUsername())) {
			throw new BusinessException(ErrorCode.E7, "Username già in uso nel sistema", utenteRequest.getUsername());
		}

		if ((isNew || !utente.getCodiceFiscale().equals(utenteRequest.getCodiceFiscale()))
				&& utenteRepository.existsByCodiceFiscaleAndFlagEliminatoFalse(utenteRequest.getCodiceFiscale())) {
			throw new BusinessException(ErrorCode.E7, "Codice fiscale già in uso nel sistema",
					utenteRequest.getCodiceFiscale());
		}

		Integer idGruppo = utenteRequest.getIdGruppo();
		Gruppo gruppo = gruppoRepository.findById(idGruppo).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID gruppo non presente nel sistema", idGruppo));

		Set<Municipio> municipi = new HashSet<>();

		if (utenteRequest.getIdsMunicipi() != null && !utenteRequest.getIdsMunicipi().isEmpty()) {

			for (Integer idMunicipio : utenteRequest.getIdsMunicipi()) {
				municipi.add(
						municipioRepository.findById(idMunicipio).orElseThrow(() -> new BusinessException(ErrorCode.E1,
								"Errore: ID municipio non presente nel sistema", idMunicipio)));
			}
		}

		// informazioni obbligatorie
		utente.setUsername(utenteRequest.getUsername());
		utente.setEmail(utenteRequest.getEmail());
		utente.setCodiceFiscale(utenteRequest.getCodiceFiscale());
		utente.setGruppo(gruppo);
		utente.setEnabled(enabled);

		// informazioni facoltative
		utente.setMunicipi(municipi);
		utente.setNome(utenteRequest.getNome());
		utente.setCognome(utenteRequest.getCognome());
		utente.setSesso(utenteRequest.getSesso());
		utente.setDataDiNascita(utenteRequest.getDataDiNascita());
		utente.setLuogoDiNascita(utenteRequest.getLuogoDiNascita());
		if (StringUtils.isNotBlank(utenteRequest.getProvinciaDiNascita())) {
			utente.setProvinciaDiNascita(utenteRequest.getProvinciaDiNascita().toUpperCase());
		}
		utente.setRagioneSociale(utenteRequest.getRagioneSociale());
		utente.setNumTel(utenteRequest.getNumTel());
		utente.setUoId(utenteRequest.getUoId());
		utente.setIndirizzo(utenteRequest.getIndirizzo());

		utenteRepository.save(utente);
		log.info(isNew ? "Utente inserito correttamente" : "Dati dell'utente aggiornati correttamente");

		if(listaUtentiDaDisabilitare != null && listaUtentiDaDisabilitare.size() > 0) {
			listaUtentiDaDisabilitare.stream().forEach(u -> {
				disableUtente(u.getId());
			});
		}

		return userMapper.entityToDto(utente);
	}

	@Override
	@Transactional
	@LogEntryExit(showArgs = true)
	public void deleteUtente(Long id) {
		Utente utente = utenteRepository.findById(id).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", id));

		if (utente.isFlagEliminato()) {
			throw new BusinessException(ErrorCode.E6, "Errore: l'utente è già stato eliminato");
		}

		utente.setFlagEliminato(true);
		utente.setDataEliminazione(LocalDateTime.now().withNano(0));

		log.info("L'utente è stato eliminato");
	}

	@Override
	@Transactional
	@LogEntryExit(showArgs = true)
	public void disableUtente(Long id) {
		Utente utente = utenteRepository.findById(id).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema", id));

		if (utente.isEnabled()) {
			utente.setEnabled(false);
		}

		utenteRepository.save(utente);

		log.info("L'utente " + utente.getId() + " è stato disabilitato");
	}
}
