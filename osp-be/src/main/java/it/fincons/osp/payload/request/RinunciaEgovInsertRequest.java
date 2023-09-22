package it.fincons.osp.payload.request;

import java.util.List;

import javax.validation.constraints.NotNull;

import it.fincons.osp.dto.AllegatoDTO;
import lombok.Data;

@Data
public class RinunciaEgovInsertRequest {

	@NotNull
	private PraticaRinunciaEgovInsertEditRequest pratica;
	
	@NotNull
	private List<AllegatoDTO> allegati;
	
}
