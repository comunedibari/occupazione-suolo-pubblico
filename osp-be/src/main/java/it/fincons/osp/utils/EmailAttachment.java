package it.fincons.osp.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailAttachment {

	private String fileName;
	
	private String mimeType;
	
	private byte[] fileAttachment;
}
