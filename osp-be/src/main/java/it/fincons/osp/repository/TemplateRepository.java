package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.Template;
import it.fincons.osp.model.TipoTemplate;

public interface TemplateRepository extends JpaRepository<Template, Integer> {

	Optional<Template> findByTipoTemplate(TipoTemplate tipoTemplate);

}
