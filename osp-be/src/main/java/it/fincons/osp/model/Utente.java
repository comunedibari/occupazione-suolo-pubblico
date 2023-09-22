package it.fincons.osp.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Table(name = "T_UTENTE")
@Data
public class Utente implements Serializable {

	private static final long serialVersionUID = 6907098283035861853L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTENTE")
	@SequenceGenerator(name = "SEQ_UTENTE", sequenceName = "SEQ_UTENTE", allocationSize = 1, initialValue = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "ID_GRUPPO", nullable = false, foreignKey = @ForeignKey(name = "FK_UTENTE_GRUPPO"))
	private Gruppo gruppo;

	@ManyToMany
	@JoinTable(name = "A_UTENTE_MUNICIPIO", 
		joinColumns = @JoinColumn(name = "ID_UTENTE", foreignKey = @ForeignKey(name = "FK_A_UTENTE_MUNICIPIO_UT")), 
		inverseJoinColumns = @JoinColumn(name = "ID_MUNICIPIO", foreignKey = @ForeignKey(name = "FK_A_UTENTE_MUNICIPIO_MUN")))
	private Set<Municipio> municipi;

	@NotBlank
	@Size(min = 3, max = 30)
	@Column(name = "USERNAME", nullable = false)
	private String username;

	@NotBlank
	@Size(max = 120)
	@Column(name = "PASSWORD_", nullable = false)
	private String password;

	@Size(max = 50)
	@Column(name = "NOME")
	private String nome;

	@Size(max = 50)
	@Column(name = "COGNOME")
	private String cognome;

	@Column(name = "SESSO", length = 1)
	@Enumerated(EnumType.STRING)
	private Sesso sesso;

	@Column(name = "DATA_DI_NASCITA")
	private LocalDate dataDiNascita;

	@Size(max = 50)
	@Column(name = "LUOGO_DI_NASCITA")
	private String luogoDiNascita;

	@Size(max = 50)
	@Column(name = "PROVINCIA_DI_NASCITA")
	private String provinciaDiNascita;

	@NotBlank
	@Size(min = 11, max = 16)
	@Column(name = "CODICE_FISCALE", length = 16, nullable = false)
	private String codiceFiscale;
	
	@Size(max = 100)
	@Column(name = "RAGIONE_SOCIALE")
	private String ragioneSociale;

	@NotBlank
	@Size(max = 50)
	@Email
	@Column(name = "EMAIL")
	private String email;

	@Size(max = 15)
	@Column(name = "NUM_TEL")
	private String numTel;

	@Column(name = "ENABLED", nullable = false, columnDefinition = "boolean default true")
	private boolean enabled;

	@Column(name = "DATE_CREATED", nullable = false)
	private LocalDateTime dateCreated;

	@Column(name = "LAST_LOGIN")
	private LocalDateTime lastLogin;
	
	@Column(name = "FLG_ELIMINATO", nullable = false, columnDefinition = "boolean default false")
	private boolean flagEliminato;
	
	@Column(name = "DATA_ELIMINAZIONE")
	private LocalDateTime dataEliminazione;
	
	@Size(max = 255)
	@Column(name = "UO_ID")
	private String uoId;
	
	@Size(max = 255)
	@Column(name = "INDIRIZZO")
	private String indirizzo;

}
