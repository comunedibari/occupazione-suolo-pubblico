package it.fincons.osp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.Gruppo;
import it.fincons.osp.model.RichiestaParere;
import it.fincons.osp.model.Utente;

public interface RichiestaParereRepository extends JpaRepository<RichiestaParere, Long> {

	List<RichiestaParere> findByUtenteRichiedente(Utente utenteRichiedente);

	List<RichiestaParere> findByGruppoDestinatarioParereAndFlagInseritaRispostaFalse(Gruppo gruppoDestinatarioParere);

	List<RichiestaParere> findByGruppoDestinatarioParereAndNotaRichiestaParereAndFlagInseritaRispostaFalse(
			Gruppo gruppoDestinatarioParere, String notaRichiestaParere);

	List<RichiestaParere> findAllByCodiceProtocolloLike(String codiceProtocollo);
}
