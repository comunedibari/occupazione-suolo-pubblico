package it.fincons.ospscheduler.model;

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
@Table(name = "TP_TIPO_PROCESSO", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_TIPO_PROCESSO_DESC", columnNames = "DESCRIZIONE") })
@Data
public class TipoProcesso implements Serializable {

	private static final long serialVersionUID = -8010977579634271935L;

	@Id
	private Integer id;

	@NotBlank
	@Size(max = 50)
	@Column(name = "DESCRIZIONE", nullable = false)
	private String descrizione;

}