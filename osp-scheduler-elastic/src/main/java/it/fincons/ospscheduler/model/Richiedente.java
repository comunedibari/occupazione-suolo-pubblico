package it.fincons.ospscheduler.model;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Table(name = "T_RICHIEDENTE")
@Data
public class Richiedente implements Serializable {

	private static final long serialVersionUID = -4860285842646448439L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RICHIEDENTE")
	@SequenceGenerator(name = "SEQ_RICHIEDENTE", sequenceName = "SEQ_RICHIEDENTE", allocationSize = 1, initialValue = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "ID_TIPO_RUOLO_RICHIEDENTE", foreignKey = @ForeignKey(name = "FK_RICHIEDENTE_TIP_RUOLO_RICH"), nullable = false)
	private TipoRuoloRichiedente tipoRuoloRichiedente;


}
