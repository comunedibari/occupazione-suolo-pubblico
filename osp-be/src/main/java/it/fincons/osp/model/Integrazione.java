package it.fincons.osp.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "T_INTEGRAZIONE")
@Data
public class Integrazione implements Serializable {

	private static final long serialVersionUID = -4205269003049868094L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INTEGRAZIONE")
	@SequenceGenerator(name = "SEQ_INTEGRAZIONE", sequenceName = "SEQ_INTEGRAZIONE", allocationSize = 1, initialValue = 1)
	private Long id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToOne
	@JoinColumn(name = "ID_RICHIESTA_INTEGRAZIONE", foreignKey = @ForeignKey(name = "FK_INTEGRAZIONE_RICH_INT"), nullable = false)
	private RichiestaIntegrazione richiestaIntegrazione;
	
	@ManyToOne
	@JoinColumn(name = "ID_UTENTE_INTEGRAZIONE", foreignKey = @ForeignKey(name = "FK_INTEGRAZIONE_UTENTE"), nullable = false)
	private Utente utenteIntegrazione;

	@Size(max = 512)
	@Column(name = "MOTIVO_INTEGRAZIONE")
	private String motivoIntegrazione;

	@Size(max = 255)
	@Column(name = "CODICE_PROTOCOLLO")
	private String codiceProtocollo;

	@Size(max = 100)
	@Column(name = "NUMERO_PROTOCOLLO")
	private String numeroProtocollo;

	@Column(name = "DATA_PROTOCOLLO")
	private LocalDateTime dataProtocollo;

	@Column(name = "ANNO")
	private String anno;

	@Column(name = "DATA_INSERIMENTO", nullable = false)
	private LocalDateTime dataInserimento;
	
	@Column(name = "DATA_INIZIO_OCCUPAZIONE")
	private LocalDate dataInizioOccupazione;

	@Column(name = "ORA_INIZIO_OCCUPAZIONE")
	private LocalTime oraInizioOccupazione;

	@Column(name = "DATA_SCADENZA_OCCUPAZIONE")
	private LocalDate dataScadenzaOccupazione;

	@Column(name = "ORA_SCADENZA_OCCUPAZIONE")
	private LocalTime oraScadenzaOccupazione;
	
	@Size(max = 50)
	@Column(name = "NOME_CITTADINO_EGOV")
	private String nomeCittadinoEgov;

	@Size(max = 50)
	@Column(name = "COGNOME_CITTADINO_EGOV")
	private String cognomeCittadinoEgov;
	
	@Size(max = 16)
	@Column(name = "CF_CITTADINO_EGOV", length = 16)
	private String cfCittadinoEgov;
}
