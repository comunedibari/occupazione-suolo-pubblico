package it.fincons.osp.model;

import lombok.Data;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "T_PRATICA", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_PRATICA_DATI_RICHIESTA", columnNames = "ID_DATI_RICHIESTA") })
@Data
public class Pratica implements Serializable {
	private static final long serialVersionUID = -1734237787812205647L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PRATICA")
	@SequenceGenerator(name = "SEQ_PRATICA", sequenceName = "SEQ_PRATICA", allocationSize = 1, initialValue = 1)
	private Long id;

	@Column(name = "ID_PRATICA_ORIGINARIA")
	private Long idPraticaOriginaria;

	@Column(name = "ID_PROROGA_PRECEDENTE")
	private Long idProrogaPrecedente;

	@Size(max = 512)
	@Column(name = "MOTIVAZIONE_RICHIESTA")
	private String motivazioneRichiesta;

	@OneToOne(orphanRemoval = true)
	@JoinColumn(name = "ID_DATI_RICHIESTA", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_PRATICA_DATI_RICHIESTA"), nullable = false)
	private DatiRichiesta datiRichiesta;

	@OneToOne(orphanRemoval = true)
	@JoinColumn(name = "ID_RICHIEDENTE_FIRMATARIO", foreignKey = @ForeignKey(name = "FK_PRATICA_RICH_FIRMATARIO"), nullable = false)
	private Richiedente firmatario;

	@OneToOne(orphanRemoval = true)
	@JoinColumn(name = "ID_RICHIEDENTE_DESTINATARIO", foreignKey = @ForeignKey(name = "FK_PRATICA_RICH_DESTINATARIO"))
	private Richiedente destinatario;

	@ManyToOne
	@JoinColumn(name = "ID_MUNICIPIO", foreignKey = @ForeignKey(name = "FK_PRATICA_MUNICIPIO"))
	private Municipio municipio;

	@ManyToOne
	@JoinColumn(name = "ID_UTENTE_PRESA_IN_CARICO", foreignKey = @ForeignKey(name = "FK_PRATICA_UTENTE_PC"))
	private Utente utentePresaInCarico;

	@ManyToOne
	@JoinColumn(name = "ID_UTENTE_CREAZIONE", foreignKey = @ForeignKey(name = "FK_PRATICA_UTENTE_CRE"), nullable = false)
	private Utente utenteCreazione;

	@ManyToOne
	@JoinColumn(name = "ID_UTENTE_MODIFICA", foreignKey = @ForeignKey(name = "FK_PRATICA_UTENTE_MOD"))
	private Utente utenteModifica;

	@ManyToOne
	@JoinColumn(name = "ID_UTENTE_ASSEGNATARIO", foreignKey = @ForeignKey(name = "FK_PRATICA_UTENTE_ASS"))
	private Utente utenteAssegnatario;

	@Column(name = "DATA_CREAZIONE", nullable = false)
	private LocalDateTime dataCreazione;

	@Column(name = "DATA_INSERIMENTO")
	private LocalDateTime dataInserimento;

	@Column(name = "DATA_MODIFICA")
	private LocalDateTime dataModifica;

	@ManyToOne
	@JoinColumn(name = "ID_TIPO_PROCESSO", foreignKey = @ForeignKey(name = "FK_PRATICA_TIPO_PROCESSO"), nullable = false)
	private TipoProcesso tipoProcesso;

	@ManyToOne
	@JoinColumn(name = "ID_STATO_PRATICA", foreignKey = @ForeignKey(name = "FK_PRATICA_STATO_PRATICA"), nullable = false)
	private StatoPratica statoPratica;

	@Size(max = 512)
	@Column(name = "INFO_PASSAGGIO_STATO", nullable = true)
	private String infoPassaggioStato;

	@Column(name = "FLG_VERIFICA_FORMALE")
	private Boolean flagVerificaFormale;

	@Column(name = "FLG_PROCEDURA_DINIEGO")
	private Boolean flagProceduraDiniego;

	@OneToMany(mappedBy = "pratica")
	// @OrderBy("statoPratica")
	private List<Protocollo> protocolli;

	@Size(max = 255)
	@Column(name = "CODICE_DETERMINA")
	private String codiceDetermina;

	@Column(name = "DATA_EMISSIONE_DETERMINA")
	private LocalDate dataEmissioneDetermina;

	@Size(max = 255)
	@Column(name = "CODICE_DETERMINA_RINUNCIA")
	private String codiceDeterminaRinuncia;

	@Column(name = "DATA_EMISSIONE_DETERMINA_RIN")
	private LocalDate dataEmissioneDeterminaRinuncia;

	@Size(max = 512)
	@Column(name = "NOTA_AL_CITTADINO_RDA")
	private String notaAlCittadinoRda;

	@Size(max = 255)
	@Column(name = "CODICE_DETERMINA_RDA")
	private String codiceDeterminaRda;

	@Column(name = "DATA_EMISSIONE_DETERMINA_RDA")
	private LocalDate dataEmissioneDeterminaRda;

	@Column(name = "DATA_SCADENZA_PRATICA")
	private LocalDateTime dataScadenzaPratica;

	@Column(name = "DATA_SCADENZA_RIGETTO")
	private LocalDateTime dataScadenzaRigetto;

	@Column(name = "DATA_SCADENZA_PREAV_DINIEGO")
	private LocalDateTime dataScadenzaPreavvisoDiniego;

	@Column(name = "DATA_SCADENZA_PAGAMENTO")
	private LocalDateTime dataScadenzaPagamento;

	@Column(name = "CONTATORE_RICHIESTE_INTEG")
	private Integer contatoreRichiesteIntegrazioni;

	@OneToMany(mappedBy = "pratica")
	@OrderBy("dataInserimento")
	private List<RichiestaParere> richiestePareri;

	@OneToMany(mappedBy = "pratica")
	@OrderBy("dataInserimento")
	private List<RichiestaIntegrazione> richiesteIntegrazioni;

	@Size(max = 50)
	@Column(name = "NOME_CITTADINO_EGOV")
	private String nomeCittadinoEgov;

	@Size(max = 50)
	@Column(name = "COGNOME_CITTADINO_EGOV")
	private String cognomeCittadinoEgov;

	@Size(max = 16)
	@Column(name = "CF_CITTADINO_EGOV", length = 16)
	private String cfCittadinoEgov;

	@Column(name = "ORIGIN_EGOV", nullable = true, columnDefinition = "boolean default false")
	private boolean originEgov;

	@OneToOne(orphanRemoval = true)
	@JoinColumn(name = "ID_MARCA_BOLLO_PRATICA", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_MARCA_BOLLO_PRATICA"), nullable = true)
	private MarcaBolloPratica marcaBolloPratica;

	@OneToOne(orphanRemoval = true)
	@JoinColumn(name = "ID_MARCA_BOLLO_DETERMINA", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_MARCA_BOLLO_DETERMINA"), nullable = true)
	private MarcaBolloDetermina marcaBolloDetermina;

	@Column(name = "FLAG_ESENZIONE_PAGAMENTO_CUP", nullable = true, columnDefinition = "boolean default false")
	private boolean flagEsenzionePagamentoCUP = false;

	@Size(max = 255)
	@Column(name = "MOTIVAZIONE_ESENZIONE_PAGAMENTO_CUP")
	private String motivazioneEsenzionePagamentoCup;
}
