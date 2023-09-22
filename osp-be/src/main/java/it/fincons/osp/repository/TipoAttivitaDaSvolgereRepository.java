package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.TipoAttivitaDaSvolgere;

public interface TipoAttivitaDaSvolgereRepository extends JpaRepository<TipoAttivitaDaSvolgere, Integer> {

	Optional<TipoAttivitaDaSvolgere> findByDescrizione(String descrizione);

}
