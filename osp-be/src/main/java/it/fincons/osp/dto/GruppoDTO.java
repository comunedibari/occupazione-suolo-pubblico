package it.fincons.osp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class GruppoDTO {

	@NotNull
	private Integer id;

	@NotBlank
	private String descrizione;

	private boolean flagGestInvioMailDetermina;
	private boolean flagGestRichiestaParere;
	private boolean flagInserisciRichiesta;
	private boolean flagConcessioniValide;
	private boolean flagFascicolo;
	private boolean flagPraticaInserita;
	private boolean flagPresaInCarico;
	private boolean flagVerificaPratiche;
	private boolean flagRichiestaPareri;
	private boolean flagNecessariaIntegrazione;
	private boolean flagPraticheApprovate;
	private boolean flagAttesaPagamento;
	private boolean flagPronteRilascio;
	private boolean flagPraticheDaRigettare;
	private boolean flagPratichePreavvisoDiniego;
	private boolean flagPraticheArchiviate;
	private boolean flagPraticheRigettate;
	private boolean flagGestioneProfilo;
	private boolean flagGestioneUtenti;
	private boolean flagVerificaRipristinoLuoghi;
}
