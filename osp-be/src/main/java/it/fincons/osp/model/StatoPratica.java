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
@Table(name = "TP_STATO_PRATICA", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_STATO_PRATICA_DESCRIZIONE", columnNames = "DESCRIZIONE") })
@Data
public class StatoPratica implements Serializable {

	private static final long serialVersionUID = -831348817716235579L;

	@Id
	private Integer id;

	@NotBlank
	@Size(max = 50)
	@Column(name = "DESCRIZIONE", nullable = false)
	private String descrizione;

	@NotBlank
	@Size(max = 255)
	@Column(name = "DESCRIZIONE_ESTESA", nullable = false)
	private String descrizioneEstesa;
}
