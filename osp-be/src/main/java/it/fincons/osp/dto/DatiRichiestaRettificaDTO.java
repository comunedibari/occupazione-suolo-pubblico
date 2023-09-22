package it.fincons.osp.dto;

import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class DatiRichiestaRettificaDTO {
	
	private String noteUbicazione;

	private Double superficieAreaMq;

	private Double larghezzaM;

	private Double lunghezzaM;

	private Double superficieMarciapiedeMq;

	private Double larghezzaMarciapiedeM;

	private Double lunghezzaMarciapiedeM;

	private Double larghezzaCarreggiataM;

	private Double lunghezzaCarreggiataM;

	private Boolean stalloDiSosta;

	private Boolean presScivoliDiversamenteAbili;

	private Boolean presPassiCarrabiliDiversamenteAbili;

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

	private boolean flagEsenzioneMarcaDaBollo = false;

	private String motivazioneEsenzioneMarcaBollo;
	
}
