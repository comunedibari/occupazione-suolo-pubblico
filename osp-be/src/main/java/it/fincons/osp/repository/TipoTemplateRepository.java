package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.TipoTemplate;

public interface TipoTemplateRepository extends JpaRepository<TipoTemplate, Integer> {

	Optional<TipoTemplate> findByDescrizione(String descrizione);

}
