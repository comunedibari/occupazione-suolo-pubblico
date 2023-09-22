package it.fincons.osp.payload.request;

import it.fincons.osp.dto.AllegatoDTO;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PraticaEgovValidazioneRequest {

	private PraticaInsertEditRequest pratica;

	private String dimensioneTotaleAllegatiProtocolloKb;

}
