package it.fincons.osp.model;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Table(name = "T_RICHIEDENTE")
@Data
public class Richiedente implements Serializable {

	private static final long serialVersionUID = -4860285842646448439L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RICHIEDENTE")
	@SequenceGenerator(name = "SEQ_RICHIEDENTE", sequenceName = "SEQ_RICHIEDENTE", allocationSize = 1, initialValue = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "ID_TIPO_RUOLO_RICHIEDENTE", foreignKey = @ForeignKey(name = "FK_RICHIEDENTE_TIP_RUOLO_RICH"), nullable = false)
	private TipoRuoloRichiedente tipoRuoloRichiedente;

	@Size(max = 50)
	@Column(name = "NOME")
	private String nome;

	@Size(max = 50)
	@Column(name = "COGNOME")
	private String cognome;

	@Size(max = 50)
	@Column(name = "DENOMINAZIONE")
	private String denominazione;

	@Size(max = 16)
	@Column(name = "CODICE_FISCALE_P_IVA", length = 16)
	private String codiceFiscalePartitaIva;

	@Column(name = "DATA_DI_NASCITA")
	private LocalDate dataDiNascita;

	@Size(max = 50)
	@Column(name = "COMUNE_DI_NASCITA")
	private String comuneDiNascita;

	@Size(max = 50)
	@Column(name = "PROVINCIA_DI_NASCITA")
	private String provinciaDiNascita;

	@Size(max = 50)
	@Column(name = "NAZIONALITA")
	private String nazionalita;

	@Size(max = 50)
	@Column(name = "CITTA")
	private String citta;

	@Size(max = 100)
	@Column(name = "INDIRIZZO")
	private String indirizzo;

	@Size(max = 10)
	@Column(name = "CIVICO")
	private String civico;

	@Size(max = 50)
	@Column(name = "PROVINCIA")
	private String provincia;

	@Size(max = 5)
	@Column(name = "CAP")
	private String cap;

	@Size(max = 15)
	@Column(name = "RECAPITO_TELEFONICO")
	private String recapitoTelefonico;

	@Size(max = 50)
	@Email
	@Column(name = "EMAIL")
	private String email;

	@ManyToOne
	@JoinColumn(name = "ID_TIPO_DOCUMENTO_ALLEGATO", foreignKey = @ForeignKey(name = "FK_RICHIEDENTE_TIP_DOC_ALL"))
	private TipoDocumentoAllegato tipoDocumentoAllegato;

	@Size(max = 50)
	@Column(name = "NUMERO_DOC_ALLEGATO")
	private String numeroDocumentoAllegato;

	@Size(max = 50)
	@Column(name = "AMM_DOC_ALLEGATO")
	private String amministrazioneDocumentoAllegato;

	@Size(max = 50)
	@Column(name = "QUALITA_RUOLO")
	private String qualitaRuolo;

	@Size(max = 50)
	@Column(name = "DESCRIZIONE_RUOLO")
	private String descrizioneRuolo;

	@Column(name = "FLG_FIRMATARIO", nullable = false)
	private boolean flagFirmatario;

	@Column(name = "FLG_DESTINATARIO", nullable = false)
	private boolean flagDestinatario;
}
