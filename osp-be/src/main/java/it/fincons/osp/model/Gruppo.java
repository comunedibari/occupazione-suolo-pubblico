package it.fincons.osp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Table(name = "T_GRUPPO", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_GRUPPO_DESCRIZIONE", columnNames = "DESCRIZIONE") })
@Data
public class Gruppo implements Serializable {

	private static final long serialVersionUID = 3605798192148925177L;

	@Id
	private Integer id;

	@NotBlank
	@Size(max = 50)
	@Column(name = "DESCRIZIONE", nullable = false)
	private String descrizione;

	@Column(name = "FLG_GEST_INVIO_MAIL_DETERMINA", nullable = false, columnDefinition = "boolean default false")
	private boolean flagGestInvioMailDetermina;

	@Column(name = "FLG_GEST_RICHIESTA_PARERE", nullable = false, columnDefinition = "boolean default false")
	private boolean flagGestRichiestaParere;

	@Column(name = "FLG_INSERISCI_RICHIESTA", nullable = false, columnDefinition = "boolean default false")
	private boolean flagInserisciRichiesta;

	@Column(name = "FLG_CONCESSIONI_VALIDE", nullable = false, columnDefinition = "boolean default false")
	private boolean flagConcessioniValide;

	@Column(name = "FLG_FASCICOLO", nullable = false, columnDefinition = "boolean default false")
	private boolean flagFascicolo;

	@Column(name = "FLG_PRATICA_INSERITA", nullable = false, columnDefinition = "boolean default false")
	private boolean flagPraticaInserita;

	@Column(name = "FLG_VERIFICA_PRATICHE", nullable = false, columnDefinition = "boolean default false")
	private boolean flagVerificaPratiche;

	@Column(name = "FLG_RICHIESTA_PARERI", nullable = false, columnDefinition = "boolean default false")
	private boolean flagRichiestaPareri;

	@Column(name = "FLG_NECESSARIA_INTEGRAZIONE", nullable = false, columnDefinition = "boolean default false")
	private boolean flagNecessariaIntegrazione;

	@Column(name = "FLG_PRATICHE_APPROVATE", nullable = false, columnDefinition = "boolean default false")
	private boolean flagPraticheApprovate;

	@Column(name = "FLG_ATTESA_PAGAMENTO", nullable = false, columnDefinition = "boolean default false")
	private boolean flagAttesaPagamento;

	@Column(name = "FLG_PRONTE_RILASCIO", nullable = false, columnDefinition = "boolean default false")
	private boolean flagPronteRilascio;

	@Column(name = "FLG_PRATICHE_DA_RIGETTARE", nullable = false, columnDefinition = "boolean default false")
	private boolean flagPraticheDaRigettare;

	@Column(name = "FLG_PRATICHE_PREAVVISO_DINIEGO", nullable = false, columnDefinition = "boolean default false")
	private boolean flagPratichePreavvisoDiniego;

	@Column(name = "FLG_PRATICHE_ARCHIVIATE", nullable = false, columnDefinition = "boolean default false")
	private boolean flagPraticheArchiviate;

	@Column(name = "FLG_PRATICHE_RIGETTATE", nullable = false, columnDefinition = "boolean default false")
	private boolean flagPraticheRigettate;

	@Column(name = "FLG_GESTIONE_PROFILO", nullable = false, columnDefinition = "boolean default false")
	private boolean flagGestioneProfilo;

	@Column(name = "FLG_GESTIONE_UTENTI", nullable = false, columnDefinition = "boolean default false")
	private boolean flagGestioneUtenti;

	@Column(name = "FLG_VERIFICA_RIPRISTINO_LUOGHI", nullable = false, columnDefinition = "boolean default false")
	private boolean flagVerificaRipristinoLuoghi;

}
