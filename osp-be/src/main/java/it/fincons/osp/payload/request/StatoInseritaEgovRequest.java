package it.fincons.osp.payload.request;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class StatoInseritaEgovRequest {
	
	@NotNull
	private Long idPratica;
	
	private String nomeCittadinoEgov;

	private String cognomeCittadinoEgov;
	
	private String cfCittadinoEgov;
}
