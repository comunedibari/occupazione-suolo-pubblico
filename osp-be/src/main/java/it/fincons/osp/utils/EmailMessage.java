package it.fincons.osp.utils;

import java.io.File;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class EmailMessage {

	private String from;

	private String replyTo;

	private List<String> to;

	private List<String> cc;

	private List<String> bcc;

	private Date sentDate;

	private String subject;

	private String text;
	
	private File logo;
	
	private File logoFooter;

	private List<EmailAttachment> attachments;

	private Integer municipio;
	
	public EmailMessage() {
	}

	public EmailMessage(EmailMessage source) {
		this.from = source.from;
		this.subject = source.subject;
		this.text = source.text;
	}

}
