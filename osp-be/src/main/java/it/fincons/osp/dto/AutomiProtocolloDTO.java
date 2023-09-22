package it.fincons.osp.dto;

import it.fincons.osp.model.Municipio;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AutomiProtocolloDTO {
	private Long id;

	@NotBlank
	private String uoId;

	private TypologicalDTO municipio;

	@NotBlank
	private String label;

	private String denominazione;

	private LocalDateTime dataInserimento;

	private LocalDateTime dataModifica;

}
