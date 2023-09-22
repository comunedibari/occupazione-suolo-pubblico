package it.fincons.osp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Table(name = "TP_TIPO_TEMPLATE", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_TIPO_TEMPLATE_DESCRIZIONE", columnNames = "DESCRIZIONE") })
@Data
public class TipoTemplate implements Serializable {

	private static final long serialVersionUID = 8436754986929475170L;

	@Id
	@OrderBy("id")
	private Integer id;

	@NotBlank
	@Size(max = 100)
	@Column(name = "DESCRIZIONE", nullable = false)
	private String descrizione;

}
