package it.fincons.osp.payload.request;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import it.fincons.osp.dto.DatiRichiestaRettificaDTO;
import it.fincons.osp.dto.RichiedenteRettificaDTO;
import lombok.Data;

@Data
public class PraticaRettificaRequest {

	@NotNull
	private Long id;
	
	@NotNull
	private Long idUtente;

	@NotNull
	private DatiRichiestaRettificaDTO datiRichiesta;

	@NotNull
	private RichiedenteRettificaDTO firmatario;

	private RichiedenteRettificaDTO destinatario;

	private String codiceDeterminaRettifica;
	
	private LocalDate dataEmissioneDeterminaRettifica;
}
