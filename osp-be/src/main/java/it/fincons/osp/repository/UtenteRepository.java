package it.fincons.osp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.fincons.osp.model.Gruppo;
import it.fincons.osp.model.Municipio;
import it.fincons.osp.model.Utente;

@Repository
public interface UtenteRepository extends PagingAndSortingRepository<Utente, Long>, JpaSpecificationExecutor<Utente> {

	boolean existsByCodiceFiscaleAndFlagEliminatoFalse(String codiceFiscale);

	Page<Utente> findAll(Pageable pageable);

	List<Utente> findDistinctByGruppoInAndMunicipiInAndEnabledTrueAndFlagEliminatoFalse(List<Gruppo> gruppo,
			List<Municipio> municipi);

	List<Utente> findDistinctByGruppoInAndEnabledTrueAndFlagEliminatoFalse(List<Gruppo> gruppo);

	@Query(
		" SELECT DISTINCT u " +
		" FROM Utente u LEFT JOIN u.municipi m " +
		" WHERE u.enabled = TRUE " +
			" AND u.flagEliminato = FALSE " +
			" AND u.gruppo IN (:gruppi) " +
			" AND (m IN (:municipi) OR u.municipi IS EMPTY) "
	)
	List<Utente> findUtentiAttiviByGruppiAndMunicipi(@Param("gruppi") List<Gruppo> gruppi,
			@Param("municipi") List<Municipio> municipi);

	List<Utente> findByGruppo(Gruppo gruppo);
	
	Optional<Utente> findByUsernameAndFlagEliminatoFalse(String username);
	
	boolean existsByUsernameAndFlagEliminatoFalse(String username);

	List<Utente> findByUsername(String username);

	List<Utente> findByGruppoAndEnabledTrueAndFlagEliminatoFalse(Gruppo id);
}
