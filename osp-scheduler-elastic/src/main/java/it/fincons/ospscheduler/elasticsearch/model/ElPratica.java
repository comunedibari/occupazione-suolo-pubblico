package it.fincons.ospscheduler.elasticsearch.model;

import lombok.Builder;
import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Document(indexName = "osp", createIndex = false)
@Data
@Builder
@Mapping()
public class ElPratica {
    @Id
    private Long id;

    @Field(name ="id_municipio")
    private Integer idMunicipio;

    @Field(name ="id_tipo_ruolo_richiedente")
    private Integer idTipoRuoloRichiedente;

    @Field(name ="data_inserimento", type= FieldType.Date, format = DateFormat.date)
    private LocalDateTime dataInserimento;

    @Field(name ="data_inizio_occupazione", type= FieldType.Date, format = DateFormat.date)
    private LocalDate dataInizioOccupazione;

    @Field(name ="data_scadenza_occupazione", type= FieldType.Date, format = DateFormat.date)
    private LocalDate dataScadenzaOccupazione;

    @Field(name ="ora_inizio_occupazione", type=FieldType.Date, format={}, pattern="HH:mm:ss")
    private LocalTime oraInizioOccupazione;

    @Field(name ="ora_scadenza_occupazione", type=FieldType.Date, format={}, pattern="HH:mm:ss")
    private LocalTime oraScadenzaOccupazione;

    @Field(name ="id_tipo_attivita_da_svolgere")
    private Integer idTipoAttivitaDaSvolgere;

    @Field(name ="id_stato_pratica")
    private Integer idStatoPratica;

    @Field(name ="id_tipo_processo")
    private Integer idTipoProcesso;

    @GeoPointField
    @Field(name ="coord_ubicazione_temporanea", type= FieldType.Text)
    private String coordUbicazioneTemporanea;

    @GeoPointField
    @Field(name ="coord_ubicazione_definitiva", type= FieldType.Text)
    private String coordUbicazioneDefinitiva;

    @Field(name ="istruttore_responsabile")
    private String istruttoreResponsabile;
}
