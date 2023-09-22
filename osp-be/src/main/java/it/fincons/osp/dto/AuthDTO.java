package it.fincons.osp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AuthDTO {

	@JsonProperty("gestInvioMailDetermina")
	private boolean flagGestInvioMailDetermina;

	@JsonProperty("gestRichiestaParere")
	private boolean flagGestRichiestaParere;

	@JsonProperty("inserisciRichiesta")
	private boolean flagInserisciRichiesta;

	@JsonProperty("concessioniValide")
	private boolean flagConcessioniValide;

	@JsonProperty("fascicolo")
	private boolean flagFascicolo;

	@JsonProperty("praticaInserita")
	private boolean flagPraticaInserita;

	@JsonProperty("verificaPratiche")
	private boolean flagVerificaPratiche;

	@JsonProperty("richiestaPareri")
	private boolean flagRichiestaPareri;

	@JsonProperty("necessariaIntegrazione")
	private boolean flagNecessariaIntegrazione;

	@JsonProperty("praticheApprovate")
	private boolean flagPraticheApprovate;

	@JsonProperty("attesaPagamento")
	private boolean flagAttesaPagamento;

	@JsonProperty("pronteRilascio")
	private boolean flagPronteRilascio;

	@JsonProperty("praticheDaRigettare")
	private boolean flagPraticheDaRigettare;

	@JsonProperty("pratichePreavvisoDiniego")
	private boolean flagPratichePreavvisoDiniego;

	@JsonProperty("praticheArchiviate")
	private boolean flagPraticheArchiviate;

	@JsonProperty("praticheRigettate")
	private boolean flagPraticheRigettate;

	@JsonProperty("gestioneProfilo")
	private boolean flagGestioneProfilo;

	@JsonProperty("gestioneUtenti")
	private boolean flagGestioneUtenti;
	
	@JsonProperty("verificaRipristinoLuoghi")
	private boolean flagVerificaRipristinoLuoghi;

}
