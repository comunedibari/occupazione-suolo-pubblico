package it.fincons.osp.payload.request;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UtenteEditRequest extends UtenteCommonRequest {

	@NotNull
	private Long id;
	
	@NotNull
	private Boolean enabled;
}
