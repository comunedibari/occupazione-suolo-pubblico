package it.fincons.osp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TipoAllegatoDTO extends TypologicalDTO {

	private String descrizioneEstesa;

}
