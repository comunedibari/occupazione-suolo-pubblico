package it.fincons.osp.payload.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class DateOccupazioneEditRequest {
	
	@NotNull
	private Long idPratica;
	private String numeroProtocollo;
	private String annoProtocollo;
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
