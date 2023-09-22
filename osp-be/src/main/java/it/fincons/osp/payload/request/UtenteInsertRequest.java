package it.fincons.osp.payload.request;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UtenteInsertRequest extends UtenteCommonRequest {

	@NotBlank
	private String password;
}
