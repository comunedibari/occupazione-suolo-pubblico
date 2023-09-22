package it.fincons.osp.payload.request;

import it.fincons.osp.dto.AllegatoDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AllegatoRichiestaIntegrazioneRequest {
	@NotNull
	private AllegatoDTO allegato;
}
