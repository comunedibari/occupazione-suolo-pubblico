package it.fincons.osp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

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
