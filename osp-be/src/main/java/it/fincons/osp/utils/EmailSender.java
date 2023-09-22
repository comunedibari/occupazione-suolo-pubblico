package it.fincons.osp.utils;

import java.util.Arrays;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailSender {

	@Value("${spring.mail.host}")
	private String host;

	@Value("${spring.mail.port}")
	private String port;

	@Value("${spring.mail.username}")
	private String username;

	@Value("${osp.app.mail.from}")
	private String emailFrom;

	@Value("${spring.mail.properties.mail.smtp.auth}")
	private String smtpAuth;

	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
	private String starttlsEnable;

	@Autowired
	private JavaMailSender javaMailSender;

	public void send(EmailMessage emailMessage) throws EmailException {

		log.info("send - START");

		try {

			MimeMessage mailMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true, "UTF-8");

			if (!StringUtils.isBlank(emailMessage.getFrom())) {
				helper.setFrom(emailMessage.getFrom());
			} else {
				helper.setFrom(emailFrom);
			}

			if (!StringUtils.isBlank(emailMessage.getReplyTo())) {
				helper.setReplyTo(emailMessage.getReplyTo());
			}

			if (emailMessage.getTo() != null && !emailMessage.getTo().isEmpty()) {
				helper.setTo(emailMessage.getTo().toArray(new String[emailMessage.getTo().size()]));
			} else {
				log.error("Nessun destinatario presente nel messaggio");
				throw new EmailException("Nessun destinatario presente nel messaggio");
			}

			if (emailMessage.getCc() != null && !emailMessage.getCc().isEmpty()) {
				helper.setCc(emailMessage.getCc().toArray(new String[emailMessage.getCc().size()]));
			} else {
				log.info("Nessun destinatario presente in CC");
			}

			if (emailMessage.getBcc() != null && !emailMessage.getBcc().isEmpty()) {
				helper.setBcc(emailMessage.getBcc().toArray(new String[emailMessage.getBcc().size()]));
			} else {
				log.info("Nessun destinatario presente in BCC");
			}

			if (emailMessage.getSentDate() != null) {
				helper.setSentDate(emailMessage.getSentDate());
			}

			if (emailMessage.getSubject() != null) {
				mailMessage.setSubject(emailMessage.getSubject(), "UTF-8");
			}

			if (emailMessage.getText() != null) {
				helper.setText(emailMessage.getText(), true);
			}

			if (emailMessage.getLogo() != null) {
				helper.addInline("logo_comune_bari", emailMessage.getLogo());
			}

			if (emailMessage.getLogoFooter() != null) {
				helper.addInline("logo_footer_comune_bari", emailMessage.getLogoFooter());
			}

			// attachments
			if (emailMessage.getAttachments() != null) {
				for (EmailAttachment emailAttachment : emailMessage.getAttachments()) {
					helper.addAttachment(emailAttachment.getFileName(),
							new ByteArrayResource(emailAttachment.getFileAttachment()));
				}
			}

			javaMailSender.send(mailMessage);

			log.info("Email mandata ai seguenti destinatari: "
					+ Arrays.toString(emailMessage.getTo().toArray(new String[emailMessage.getTo().size()])));

		} catch (Exception e) {
			log.error("Email sender exception", e);
			log.error("Current configuration - spring.mail.host: [" + host + "], spring.mail.port: [" + port
					+ "], spring.mail.username: [" + username + "], osp.app.mail.from [" + emailFrom
					+ "], spring.mail.properties.mail.smtp.auth [" + smtpAuth
					+ "], spring.mail.properties.mail.smtp.starttls.enable [" + starttlsEnable + "]");
			throw new EmailException("MessagingException", e);
		}
		log.info("send - END");
	}

//	public void sendTest() {
//
//		SimpleMailMessage msg = new SimpleMailMessage();
//		msg.setTo("alfonso.coppola@finconsgroup.com");
//
//		msg.setSubject("Testing from Spring Boot");
//		msg.setText("Hello World \n Spring Boot Email");
//		msg.setFrom("pippo@mail.it");
//
//		javaMailSender.send(msg);
//
//	}
//
//	void sendEmailWithAttachmentTest() throws MessagingException, IOException {
//
//		MimeMessage msg = javaMailSender.createMimeMessage();
//
//		// true = multipart message
//		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
//
//		helper.setTo("to_@email");
//
//		helper.setSubject("Testing from Spring Boot");
//
//		// default = text/plain
//		// helper.setText("Check attachment for image!");
//
//		// true = text/html
//		helper.setText("<h1>Check attachment for image!</h1>", true);
//
//		// hard coded a file path
//		// FileSystemResource file = new FileSystemResource(new
//		// File("path/android.png"));
//
//		helper.addAttachment("my_photo.png", new ClassPathResource("android.png"));
//
//		javaMailSender.send(msg);
//
//	}

}
