package it.fincons.osp.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class TemplateSimplifiedDTO {

	private Integer id;

	private TypologicalDTO tipoTemplate;

	@NotNull
	@Size(max = 255)
	private String nomeFile;

	@NotNull
	@Size(max = 255)
	private String mimeType;

	private LocalDateTime dataInserimento;

	private LocalDateTime dataModifica;

	private UtenteDTO utenteModifica;
}
