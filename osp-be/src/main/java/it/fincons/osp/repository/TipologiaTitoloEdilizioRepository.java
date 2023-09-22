package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.TipologiaTitoloEdilizio;

public interface TipologiaTitoloEdilizioRepository extends JpaRepository<TipologiaTitoloEdilizio, Integer> {

	Optional<TipologiaTitoloEdilizio> findByDescrizione(String descrizione);

}
