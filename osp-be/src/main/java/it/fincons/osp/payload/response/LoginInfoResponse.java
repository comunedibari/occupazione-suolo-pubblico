package it.fincons.osp.payload.response;

import it.fincons.osp.dto.GroupsDTO;
import it.fincons.osp.dto.UserLoginDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginInfoResponse {

	private boolean auth;
	private String token;
	private String username;
	private GroupsDTO groups;
	private UserLoginDTO userLogged;
}
