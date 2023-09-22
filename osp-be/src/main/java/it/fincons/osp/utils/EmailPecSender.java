package it.fincons.osp.utils;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.validation.constraints.NotNull;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import it.fincons.osp.config.CustomSSLSocketFactory;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailPecSender {

	private static String logPattern = "[MAIL Service]";

	@Value("${pec.hostname}")
	@NotNull
	private String pecHostName;

	@Value("${pec.port}")
	@NotNull
	private int pecPort;

	@Value("${pecM1.auth.username}")
	@NotNull
	private String pecM1AuthUsername;
	@Value("${pecM1.auth.password}")
	@NotNull
	private String pecM1AuthPassword;

	@Value("${pecM2.auth.username}")
	@NotNull
	private String pecM2AuthUsername;
	@Value("${pecM1.auth.password}")
	@NotNull
	private String pecM2AuthPassword;

	@Value("${pecM3.auth.username}")
	@NotNull
	private String pecM3AuthUsername;
	@Value("${pecM3.auth.password}")
	@NotNull
	private String pecM3AuthPassword;

	@Value("${pecM4.auth.username}")
	@NotNull
	private String pecM4AuthUsername;
	@Value("${pecM4.auth.password}")
	@NotNull
	private String pecM4AuthPassword;

	@Value("${pecM5.auth.username}")
	@NotNull
	private String pecM5AuthUsername;
	@Value("${pecM5.auth.password}")
	@NotNull
	private String pecM5AuthPassword;

	@Autowired
	private JavaMailSender javaMailSender;

	public void sendMailPEC(EmailMessage emailMessage) throws it.fincons.osp.utils.EmailException {
		log.info("{} - Sending PEC mail to {} recipients", logPattern, emailMessage.getTo().size());
		// Prepare system properties
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.ssl.enable", "true");
		props.setProperty("mail.smtp.ssl.socketFactory.class", CustomSSLSocketFactory.class.getName());
		props.setProperty("mail.smtp.ssl.socketFactory.fallback", "false");

		emailMessage.getTo().forEach(recipient -> {
			try {
				// Prepare email
				HtmlEmail email = new HtmlEmail();

				email.setHostName(pecHostName);
				email.setSmtpPort(pecPort);
				switch (emailMessage.getMunicipio()) {
					case 1:
						email.setAuthentication(pecM1AuthUsername, pecM1AuthPassword);
						break;
					case 2:
						email.setAuthentication(pecM2AuthUsername, pecM2AuthPassword);
						break;
					case 3:
						email.setAuthentication(pecM3AuthUsername, pecM3AuthPassword);
						break;
					case 4:
						email.setAuthentication(pecM4AuthUsername, pecM4AuthPassword);
						break;
					case 5:
						email.setAuthentication(pecM5AuthUsername, pecM5AuthPassword);
						break;
					default:
						throw new it.fincons.osp.utils.EmailException("No municipio found.");
				}
				email.setSSL(true);

				if(emailMessage.getFrom()==null||"".equals(emailMessage.getFrom())){
					throw new it.fincons.osp.utils.EmailException("Empty pec from. 	");
				}else{
					email.setFrom(emailMessage.getFrom());
				}
				if (emailMessage.getCc() != null && !emailMessage.getCc().isEmpty()) {
					email.setCc(emailMessage.getCc().stream().map(
						el -> {
							try {
								return new InternetAddress(el);
							} catch (AddressException e) {
								throw new RuntimeException("Unparsable Email: " + el);
							}
						}).collect(Collectors.toList()));
				} else {
					log.info("Nessun destinatario presente in CC");
				}

				email.setCharset("UTF-8");
				email.setHtmlMsg(emailMessage.getText());
				email.setSubject(emailMessage.getSubject());
				email.addTo(recipient, recipient);

				// logo
				if (emailMessage.getLogo() != null) {
					email.embed(emailMessage.getLogo(), "logo_comune_bari");
				}

				// logo footer
				if (emailMessage.getLogoFooter() != null) {
					email.embed(emailMessage.getLogoFooter(), "logo_footer_comune_bari");
				}

				// attachments
				if (emailMessage.getAttachments() != null) {
					for (EmailAttachment currentEmailAttachment : emailMessage.getAttachments()) {
						DataSource source = new ByteArrayDataSource(currentEmailAttachment.getFileAttachment(),
								currentEmailAttachment.getMimeType());
						email.attach(source, currentEmailAttachment.getFileName(), "");
					}
				}

//                for (MailAttachment attachment : Optional.ofNullable(model.getAttachments()).orElse(Collections.emptyList())) {
//                    EmailAttachment emailAttachment = new EmailAttachment();
//                    emailAttachment.setDisposition(EmailAttachment.ATTACHMENT);
//                    emailAttachment.setURL(new URL(attachment.getURL()));
//                    emailAttachment.setName(attachment.getFileName());
//                    emailAttachment.setDescription(attachment.getDescription());
//                    email.attach(new ByteArrayResource(emailAttachment.getFileAttachment())
//                }

				// Finally send email to recipient
				email.send();
			} catch (EmailException e) {
				log.error("{} - Failed to send PEC mail! ({} error)", logPattern, e.getClass().getName());
				log.error("{} - Error message: {}. Caused by: {}", logPattern, e.getMessage(), e.getCause());
				throw new it.fincons.osp.utils.EmailException("MessagingException", e);
			}
		});

		log.info("{} - Sent PEC mails to {} recipients", logPattern, emailMessage.getTo().size());
	}

}
