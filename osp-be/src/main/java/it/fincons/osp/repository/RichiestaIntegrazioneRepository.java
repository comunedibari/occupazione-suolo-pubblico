package it.fincons.osp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.RichiestaIntegrazione;
import it.fincons.osp.model.Utente;
import org.springframework.data.jpa.repository.Query;

public interface RichiestaIntegrazioneRepository extends JpaRepository<RichiestaIntegrazione, Long> {

	List<RichiestaIntegrazione> findByUtenteRichiedente(Utente utenteRichiedente);

	List<RichiestaIntegrazione> findAllByCodiceProtocolloLike(String codiceProtocollo);
}
