package it.fincons.osp.payload.request;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class LogoutRequest {

	@NotBlank
	private String username;

}
