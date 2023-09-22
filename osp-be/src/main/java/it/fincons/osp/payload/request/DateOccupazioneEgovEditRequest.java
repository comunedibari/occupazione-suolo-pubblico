package it.fincons.osp.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class DateOccupazioneEgovEditRequest {

	private Long idPratica;

	@NotNull
	private String numeroProtocollo;
	@NotNull
	private LocalDateTime dataProtocollo;
	@NotNull
	private String anno;

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
