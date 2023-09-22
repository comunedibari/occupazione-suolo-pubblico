package it.fincons.ospscheduler.repository;

import it.fincons.ospscheduler.model.Pratica;
import it.fincons.ospscheduler.model.PraticaSearch;
import it.fincons.ospscheduler.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PraticaRepository extends JpaRepository<Pratica, Long> {
    @Query("SELECT new it.fincons.ospscheduler.model.PraticaSearch(" +
            "pratica.id, " +
            "pratica.municipio.id, " +
            "pratica.firmatario.tipoRuoloRichiedente.id, " +
            "pratica.dataInserimento, " +
            "pratica.datiRichiesta.dataInizioOccupazione, " +
            "pratica.datiRichiesta.dataScadenzaOccupazione, " +
            "pratica.datiRichiesta.oraInizioOccupazione, " +
            "pratica.datiRichiesta.oraScadenzaOccupazione, " +
            "pratica.datiRichiesta.attivitaDaSvolgere.id, " +
            "pratica.statoPratica.id, " +
            "pratica.tipoProcesso.id, " +
            "pratica.datiRichiesta.coordUbicazioneTemporanea, " +
            "pratica.datiRichiesta.coordUbicazioneDefinitiva," +
            "pratica.utentePresaInCarico) " +
            "FROM Pratica pratica " +
            "left join pratica.datiRichiesta datiRichiesta " +
            "left join pratica.utentePresaInCarico utentePresaInCarico ")
    List<PraticaSearch> findPratiche();
}
