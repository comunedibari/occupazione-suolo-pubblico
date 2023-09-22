package it.fincons.osp.payload.protocollazione.request;

import lombok.Data;

@Data
public class DocumentoDTO {
	private String nomeFile;
	protected String titolo;
	private byte[] contenuto;
}
