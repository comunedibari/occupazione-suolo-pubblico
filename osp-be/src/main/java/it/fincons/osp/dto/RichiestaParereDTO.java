package it.fincons.osp.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class RichiestaParereDTO {

	private Long id;

	@NotNull
	private Long idPratica;

	@NotNull
	private Long idUtenteRichiedente;

	private Integer idStatoPratica;

	private Integer idGruppoDestinatarioParere;

	private String codiceProtocollo;
	
	private LocalDateTime dataProtocollo;

	@Size(max = 512)
	private String notaRichiestaParere;

	private LocalDateTime dataInserimento;
	
	private boolean flagInseritaRisposta;
	
	private ParereDTO parere;

	private boolean riabilitaEsenzioneMarcaDaBollo;

}
