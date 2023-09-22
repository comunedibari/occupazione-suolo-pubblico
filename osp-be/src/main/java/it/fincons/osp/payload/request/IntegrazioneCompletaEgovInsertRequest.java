package it.fincons.osp.payload.request;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import it.fincons.osp.dto.AllegatoDTO;
import lombok.Data;

@Data
public class IntegrazioneCompletaEgovInsertRequest {

	@NotNull
	private Long idPratica;

	@NotBlank
	private String numeroProtocollo;
	@NotBlank
	private String anno;
	@NotNull
	private LocalDateTime dataProtocollo;

	@Size(max = 512)
	private String motivoIntegrazione;
	
	@NotNull
	private List<AllegatoDTO> allegati;
	
	private String nomeCittadinoEgov;

	private String cognomeCittadinoEgov;
	
	private String cfCittadinoEgov;
}
