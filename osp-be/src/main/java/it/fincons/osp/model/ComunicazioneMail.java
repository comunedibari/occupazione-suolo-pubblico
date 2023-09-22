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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;

import lombok.Data;

@Entity
@Table(name = "T_COMUNICAZIONE_MAIL")
@Data
public class ComunicazioneMail implements Serializable {

	private static final long serialVersionUID = -4380344885149075970L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_COM_EMAIL")
	@SequenceGenerator(name = "SEQ_COM_EMAIL", sequenceName = "SEQ_COM_EMAIL", allocationSize = 1, initialValue = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "ID_PRATICA", foreignKey = @ForeignKey(name = "FK_COM_MAIL_PRATICA"))
	private Pratica pratica;

	@ManyToOne
	@JoinColumn(name = "ID_RICH_INTEGRAZIONE", foreignKey = @ForeignKey(name = "FK_COM_MAIL_RICH_INT"))
	private RichiestaIntegrazione richiestaIntegrazione;

	@ManyToOne
	@JoinColumn(name = "ID_RICH_PARERE", foreignKey = @ForeignKey(name = "FK_COM_MAIL_RICH_PARERE"))
	private RichiestaParere richiestaParere;

	@NotBlank
	@Size(max = 500)
	@Column(name = "DESTINATARI", nullable = false)
	private String destinatari;

	@Size(max = 500)
	@Column(name = "DESTINATARI_CC")
	private String destinatariCc;

	@NotBlank
	@Size(max = 998)
	@Column(name = "OGGETTO", nullable = false)
	private String oggetto;

	@Size(max = 2000)
	@Column(name = "TESTO")
	private String testo;

	@Column(name = "NUMERO_TENTATIVI_INVIO")
	private Integer numeroTentativiInvio;

	@Column(name = "FLG_INVIATA", nullable = false, columnDefinition = "boolean default false")
	private boolean flagInviata;

	@Column(name = "DATE_INSERIMENTO", nullable = false)
	private LocalDateTime dataInserimento;

	@Column(name = "DATE_INVIO")
	private LocalDateTime dataInvio;

	@Column(name = "FLG_PEC", nullable = false, columnDefinition = "boolean default false")
	private boolean flagPec;
	
	@Size(max = 255)
	@Column(name = "NOME_FILE_ALLEGATO")
	private String nomeFileAllegato;
	
	@Size(max = 255)
	@Column(name = "MIME_TYPE_FILE_ALLEGATO")
	private String mimeTypeFileAllegato;
	
	@Lob
	@Type(type="org.hibernate.type.BinaryType")
	@Column(name = "FILE_ALLEGATO", columnDefinition="bytea")
	private byte[] fileAllegato;
}
