package it.fincons.ospscheduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@ToString
@AllArgsConstructor
public class PraticaSearch {
    private Long id;
    private Integer idMunicipio;
    private Integer idTipoRuoloRichiedente;
    private LocalDateTime dataInserimento;
    private LocalDate dataInizioOccupazione;
    private LocalDate dataScadenzaOccupazione;
    private LocalTime oraInizioOccupazione;
    private LocalTime oraScadenzaOccupazione;
    private Integer idTipoAttivitaDaSvolgere;
    private Integer idStatoPratica;
    private Integer idTipoProcesso;
    private Geometry coordUbicazioneTemporanea;
    private Geometry coordUbicazioneDefinitiva;
    private Utente utentePresaInCarico;
}
