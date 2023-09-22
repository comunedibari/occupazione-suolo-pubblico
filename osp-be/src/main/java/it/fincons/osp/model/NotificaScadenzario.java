package it.fincons.osp.model;

import java.io.Serializable;
import java.time.LocalDateTime;

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

import lombok.Data;

@Entity
@Table(name = "T_NOTIFICA_SCADENZARIO")
@Data
public class NotificaScadenzario implements Serializable {

	private static final long serialVersionUID = -3660483468080975571L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_NOTIFICA_SCADENZARIO")
	@SequenceGenerator(name = "SEQ_NOTIFICA_SCADENZARIO", sequenceName = "SEQ_NOTIFICA_SCADENZARIO", allocationSize = 1, initialValue = 1)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "ID_PRATICA", foreignKey = @ForeignKey(name = "FK_NOTIFICA_SCAD_PRATICA"), nullable = false)
	private Pratica pratica;
	
	@ManyToOne
	@JoinColumn(name = "ID_UTENTE", foreignKey = @ForeignKey(name = "FK_NOTIFICA_SCAD_UTENTE"), nullable = false)
	private Utente utente;
	
	@ManyToOne
	@JoinColumn(name = "ID_TIPO_NOTIFICA_SCADENZARIO", foreignKey = @ForeignKey(name = "FK_NOTIFICA_SCAD_TIPO"), nullable = false)
	private TipoNotificaScadenzario tipoNotificaScadenzario;

	@Column(name = "DATA_NOTIFICA")
	private LocalDateTime dataNotifica;
}
