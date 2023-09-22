package it.fincons.osp.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PraticaRinunciaEgovInsertEditRequest {
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
	private String motivazioneRichiesta;
	
	@NotNull
	private DatiRichiestaRinunciaEgovRequest datiRichiesta;
	
	private String nomeCittadinoEgov;

	private String cognomeCittadinoEgov;

	private String cfCittadinoEgov;

	private boolean originEgov;
}
