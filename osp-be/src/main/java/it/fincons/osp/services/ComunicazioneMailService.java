package it.fincons.osp.services;

import it.fincons.osp.dto.ComunicazioneMailInsertDTO;
import it.fincons.osp.model.ComunicazioneMail;

import javax.mail.MessagingException;

public interface ComunicazioneMailService {

	/**
	 * Crea e salva un nuovo oggetto {@link ComunicazioneMail}
	 * 
	 * @param comunicazioneMailInsert
	 */
	public void insertComunicazioneMail(ComunicazioneMailInsertDTO comunicazioneMailInsert);

	/**
	 * Controlla gli oggetti {@link ComunicazioneMail} non ancora inviati, invia le
	 * rispettive mail ed aggiorna le informazioni degli oggetti
	 */
	public void sendComunicazioniMail();

}
