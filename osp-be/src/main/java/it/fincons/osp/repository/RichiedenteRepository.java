package it.fincons.osp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import it.fincons.osp.model.Richiedente;

public interface RichiedenteRepository extends JpaRepository<Richiedente, Long>, JpaSpecificationExecutor<Richiedente> {

}
