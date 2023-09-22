package it.fincons.osp.dto;

import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class RichiedenteRettificaDTO {

	@Size(max = 50)
	private String nome;

	@Size(max = 50)
	private String cognome;

	@Size(max = 50)
	private String denominazione;

	@Size(max = 16)
	private String codiceFiscalePartitaIva;

	private LocalDate dataDiNascita;

	@Size(max = 50)
	private String comuneDiNascita;

	@Size(max = 50)
	private String provinciaDiNascita;

	@Size(max = 50)
	private String nazionalita;

	@Size(max = 50)
	private String citta;

	@Size(max = 100)
	private String indirizzo;

	@Size(max = 10)
	private String civico;

	@Size(max = 50)
	private String provincia;

	@Size(max = 5)
	private String cap;

	@Size(max = 15)
	private String recapitoTelefonico;

	@Size(max = 50)
	@Email
	private String email;

	private Integer idTipoDocumentoAllegato;

	@Size(max = 50)
	private String numeroDocumentoAllegato;

	@Size(max = 50)
	private String amministrazioneDocumentoAllegato;

}
