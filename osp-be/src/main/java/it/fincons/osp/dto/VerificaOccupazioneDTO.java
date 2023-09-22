package it.fincons.osp.dto;

import javax.validation.constraints.NotNull;

import it.fincons.osp.model.TipoOperazioneVerificaOccupazione;
import lombok.Data;

@Data
public class VerificaOccupazioneDTO {

	@NotNull
	private Long idPratica;

	@NotNull
	private Long idUtente;

	@NotNull
	private TipoOperazioneVerificaOccupazione tipoOperazione;

	private GeoMultiPointDTO coordUbicazioneDefinitiva;

	private boolean skipCheck;

}
