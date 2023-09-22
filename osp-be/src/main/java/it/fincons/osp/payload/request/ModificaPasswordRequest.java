package it.fincons.osp.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ModificaPasswordRequest {

	@NotBlank
	private String username;
	
	@NotBlank
	@Size(max = 40)
	private String oldPassword;
	
	@NotBlank
	@Size(min = 6, max = 40)
	private String password;

}
