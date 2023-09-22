package it.fincons.osp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class IntegrazioneDTO {

	private Long id;

	@NotNull
	private Long idRichiestaIntegrazione;
	
	@NotNull
	private Long idUtenteIntegrazione;

	@Size(max = 512)
	private String motivoIntegrazione;

	private String codiceProtocollo;

	private String numeroProtocollo;

	private String anno;

	private LocalDateTime dataProtocollo;

	private LocalDateTime dataInserimento;
	
	private LocalDate dataInizioOccupazione;

	private LocalTime oraInizioOccupazione;

	private LocalDate dataScadenzaOccupazione;

	private LocalTime oraScadenzaOccupazione;
	
	private String nomeCittadinoEgov;

	private String cognomeCittadinoEgov;
	
	private String cfCittadinoEgov;

	private TypologicalDTO statoPratica;

	private boolean originEgov;
}
