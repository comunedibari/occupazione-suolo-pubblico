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
@Table(name = "TP_TIPO_ALLEGATO", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_TIPO_ALLEGATO", columnNames = "DESCRIZIONE") })
@Data
public class TipoAllegato implements Serializable {

	private static final long serialVersionUID = -5988120465636176520L;

	@Id
	private Integer id;

	@NotBlank
	@Size(max = 100)
	@Column(name = "DESCRIZIONE", nullable = false)
	private String descrizione;

	@Size(max = 255)
	@Column(name = "DESCRIZIONE_ESTESA")
	private String descrizioneEstesa;

}
