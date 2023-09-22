package it.fincons.osp.exceptions;

public class CiviliarioServiceException extends Exception {

	private static final long serialVersionUID = -6502828787667628978L;

	public CiviliarioServiceException(String errorMessage) {
		super(errorMessage);
	}
}
