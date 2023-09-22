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
@Table(name = "TP_TIPO_NOTIFICA_SCADENZARIO", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_TIPO_NOTIFICA_SCAD_DESC", columnNames = "DESCRIZIONE") })
@Data
public class TipoNotificaScadenzario implements Serializable {

	private static final long serialVersionUID = 5812960975897935641L;

	@Id
	private Integer id;

	@NotBlank
	@Size(max = 50)
	@Column(name = "DESCRIZIONE", nullable = false)
	private String descrizione;
}
