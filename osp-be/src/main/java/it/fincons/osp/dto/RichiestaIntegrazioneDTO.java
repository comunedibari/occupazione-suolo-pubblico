package it.fincons.osp.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import it.fincons.osp.model.TipoRichiestaIntegrazione;
import lombok.Data;

@Data
public class RichiestaIntegrazioneDTO {
	private Long id;

	@NotNull
	private Long idPratica;

	@NotNull
	private Long idUtenteRichiedente;

	private Integer idStatoPratica;

	@Size(max = 100)
	private String codiceProtocollo;
	
	private LocalDateTime dataProtocollo;

	private TipoRichiestaIntegrazione tipoRichiesta;

	@Size(max = 512)
	private String motivoRichiesta;

	private LocalDateTime dataInserimento;

	private LocalDateTime dataScadenza;
	
	private IntegrazioneDTO integrazione;

	private String flagEsenzioneMarcaDaBollo;

	private Long idAllegato;

}
