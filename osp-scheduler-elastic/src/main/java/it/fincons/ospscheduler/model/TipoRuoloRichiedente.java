package it.fincons.ospscheduler.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

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
