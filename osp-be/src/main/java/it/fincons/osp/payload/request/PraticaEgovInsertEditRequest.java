package it.fincons.osp.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class PraticaEgovInsertEditRequest extends PraticaInsertEditRequest{
	@NotBlank
	private String numeroProtocollo;
	@NotBlank
	private String anno;
	@NotBlank
	private LocalDateTime dataProtocollo;
}
