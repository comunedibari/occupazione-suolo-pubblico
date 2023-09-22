package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.StatoPratica;

public interface StatoPraticaRepository extends JpaRepository<StatoPratica, Integer> {

	Optional<StatoPratica> findByDescrizione(String descrizione);

}
