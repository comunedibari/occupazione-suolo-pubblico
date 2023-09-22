package it.fincons.osp.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class MarcaBolloPraticaDTO {
	private Long id;

	@NotBlank
	private String iuv;
	@NotBlank
	private String improntaFile;
	@NotNull
	private double importoPagato;
	@NotBlank
	private String causalePagamento;
	@NotBlank
	private String idRichiesta;
	@NotNull
	private LocalDate dataOperazione;

}
