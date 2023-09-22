package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.TipoAllegato;

public interface TipoAllegatoRepository extends JpaRepository<TipoAllegato, Integer> {

	Optional<TipoAllegato> findByDescrizione(String descrizione);

}
