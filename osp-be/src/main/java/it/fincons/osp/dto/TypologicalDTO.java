package it.fincons.osp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class TypologicalDTO {

	@NotNull
	private Integer id;

	@NotBlank
	private String descrizione;

	@NotBlank
	private String descrizioneEstesa;
	
}
