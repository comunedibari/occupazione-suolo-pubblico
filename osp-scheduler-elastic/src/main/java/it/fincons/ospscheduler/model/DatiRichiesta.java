package it.fincons.ospscheduler.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "T_DATI_RICHIESTA")
@Data
public class DatiRichiesta implements Serializable {

	private static final long serialVersionUID = 7143196339282683159L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DATI_RICHIESTA")
	@SequenceGenerator(name = "SEQ_DATI_RICHIESTA", sequenceName = "SEQ_DATI_RICHIESTA", allocationSize = 1, initialValue = 1)
	private Long id;

	@OneToOne(mappedBy = "datiRichiesta")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Pratica pratica;

	@Column(name = "DATA_INIZIO_OCCUPAZIONE", nullable = false)
	private LocalDate dataInizioOccupazione;

	@Column(name = "ORA_INIZIO_OCCUPAZIONE")
	private LocalTime oraInizioOccupazione;

	@Column(name = "DATA_SCADENZA_OCCUPAZIONE", nullable = false)
	private LocalDate dataScadenzaOccupazione;

	@Column(name = "ORA_SCADENZA_OCCUPAZIONE")
	private LocalTime oraScadenzaOccupazione;

	@ManyToOne
	@JoinColumn(name = "ID_TIPO_ATTIVITA_DA_SVOLGERE", foreignKey = @ForeignKey(name = "FK_DATI_RICHIESTA_TIPO_ATT"))
	private TipoAttivitaDaSvolgere attivitaDaSvolgere;

	private Geometry coordUbicazioneTemporanea;

	private Geometry coordUbicazioneDefinitiva;



}
