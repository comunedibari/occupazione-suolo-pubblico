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
@Table(name = "TP_TIPO_RUOLO_RICHIEDENTE", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_TIPO_RUOLO_RICH_DESCRIZIONE", columnNames = "DESCRIZIONE") })
@Data
public class TipoRuoloRichiedente implements Serializable {

	private static final long serialVersionUID = -4817809755812642792L;

	@Id
	private Integer id;

	@NotBlank
	@Size(max = 50)
	@Column(name = "DESCRIZIONE", nullable = false)
	private String descrizione;
}
