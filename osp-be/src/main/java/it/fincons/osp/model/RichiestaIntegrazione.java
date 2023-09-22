package it.fincons.osp.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;


@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "T_RICHIESTA_INTEGRAZIONE")
@Data
public class RichiestaIntegrazione implements Serializable {

	private static final long serialVersionUID = 5091305772586498298L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RICH_INTEGRAZIONE")
	@SequenceGenerator(name = "SEQ_RICH_INTEGRAZIONE", sequenceName = "SEQ_RICH_INTEGRAZIONE", allocationSize = 1, initialValue = 1)
	private Long id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne
	@JoinColumn(name = "ID_PRATICA", foreignKey = @ForeignKey(name = "FK_RICH_INTEGRAZIONE_PRATICA"), nullable = false)
	private Pratica pratica;

	@ManyToOne
	@JoinColumn(name = "ID_UTENTE_RICHIEDENTE", foreignKey = @ForeignKey(name = "FK_PRATICA_UTENTE_RICH"), nullable = false)
	private Utente utenteRichiedente;

	@ManyToOne
	@JoinColumn(name = "ID_STATO_PRATICA", foreignKey = @ForeignKey(name = "FK_PRATICA_STATO_PRATICA"), nullable = false)
	private StatoPratica statoPratica;

	@Size(max = 100)
	@Column(name = "CODICE_PROTOCOLLO")
	private String codiceProtocollo;
	
	@Column(name = "DATA_PROTOCOLLO")
	private LocalDateTime dataProtocollo;

	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO_RICHIESTA")
	private TipoRichiestaIntegrazione tipoRichiesta;

	@Size(max = 512)
	@Column(name = "MOTIVO_RICHIESTA")
	private String motivoRichiesta;

	@Column(name = "DATA_INSERIMENTO", nullable = false)
	private LocalDateTime dataInserimento;

	@Column(name = "DATA_SCADENZA", nullable = false)
	private LocalDateTime dataScadenza;

	@Column(name = "FLG_INSERITA_RISPOSTA", nullable = false, columnDefinition = "boolean default false")
	private boolean flagInseritaRisposta;

	@OneToOne(mappedBy = "richiestaIntegrazione")
	private Integrazione integrazione;
}
