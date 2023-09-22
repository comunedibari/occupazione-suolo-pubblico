package it.fincons.osp.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PraticaDTO extends PraticaSimplifiedDTO {
	private String codiceProtocollo;
	
	private List<RichiestaParereDTO> richiestePareri;
	
	private List<RichiestaIntegrazioneDTO> richiesteIntegrazioni;
}
