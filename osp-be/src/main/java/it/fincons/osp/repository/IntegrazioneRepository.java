package it.fincons.osp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.Integrazione;
import it.fincons.osp.model.Utente;

public interface IntegrazioneRepository extends JpaRepository<Integrazione, Long> {

	List<Integrazione> findByUtenteIntegrazione(Utente utenteIntegrazione);

}
