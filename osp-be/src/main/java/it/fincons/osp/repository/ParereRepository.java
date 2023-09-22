package it.fincons.osp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.Parere;
import it.fincons.osp.model.Utente;

public interface ParereRepository extends JpaRepository<Parere, Long> {

	List<Parere> findByUtenteParere(Utente utenteParere);

}
