package it.fincons.osp.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.fincons.osp.model.DatiRichiesta;

public interface DatiRichiestaRepository extends JpaRepository<DatiRichiesta, Long> {

	@Query("SELECT d FROM DatiRichiesta d WHERE d.pratica.statoPratica.id NOT IN (:listaIdStatiPraticaConclusivi) AND LOWER(d.nomeVia) = LOWER(:nomeVia) AND LOWER(d.numeroVia) = LOWER(:numeroVia) AND LOWER(d.localita) = LOWER(:localita) AND (d.coordUbicazioneTemporanea = :coordUbicazioneTemporanea OR d.coordUbicazioneDefinitiva = :coordUbicazioneDefinitiva) AND (d.dataInizioOccupazione <= :dataScadenzaOccupazione and d.dataScadenzaOccupazione >= :dataInizioOccupazione)")
	List<DatiRichiesta> findStessaUbicazione(
			@Param("listaIdStatiPraticaConclusivi") List<Integer> listaIdStatiPraticaConclusivi,
			@Param("nomeVia") String nomeVia, @Param("numeroVia") String numeroVia, @Param("localita") String localita,
			@Param("dataInizioOccupazione") LocalDate dataInizioOccupazione,
			@Param("dataScadenzaOccupazione") LocalDate dataScadenzaOccupazione,
			@Param("coordUbicazioneTemporanea") Geometry coordUbicazioneTemporanea,
			@Param("coordUbicazioneDefinitiva") Geometry coordUbicazioneDefinitiva);

	@Query(nativeQuery = true, value = "select count(*) from t_dati_richiesta dr WHERE ST_Intersects(dr.test_polygon, :polygon) = true")
	Integer countOverlapping(@Param("polygon") Polygon polygon);

	@Query(nativeQuery = true, value = "select * from t_dati_richiesta dr join t_pratica p on(dr.id = p.id_dati_richiesta) where p.id_stato_pratica not in (:listaIdStatiPraticaConclusivi) and (dr.data_inizio_occupazione <= :dataScadenzaOccupazione and dr.data_scadenza_occupazione >= :dataInizioOccupazione) and (ST_Intersects(dr.coord_ubicazione_temporanea, :coordUbicazioneDefinitiva) = true or ST_Intersects(dr.coord_ubicazione_definitiva, :coordUbicazioneDefinitiva) = true)")
	List<DatiRichiesta> countOverlappingUbicazione(
			@Param("listaIdStatiPraticaConclusivi") List<Integer> listaIdStatiPraticaConclusivi,
			@Param("dataInizioOccupazione") LocalDate dataInizioOccupazione,
			@Param("dataScadenzaOccupazione") LocalDate dataScadenzaOccupazione,
			@Param("coordUbicazioneDefinitiva") Geometry coordUbicazioneDefinitiva);

	@Query("SELECT d FROM DatiRichiesta d WHERE lower(d.ubicazioneOccupazione) like lower(:ubicazioneOccupazione)")
	List<DatiRichiesta> findByUbicazioneOccupazione(String ubicazioneOccupazione);
}
