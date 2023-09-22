package it.fincons.osp.payload.request;

import java.util.List;

import javax.validation.constraints.NotNull;

import it.fincons.osp.dto.AllegatoDTO;
import lombok.Data;

@Data
public class PraticaEgovInsertRequest {

	@NotNull
	private PraticaEgovInsertEditRequest pratica;
	
	@NotNull
	private List<AllegatoDTO> allegati;

}
