package it.fincons.osp.model;

import java.io.Serializable;
import java.time.LocalDate;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "T_PROTOCOLLO")
@Data
public class Protocollo implements Serializable{

	private static final long serialVersionUID = -8183850963693965257L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PROTOCOLLO")
	@SequenceGenerator(name = "SEQ_PROTOCOLLO", sequenceName = "SEQ_PROTOCOLLO", allocationSize = 1, initialValue = 1)
	private Long id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne
	@JoinColumn(name = "ID_PRATICA", nullable = false, foreignKey = @ForeignKey(name = "FK_PROTOCOLLO_PRATICA"))
	private Pratica pratica;

	@ManyToOne
	@JoinColumn(name = "ID_STATO_PRATICA", nullable = false, foreignKey = @ForeignKey(name = "FK_PROTOCOLLO_STATO_PRATICA"))
	private StatoPratica statoPratica;

	@NotBlank
	@Size(max = 100)
	@Column(name = "CODICE_PROTOCOLLO")
	private String codiceProtocollo;

	@NotBlank
	@Size(max = 100)
	@Column(name = "NUMERO_PROTOCOLLO")
	private String numeroProtocollo;
	
	@Column(name = "DATA_PROTOCOLLO")
	private LocalDateTime dataProtocollo;

	@Column(name = "ANNO")
	private String anno;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO_OPERAZIONE")
	private TipoOperazioneProtocollo tipoOperazione;
	
	@Size(max = 255)
	@Column(name = "CODICE_DETERMINA_RETTIFICA")
	private String codiceDeterminaRettifica;
	
	@Column(name = "DATA_EMISSIONE_DETERMINA_RET")
	private LocalDate dataEmissioneDeterminaRettifica;
}
