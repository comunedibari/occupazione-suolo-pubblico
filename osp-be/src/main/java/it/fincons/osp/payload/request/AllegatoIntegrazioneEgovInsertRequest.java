package it.fincons.osp.payload.request;

import javax.validation.constraints.NotNull;

import it.fincons.osp.dto.AllegatoDTO;
import lombok.Data;

@Data
public class AllegatoIntegrazioneEgovInsertRequest {
	
	Long idPratica;
	
	String codiceProtocollo;
	
	@NotNull
	private AllegatoDTO allegato;
	
}
