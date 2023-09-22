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
@Table(name = "T_PARERE")
@Data
public class Parere implements Serializable {

	private static final long serialVersionUID = -4108755607511032069L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PARERE")
	@SequenceGenerator(name = "SEQ_PARERE", sequenceName = "SEQ_PARERE", allocationSize = 1, initialValue = 1)
	private Long id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToOne
	@JoinColumn(name = "ID_RICHIESTA_PARERE", foreignKey = @ForeignKey(name = "FK_PARERE_RICH_PARERE"), nullable = false)
	private RichiestaParere richiestaParere;

	@ManyToOne
	@JoinColumn(name = "ID_UTENTE_PARERE", foreignKey = @ForeignKey(name = "FK_PARERE_UTENTE"), nullable = false)
	private Utente utenteParere;

	@Size(max = 255)
	@Column(name = "NOTA")
	private String nota;

	@Column(name = "FLG_ESITO")
	private Boolean esito;

	@Size(max = 255)
	@Column(name = "CODICE_PROTOCOLLO")
	private String codiceProtocollo;
	
	@Column(name = "DATA_PROTOCOLLO")
	private LocalDateTime dataProtocollo;

	@Column(name = "DATA_INSERIMENTO", nullable = false)
	private LocalDateTime dataInserimento;
	
	@Column(name = "FLG_COMPETENZA")
	private Boolean flagCompetenza;
}
