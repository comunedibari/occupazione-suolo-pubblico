package it.fincons.osp.payload.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class RettificaDateEgovInsertRequest {

	@NotNull
	private Long idPratica;

	@NotBlank
	private String numeroProtocollo;
	@NotBlank
	private String anno;
	@NotNull
	private LocalDateTime dataProtocollo;
	
	@NotNull
	private LocalDate dataInizioOccupazione;

	private LocalTime oraInizioOccupazione;

	@NotNull
	private LocalDate dataScadenzaOccupazione;

	private LocalTime oraScadenzaOccupazione;
	
	private String nomeCittadinoEgov;

	private String cognomeCittadinoEgov;

	private String cfCittadinoEgov;

	private boolean originEgov;
}
