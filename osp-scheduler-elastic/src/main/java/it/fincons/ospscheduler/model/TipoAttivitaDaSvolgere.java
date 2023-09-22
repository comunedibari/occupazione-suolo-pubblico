package it.fincons.ospscheduler.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "TP_TIPO_ATTIVITA_DA_SVOLGERE", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_TIPO_ATT_DA_SVOL_DESC", columnNames = "DESCRIZIONE") })
@Data
public class TipoAttivitaDaSvolgere implements Serializable {

	private static final long serialVersionUID = -2037883058269954337L;

	@Id
	private Integer id;

	@NotBlank
	@Size(max = 50)
	@Column(name = "DESCRIZIONE", nullable = false)
	private String descrizione;

	@Column(name = "FLG_TESTO_LIBERO", nullable = false, columnDefinition = "boolean default false")
	private boolean flagTestoLibero;
}
