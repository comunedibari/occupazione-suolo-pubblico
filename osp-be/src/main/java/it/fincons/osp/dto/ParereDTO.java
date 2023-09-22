package it.fincons.osp.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ParereDTO {
	
	private Long id;

	@NotNull
	private Long idRichiestaParere;

	@NotNull
	private Long idUtenteParere;

	@Size(max = 255)
	private String nota;

	private Boolean esito;

	private String codiceProtocollo;
	
	private LocalDateTime dataProtocollo;

	private LocalDateTime dataInserimento;
	
	private Boolean flagCompetenza;
}
