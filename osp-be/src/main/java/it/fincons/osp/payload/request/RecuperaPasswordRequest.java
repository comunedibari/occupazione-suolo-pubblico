package it.fincons.osp.payload.request;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class RecuperaPasswordRequest {

	@NotBlank
	private String username;

}
