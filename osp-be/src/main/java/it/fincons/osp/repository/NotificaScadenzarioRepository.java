package it.fincons.osp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.NotificaScadenzario;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.Utente;

public interface NotificaScadenzarioRepository extends JpaRepository<NotificaScadenzario, Long> {

	public Long deleteByPratica(Pratica pratica);

	public List<NotificaScadenzario> findByUtenteAndPratica_UtentePresaInCaricoNotNull(Utente utente);

	public long countByUtenteAndPratica_UtentePresaInCaricoNotNull(Utente utente);
}
