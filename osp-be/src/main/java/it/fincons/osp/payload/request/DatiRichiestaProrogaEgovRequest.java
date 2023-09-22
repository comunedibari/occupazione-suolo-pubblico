package it.fincons.osp.payload.request;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class DatiRichiestaProrogaEgovRequest {

	@NotNull
	private LocalDate dataInizioOccupazione;

	private LocalTime oraInizioOccupazione;
	@NotNull
	private LocalDate dataScadenzaOccupazione;

	private LocalTime oraScadenzaOccupazione;

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

	@NotNull
	@AssertTrue
	private Boolean flagNonModificheRispettoConcessione;

	private boolean flagEsenzioneMarcaDaBollo;
	private String motivazioneEsenzioneMarcaBollo;

}
