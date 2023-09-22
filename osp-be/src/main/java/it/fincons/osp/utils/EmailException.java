package it.fincons.osp.utils;

public class EmailException extends RuntimeException {

	private static final long serialVersionUID = 537169120384147007L;

	public EmailException(String message) {
		super(message);
	}

	public EmailException(String message, Throwable cause) {
		super(message, cause);
	}
}
