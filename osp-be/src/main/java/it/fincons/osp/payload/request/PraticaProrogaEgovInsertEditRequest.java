package it.fincons.osp.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import it.fincons.osp.dto.MarcaBolloPraticaDTO;
import it.fincons.osp.dto.RichiedenteDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PraticaProrogaEgovInsertEditRequest {

	@NotNull
	private Long idPraticaOriginaria;
	private Long idProrogaPrecedente;

	@NotBlank
	private String numeroProtocollo;
	@NotBlank
	private String anno;
	@NotNull
	private LocalDateTime dataProtocollo;
	
	@NotNull
	private Integer idMunicipio;
	
	@NotNull
	private RichiedenteDTO firmatario;
	
	@NotNull
	private DatiRichiestaProrogaEgovRequest datiRichiesta;

	private String nomeCittadinoEgov;

	private String cognomeCittadinoEgov;

	private String cfCittadinoEgov;

	private boolean originEgov;

	private MarcaBolloPraticaDTO marcaBolloPratica;

}
