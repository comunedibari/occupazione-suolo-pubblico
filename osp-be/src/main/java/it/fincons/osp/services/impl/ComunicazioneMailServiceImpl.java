package it.fincons.osp.services.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import it.fincons.osp.config.OspConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.ComunicazioneMailInsertDTO;
import it.fincons.osp.model.ComunicazioneMail;
import it.fincons.osp.repository.ComunicazioneMailRepository;
import it.fincons.osp.services.ComunicazioneMailService;
import it.fincons.osp.utils.EmailAttachment;
import it.fincons.osp.utils.EmailMessage;
import it.fincons.osp.utils.EmailPecSender;
import it.fincons.osp.utils.EmailSender;
import lombok.extern.slf4j.Slf4j;

import javax.mail.MessagingException;
import javax.validation.constraints.NotNull;

@Service
@Slf4j
public class ComunicazioneMailServiceImpl implements ComunicazioneMailService {

	@Value("${osp.app.mail.from}")
	private String emailFrom;

	@Value("classpath:images/logo_comune_bari.png")
	private Resource resourceFileLogo;
	
	@Value("classpath:images/logo_footer_comune_bari.png")
	private Resource resourceFileLogoFooter;

	@Autowired
	ComunicazioneMailRepository comunicazioneMailRepository;

	@Autowired
	EmailSender emailSender;

	@Autowired
	EmailPecSender emailPecSender;

	@Autowired
	private OspConfiguration ospConfiguration;

	@Override
	@LogEntryExit
	public void insertComunicazioneMail(ComunicazioneMailInsertDTO comunicazioneMailInsert) {
		ComunicazioneMail comunicazioneMail = new ComunicazioneMail();

		comunicazioneMail.setPratica(comunicazioneMailInsert.getPratica());
		comunicazioneMail.setRichiestaParere(comunicazioneMailInsert.getRichiestaParere());
		comunicazioneMail.setRichiestaIntegrazione(comunicazioneMailInsert.getRichiestaIntegrazione());
		comunicazioneMail.setDestinatari(comunicazioneMailInsert.getDestinatari());
		comunicazioneMail.setDestinatariCc(comunicazioneMailInsert.getDestinatariCc());
		comunicazioneMail.setOggetto(comunicazioneMailInsert.getOggetto());
		comunicazioneMail.setTesto(comunicazioneMailInsert.getTesto());
		comunicazioneMail.setNumeroTentativiInvio(0);
		comunicazioneMail.setFlagInviata(false);
		comunicazioneMail.setDataInserimento(LocalDateTime.now().withNano(0));
		comunicazioneMail.setFlagPec(comunicazioneMailInsert.isFlagPec());
		comunicazioneMail.setNomeFileAllegato(comunicazioneMailInsert.getNomeFileAllegato());
		comunicazioneMail.setMimeTypeFileAllegato(comunicazioneMailInsert.getMimeTypeFileAllegato());
		comunicazioneMail.setFileAllegato(comunicazioneMailInsert.getFileAllegato());

		comunicazioneMailRepository.save(comunicazioneMail);
	}

	@Override
	@LogEntryExit
	public void sendComunicazioniMail() {
		List<ComunicazioneMail> listaComunicazioniMail = comunicazioneMailRepository
				.findByFlagInviataFalseOrderByDataInserimentoAsc();

		for (ComunicazioneMail comunicazioneMail : listaComunicazioniMail) {
			EmailMessage emailMessage = new EmailMessage();
			emailMessage.setFrom(emailFrom);
			emailMessage.setSubject(comunicazioneMail.getOggetto());
			emailMessage.setText(comunicazioneMail.getTesto());
			if(comunicazioneMail.getPratica()!=null&&
					comunicazioneMail.getPratica().getMunicipio()!=null){
				emailMessage.setMunicipio(comunicazioneMail.getPratica().getMunicipio().getId());
			}

			List<String> to = Arrays.asList(comunicazioneMail.getDestinatari().split(","));
			emailMessage.setTo(to);

			if (!StringUtils.isBlank(comunicazioneMail.getDestinatariCc())) {
				List<String> cc = Arrays.asList(comunicazioneMail.getDestinatariCc().split(","));
				emailMessage.setCc(cc);
			}

			try {
				emailMessage.setLogo(resourceFileLogo.getFile());
			} catch (IOException e) {
				log.warn("Non è stato possibile leggere l'immagine del logo", e);
			}
			
			try {
				emailMessage.setLogoFooter(resourceFileLogoFooter.getFile());
			} catch (IOException e) {
				log.warn("Non è stato possibile leggere l'immagine del logo del footer", e);
			}

			if (comunicazioneMail.getNomeFileAllegato() != null && comunicazioneMail.getFileAllegato() != null) {
				EmailAttachment emailAttachment = new EmailAttachment(comunicazioneMail.getNomeFileAllegato(),
						comunicazioneMail.getMimeTypeFileAllegato(), comunicazioneMail.getFileAllegato());
				emailMessage.setAttachments(List.of(emailAttachment));
			}

			comunicazioneMail.setNumeroTentativiInvio(comunicazioneMail.getNumeroTentativiInvio() + 1);
			comunicazioneMailRepository.save(comunicazioneMail);

			if (comunicazioneMail.isFlagPec()) {

				if(ospConfiguration.getSenderMailMunicipio()!=null&&
						comunicazioneMail.getPratica()!=null&&
						comunicazioneMail.getPratica().getMunicipio()!=null&&
						ospConfiguration.getSenderMailMunicipio().containsKey(comunicazioneMail.getPratica().getMunicipio().getId().toString())){
					emailMessage.setFrom(ospConfiguration.getSenderMailMunicipio().get(comunicazioneMail.getPratica().getMunicipio().getId().toString()));
				}else{
					emailMessage.setFrom(null);
				}
				emailPecSender.sendMailPEC(emailMessage);
				comunicazioneMail.setFlagInviata(true);
				comunicazioneMail.setDataInvio(LocalDateTime.now().withNano(0));
				comunicazioneMailRepository.save(comunicazioneMail);
			} else {
				emailSender.send(emailMessage);
				comunicazioneMail.setFlagInviata(true);
				comunicazioneMail.setDataInvio(LocalDateTime.now().withNano(0));
				comunicazioneMailRepository.save(comunicazioneMail);
			}
		}
	}

}
