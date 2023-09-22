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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;

import lombok.Data;

@Entity
@Table(name = "T_ALLEGATO")
@Data
public class Allegato implements Serializable {

	private static final long serialVersionUID = -8182252035805035783L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ALLEGATO")
	@SequenceGenerator(name = "SEQ_ALLEGATO", sequenceName = "SEQ_ALLEGATO", allocationSize = 1, initialValue = 1)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "ID_PRATICA", foreignKey = @ForeignKey(name = "FK_ALLEGATO_PRATICA"))
	private Pratica pratica;

	@ManyToOne
	@JoinColumn(name = "ID_PARERE", foreignKey = @ForeignKey(name = "FK_ALLEGATO_PARERE"))
	private Parere parere;

	@ManyToOne
	@JoinColumn(name = "ID_INTEGRAZIONE", foreignKey = @ForeignKey(name = "FK_ALLEGATO_INTEGRAZIONE"))
	private Integrazione integrazione;
	
	@Column(name = "ID_RICHIESTA_PARERE")
	private Long idRichiestaParere;
	
	@Column(name = "ID_RICHIESTA_INTEGRAZIONE")
	private Long idRichiestaIntegrazione;

	@ManyToOne
	@JoinColumn(name = "ID_TIPO_ALLEGATO", foreignKey = @ForeignKey(name = "FK_ALLEGATO_TIPO_ALLEGATO"))
	private TipoAllegato tipoAllegato;

	@Column(name = "DATA_INSERIMENTO", nullable = false)
	private LocalDateTime dataInserimento;

	@Size(max = 255)
	@Column(name = "NOTA")
	private String nota;
	
	@Size(max = 255)
	@Column(name = "NOME_FILE")
	private String nomeFile;
	
	@Size(max = 255)
	@Column(name = "MIME_TYPE")
	private String mimeType;
	
	@Min(1)
	@Column(name = "REVISIONE")
	private Integer revisione;

	@Lob
	@Type(type="org.hibernate.type.BinaryType")
	@Column(name = "FILE_ALLEGATO", columnDefinition="bytea")
	private byte[] fileAllegato;

	@Size(max = 100)
	@Column(name = "CODICE_PROTOCOLLO")
	private String codiceProtocollo;
}
