package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.TipoManufatto;

public interface TipoManufattoRepository extends JpaRepository<TipoManufatto, Integer> {

	Optional<TipoManufatto> findByDescrizione(String descrizione);

}
