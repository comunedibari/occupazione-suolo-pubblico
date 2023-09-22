package it.fincons.osp.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AllegatoSimplifiedDTO {

	private Long id;

	private Long idPratica;

	private Long idParere;

	private Long idIntegrazione;
	
	private Long idRichiestaParere;
	
	private Long idRichiestaIntegrazione;

	@NotNull
	private TipoAllegatoDTO tipoAllegato;

	private LocalDateTime dataInserimento;

	private String nota;

	@NotNull
	private String nomeFile;

	@NotNull
	private String mimeType;

	private Integer revisione;

	private String codiceProtocollo;
}
