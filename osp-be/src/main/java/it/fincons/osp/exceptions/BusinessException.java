package it.fincons.osp.exceptions;

import java.util.Arrays;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = -7920666220219359140L;

	private final ErrorCode code;
	private final Object[] parameter;

	public BusinessException(ErrorCode code, String message, Object... parameter) {
		super(message);
		this.code = code;
		this.parameter = parameter;
	}

	@Override
	public String toString() {
		return "BusinessException [code=" + code + ", message=" + super.getMessage() + ", parameter="
				+ Arrays.toString(parameter) + "]";
	}

}
