package it.fincons.osp.payload.request;

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.*;

import it.fincons.osp.model.Sesso;
import lombok.Data;

@Data
public abstract class UtenteCommonRequest {

	@NotBlank
	@Size(min = 3, max = 30)
	private String username;

	@NotBlank
	@Size(max = 50)
	@Email
	private String email;

	@NotNull
	private Integer idGruppo;

	private List<Integer> idsMunicipi;

	private String nome;

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

	@Size(max = 100)
	private String ragioneSociale;

	@Size(max = 15)
	private String numTel;

	@Size(max = 255)
	private String uoId;
	
	@Size(max = 255)
	private String indirizzo;

	@Pattern(regexp = "^$|^true$|^false$")
	private String skipCheckConcessionario;
}
