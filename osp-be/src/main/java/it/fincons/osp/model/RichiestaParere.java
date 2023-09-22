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
@Table(name = "T_RICHIESTA_PARERE")
@Data
public class RichiestaParere implements Serializable {

	private static final long serialVersionUID = 902212169613182343L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RICH_PARERE")
	@SequenceGenerator(name = "SEQ_RICH_PARERE", sequenceName = "SEQ_RICH_PARERE", allocationSize = 1, initialValue = 1)
	private Long id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne
	@JoinColumn(name = "ID_PRATICA", foreignKey = @ForeignKey(name = "FK_RICH_PARERE_PRATICA"), nullable = false)
	private Pratica pratica;

	@ManyToOne
	@JoinColumn(name = "ID_UTENTE_RICHIEDENTE", foreignKey = @ForeignKey(name = "FK_RICH_PARERE_UTENTE_RICH"), nullable = false)
	private Utente utenteRichiedente;

	@ManyToOne
	@JoinColumn(name = "ID_STATO_PRATICA", foreignKey = @ForeignKey(name = "FK_RICH_PARERE_STATO_PRATICA"), nullable = false)
	private StatoPratica statoPratica;
	
	@ManyToOne
	@JoinColumn(name = "ID_GRUPPO_DEST_PARERE", nullable = false, foreignKey = @ForeignKey(name = "FK_RICH_PARERE_GRUPPO"))
	private Gruppo gruppoDestinatarioParere;

	@Size(max = 100)
	@Column(name = "CODICE_PROTOCOLLO")
	private String codiceProtocollo;
	
	@Column(name = "DATA_PROTOCOLLO")
	private LocalDateTime dataProtocollo;

	@Size(max = 512)
	@Column(name = "NOTA_RICHIESTA_PARERE")
	private String notaRichiestaParere;

	@Column(name = "DATA_INSERIMENTO", nullable = false)
	private LocalDateTime dataInserimento;

	@Column(name = "FLG_INSERITA_RISPOSTA", nullable = false, columnDefinition = "boolean default false")
	private boolean flagInseritaRisposta;
	
	@OneToOne(mappedBy = "richiestaParere")
	private Parere parere;
}
