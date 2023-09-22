package it.fincons.osp.payload.request;

import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class IntegrazioneEgovInsertRequest {
	
	Long idPratica;
	
	String codiceProtocollo;
	
	@Size(max = 512)
	private String motivoIntegrazione;
	
	private String nomeCittadinoEgov;

	private String cognomeCittadinoEgov;
	
	private String cfCittadinoEgov;
}
