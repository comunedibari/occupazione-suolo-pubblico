package it.fincons.osp.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.locationtech.jts.geom.Geometry;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "T_DATI_RICHIESTA")
@Data
public class DatiRichiesta implements Serializable {

	private static final long serialVersionUID = 7143196339282683159L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DATI_RICHIESTA")
	@SequenceGenerator(name = "SEQ_DATI_RICHIESTA", sequenceName = "SEQ_DATI_RICHIESTA", allocationSize = 1, initialValue = 1)
	private Long id;

	@OneToOne(mappedBy = "datiRichiesta")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Pratica pratica;

	@NotBlank
	@Size(max = 300)
	@Column(name = "UBICAZIONE_OCCUPAZIONE", nullable = false)
	private String ubicazioneOccupazione;

	@Size(max = 255)
	@Column(name = "NOME_VIA")
	private String nomeVia;

	@Size(max = 5)
	@Column(name = "NUMERO_VIA")
	private String numeroVia;

	@Size(max = 20)
	@Column(name = "COD_VIA")
	private String codVia;

	@Column(name = "ID_MUNICIPIO")
	private Integer idMunicipio;

	@Size(max = 50)
	@Column(name = "LOCALITA")
	private String localita;

	@Column(name = "FLG_NUM_CIVICO_ASSENTE", nullable = false, columnDefinition = "boolean default false")
	private boolean flagNumeroCivicoAssente;

	@Column(name = "NOTE_UBICAZIONE", columnDefinition = "text")
	private String noteUbicazione;

	@Column(name = "SUPERFICIE_AREA_MQ", precision = 15, scale = 2, nullable = false)
	private Double superficieAreaMq;

	@Column(name = "LARGHEZZA_M", precision = 15, scale = 2, nullable = false)
	private Double larghezzaM;

	@Column(name = "LUNGHEZZA_M", precision = 15, scale = 2, nullable = false)
	private Double lunghezzaM;

	@Column(name = "SUPERFICIE_MARC_MQ", precision = 15, scale = 2)
	private Double superficieMarciapiedeMq;

	@Column(name = "LARGHEZZA_MARC_M", precision = 15, scale = 2)
	private Double larghezzaMarciapiedeM;

	@Column(name = "LUNGHEZZA_MARC_M", precision = 15, scale = 2)
	private Double lunghezzaMarciapiedeM;

	@Column(name = "LARGHEZZA_CARR_M", precision = 15, scale = 2)
	private Double larghezzaCarreggiataM;

	@Column(name = "LUNGHEZZA_CARR_M", precision = 15, scale = 2)
	private Double lunghezzaCarreggiataM;

	@Column(name = "STALLO_DI_SOSTA")
	private Boolean stalloDiSosta;

	@Column(name = "PRES_SCIVOLI_DIV_ABILI")
	private Boolean presScivoliDiversamenteAbili;

	@Column(name = "PRES_PASS_CARR_DIV_ABILI")
	private Boolean presPassiCarrabiliDiversamenteAbili;

	@Column(name = "DATA_INIZIO_OCCUPAZIONE", nullable = false)
	private LocalDate dataInizioOccupazione;

	@Column(name = "ORA_INIZIO_OCCUPAZIONE")
	private LocalTime oraInizioOccupazione;

	@Column(name = "DATA_SCADENZA_OCCUPAZIONE", nullable = false)
	private LocalDate dataScadenzaOccupazione;

	@Column(name = "ORA_SCADENZA_OCCUPAZIONE")
	private LocalTime oraScadenzaOccupazione;

	@ManyToOne
	@JoinColumn(name = "ID_TIPO_ATTIVITA_DA_SVOLGERE", foreignKey = @ForeignKey(name = "FK_DATI_RICHIESTA_TIPO_ATT"))
	private TipoAttivitaDaSvolgere attivitaDaSvolgere;

	@ManyToOne
	@JoinColumn(name = "ID_TIPOLOGIA_TITOLO_EDILIZIO", foreignKey = @ForeignKey(name = "FK_DATI_RICHIESTA_TIP_TITOLO_ED"))
	private TipologiaTitoloEdilizio tipologiaTitoloEdilizio;

	@Size(max = 100)
	@Column(name = "DESCRIZIONE_TITOLO_EDILIZIO")
	private String descrizioneTitoloEdilizio;

	@Size(max = 100)
	@Column(name = "RIFERIMENTO_TITOLO_EDILIZIO")
	private String riferimentoTitoloEdilizio;

	@Size(max = 255)
	@Column(name = "DESCRIZIONE_ATT_DA_SVOLGERE")
	private String descrizioneAttivitaDaSvolgere;

	@ManyToOne
	@JoinColumn(name = "ID_TIPO_MANUFATTO", foreignKey = @ForeignKey(name = "FK_DATI_RICHIESTA_TIPO_MAN"))
	private TipoManufatto manufatto;

	@Size(max = 255)
	@Column(name = "DESCRIZIONE_MANUFATTO")
	private String descrizioneManufatto;

	@Column(name = "FLG_ACCETT_REG_SUOLO_PUBBLICO", nullable = false, columnDefinition = "boolean default false")
	private boolean flagAccettazioneRegSuoloPubblico;

	@Column(name = "FLG_RISP_INTERESSE_TERZI", nullable = false, columnDefinition = "boolean default false")
	private boolean flagRispettoInteresseTerzi;

	@Column(name = "FLG_OBBLIGO_RIP_DANNI", nullable = false, columnDefinition = "boolean default false")
	private boolean flagObbligoRiparazioneDanni;

	@Column(name = "FLG_RISP_DISP_REGOLAMENTO", nullable = false, columnDefinition = "boolean default false")
	private boolean flagRispettoDisposizioniRegolamento;

	@Column(name = "FLG_CONOSC_TASSA_OCCUPAZIONE", nullable = false, columnDefinition = "boolean default false")
	private boolean flagConoscenzaTassaOccupazione;
	
	@Column(name = "FLG_NON_MODIFICHE_RISPETTO_CONCESSIONE", nullable = false, columnDefinition = "boolean default false")
	private boolean flagNonModificheRispettoConcessione;

	private Geometry coordUbicazioneTemporanea;

	private Geometry coordUbicazioneDefinitiva;

	@Enumerated(EnumType.STRING)
	@Column(name = "TP_OP_VERIFICA_OCCUPAZIONE")
	private TipoOperazioneVerificaOccupazione tipoOperazioneVerificaOccupazione;

	@Column(name = "FLAG_ESENZIONE_MARCA_DA_BOLLO", nullable = true, columnDefinition = "boolean default false")
	private boolean flagEsenzioneMarcaDaBollo = false;

	@Column(name = "FLAG_ESENZIONE_MARCA_DA_BOLLO_MODIFICATO", nullable = true, columnDefinition = "boolean default false")
	private boolean flagEsenzioneMarcaDaBolloModificato = false;

	@Size(max = 255)
	@Column(name = "MOTIVAZIONE_ESENZIONE_MARCA_BOLLO")
	private String motivazioneEsenzioneMarcaBollo;
}
