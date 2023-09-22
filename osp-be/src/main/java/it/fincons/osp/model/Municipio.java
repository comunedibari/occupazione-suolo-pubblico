package it.fincons.osp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Table(name = "T_MUNICIPIO", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_MUNICIPIO_DESCRIZIONE", columnNames = "DESCRIZIONE") })
@Data
public class Municipio implements Serializable {

	private static final long serialVersionUID = 2272059719584391949L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MUNICIPIO")
	@SequenceGenerator(name = "SEQ_MUNICIPIO", sequenceName = "SEQ_MUNICIPIO", allocationSize = 1, initialValue = 1)
	private Integer id;

	@NotBlank
	@Size(max = 50)
	@Column(name = "DESCRIZIONE", nullable = false)
	private String descrizione;

}
