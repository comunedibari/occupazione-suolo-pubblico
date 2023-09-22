package it.fincons.osp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import it.fincons.osp.model.Sesso;
import lombok.Data;

@Data
public class UtenteDTO {

	private Long id;

	@NotNull
	private Integer idGruppo;

	private Set<Integer> idsMunicipi;

	@NotBlank
	@Size(min = 3, max = 30)
	private String username;

	// private String password;

	@Size(max = 50)
	private String nome;

	@Size(max = 50)
	private String cognome;

	private Sesso sesso;

	private LocalDate dataDiNascita;

	@Size(max = 50)
	private String luogoDiNascita;

	@Size(max = 50)
	private String provinciaDiNascita;

	@NotBlank
	@Size(min = 11, max = 16)
	private String codiceFiscale;

	@Size(max = 50)
	private String ragioneSociale;

	@NotBlank
	@Size(max = 50)
	@Email
	private String email;

	@Size(max = 15)
	private String numTel;

	private boolean enabled;

	@NotNull
	private LocalDateTime dateCreated;

	private LocalDateTime lastLogin;

	private boolean flagEliminato;

	private LocalDateTime dataEliminazione;

	@Size(max = 255)
	private String uoId;

	@Size(max = 255)
	private String indirizzo;

}
