package it.fincons.osp.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.fincons.osp.model.TipoOperazioneVerificaOccupazione;
import lombok.Data;

@Data
public class DatiRichiestaDTO {

	private Long id;

	private boolean flagEsenzioneMarcaDaBollo;
	private boolean flagEsenzioneMarcaDaBolloModificato;
	private String motivazioneEsenzioneMarcaBollo;


	@NotBlank
	@Size(max = 300)
	private String ubicazioneOccupazione;

	@JsonProperty("nome_via")
	@Size(max = 255)
	private String nomeVia;

	@JsonProperty("numero")
	@Size(max = 5)
	private String numeroVia;

	@JsonProperty("cod_via")
	@Size(max = 20)
	private String codVia;

	private Integer idMunicipio;

	@Size(max = 50)
	private String localita;

	@NotNull
	private boolean flagNumeroCivicoAssente;
	
	private String noteUbicazione;

	@NotNull
	private Double superficieAreaMq;

	@NotNull
	private Double larghezzaM;

	@NotNull
	private Double lunghezzaM;

	@NotNull
	private Double superficieMarciapiedeMq;

	private Double larghezzaMarciapiedeM;

	private Double lunghezzaMarciapiedeM;

	private Double larghezzaCarreggiataM;

	private Double lunghezzaCarreggiataM;

	private Boolean stalloDiSosta;

	private Boolean presScivoliDiversamenteAbili;

	private Boolean presPassiCarrabiliDiversamenteAbili;

	@NotNull
	private LocalDate dataInizioOccupazione;

	private LocalTime oraInizioOccupazione;

	@NotNull
	private LocalDate dataScadenzaOccupazione;

	private LocalTime oraScadenzaOccupazione;

	@NotNull
	private Integer idAttivitaDaSvolgere;

	private Integer idTipologiaTitoloEdilizio;

	@Size(max = 100)
	private String descrizioneTitoloEdilizio;

	@Size(max = 100)
	private String riferimentoTitoloEdilizio;

	@Size(max = 255)
	private String descrizioneAttivitaDaSvolgere;

	private Integer idManufatto;

	@Size(max = 255)
	private String descrizioneManufatto;

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
	private Boolean flagNonModificheRispettoConcessione;
	
	private GeoMultiPointDTO coordUbicazioneTemporanea;
	
	private GeoMultiPointDTO coordUbicazioneDefinitiva;
	
	private TipoOperazioneVerificaOccupazione tipoOperazioneVerificaOccupazione;
	
}
