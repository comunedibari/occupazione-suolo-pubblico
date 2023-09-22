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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;

import lombok.Data;

@Entity
@Table(name = "T_TEMPLATE", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_TEMPLATE_TIPO", columnNames = "ID_TIPO_TEMPLATE") })
@Data
public class Template implements Serializable {

	private static final long serialVersionUID = 6405397771535880117L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TEMPLATE")
	@SequenceGenerator(name = "SEQ_TEMPLATE", sequenceName = "SEQ_TEMPLATE", allocationSize = 1, initialValue = 1)
	private Integer id;

	@OneToOne
	@JoinColumn(name = "ID_TIPO_TEMPLATE", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_TEMPLATE_TIPO_TEMPLATE"), nullable = false)
	private TipoTemplate tipoTemplate;

	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	@Column(name = "FILE_TEMPLATE", columnDefinition = "bytea", nullable = false)
	public byte[] fileTemplate;

	@Size(max = 255)
	@Column(name = "NOME_FILE", nullable = false)
	private String nomeFile;

	@Size(max = 255)
	@Column(name = "MIME_TYPE", nullable = false)
	private String mimeType;

	@Column(name = "DATA_INSERIMENTO", nullable = false)
	private LocalDateTime dataInserimento;

	@Column(name = "DATA_MODIFICA")
	private LocalDateTime dataModifica;

	@ManyToOne
	@JoinColumn(name = "ID_UTENTE_MODIFICA", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_TEMPLATE_UTENTE_MOD"))
	private Utente utenteModifica;

}
