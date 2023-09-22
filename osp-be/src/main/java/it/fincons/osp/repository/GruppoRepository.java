package it.fincons.osp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.Gruppo;

public interface GruppoRepository extends JpaRepository<Gruppo, Integer> {
	
	Optional<Gruppo> findByDescrizione(String descrizione);

	List<Gruppo> findByFlagGestInvioMailDeterminaTrue();
}
