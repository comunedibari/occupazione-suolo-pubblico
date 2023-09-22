package it.fincons.osp.services;

import java.util.List;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.RichiestaIntegrazioneDTO;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.Protocollo;
import it.fincons.osp.model.RichiestaIntegrazione;
import it.fincons.osp.model.Utente;

public interface RichiestaIntegrazioneService {

	/**
	 * Inizializza e salva un nuovo oggetto {@link RichiestaIntegrazione},
	 * aggiornando lo stato della pratica e inserendo le comunicazioni mail
	 * 
	 * @param richiestaIntegrazioneRequest
	 * @return le informazioni sulla richiesta integrazione inserita
	 */
	public RichiestaIntegrazioneDTO insertRichiestaIntegrazione(RichiestaIntegrazioneDTO richiestaIntegrazioneRequest);

	/**
	 * Effettua il passaggio di stato della pratica in preavviso diniego, o in
	 * alternativa in pratica da rigettare
	 * 
	 * @param pratica
	 * @param utente
	 */
	public Protocollo gestionePreavvisoDiniego(Pratica pratica, Utente utente,
											   RichiestaIntegrazioneDTO richiestaIntegrazioneRequest);

	/**
	 * Inizializza e salva un nuovo oggetto {@link RichiestaIntegrazione}, setta il
	 * tipo richiesta specifico per la rettifica date, inserisce la comunicazione
	 * mail al cittadino
	 * 
	 * @param idPratica
	 * @param idUtenteRichiedente
	 * @param notaAlCittadino
	 * @return le informazioni sulla richiesta integrazione inserita
	 */
	public RichiestaIntegrazioneDTO insertRichiestaRettificaDate(Long idPratica, Long idUtenteRichiedente,
			String notaAlCittadino);

	/**
	 * Costruisce la mail di notifica preavviso diniego per il cittadino e gli
	 * attori coinvolti e inserisce la comunicazione email da inviare
	 * 
	 * @param destinatari
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 * @param codiceProtocolloOperazione
	 * @param note
	 */
	@LogEntryExit
	public void sendEmailNotificaPreavvisoDiniego(List<Utente> destinatari, Utente utente, Pratica pratica,
			String codiceProtocolloPratica, String codiceProtocolloOperazione, String note);

	/**
	 * Costruisce la mail di notifica pratica da rigettare e inserisce la
	 * comunicazione email da inviare all'istruttore
	 * 
	 * @param utente
	 * @param pratica
	 * @param codiceProtocolloPratica
	 */
	public void sendEmailNotificaPraticaDaRigettare(Utente utente, Pratica pratica, String codiceProtocolloPratica);

	public List<RichiestaIntegrazione> findAllByProtocollo(String protocollo);
}
