package it.fincons.osp.dto;

import lombok.Data;

@Data
public class TipoAllegatoGruppoStatoProcessoDTO {
	
	private TipoAllegatoDTO tipoAllegato;

	private boolean flagObbligatorio;

	private boolean flagTestoLibero;

	private Integer idStatoPratica;

	private Integer idTipoProcesso;

	private Integer idGruppo;

}
