package it.fincons.osp.payload.response;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageResponse {

	private HttpStatus status;
	private String message;
	private Object content;

	public MessageResponse(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

}
