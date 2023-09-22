package it.fincons.osp.payload.request;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class DatiRichiestaRinunciaEgovRequest {

	@NotNull
	@AssertTrue
	private Boolean flagAccettazioneRegSuoloPubblico;

	@NotNull
	@AssertTrue
	private Boolean flagRispettoInteresseTerzi;

	@NotNull
	@AssertTrue
	private Boolean flagObbligoRiparazioneDanni;

	@NotNull
	@AssertTrue
	private Boolean flagRispettoDisposizioniRegolamento;

	@NotNull
	@AssertTrue
	private Boolean flagConoscenzaTassaOccupazione;

}
