package it.fincons.osp.services;

import java.util.List;

import it.fincons.osp.dto.RichiestaParereDTO;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.RichiestaIntegrazione;
import it.fincons.osp.model.RichiestaParere;
import it.fincons.osp.model.Utente;
import it.fincons.osp.payload.request.UlterioriPareriInsertRequest;

public interface RichiestaParereService {

	/**
	 * Inizializza e salva un nuovo oggetto {@link RichiestaParere}, aggiornando lo
	 * stato della pratica e inserendo le comunicazioni mail
	 * 
	 * @param richiestaParereRequest
	 * @return le informazioni sulla richiesta parere inserita
	 */
	public RichiestaParereDTO insertRichiestaParereFromVerificaFormale(RichiestaParereDTO richiestaParereRequest);

	/**
	 * Inizializza e salva le nuove richieste pareri, inserendo inoltre le
	 * comunicazioni mail
	 * 
	 * @param ulterioriPareriRequest
	 * @param usernameUtente
	 * @return le informazioni sulle richieste pareri inserite
	 */
	public List<RichiestaParereDTO> insertUlterioriRichiestePareri(UlterioriPareriInsertRequest ulterioriPareriRequest,
			String usernameUtente);

	/**
	 * Inizializza e salva un nuovo oggetto {@link RichiestaParere}, rappresentante
	 * la richiesta di verifica del ripristino dei luoghi alla Polizia Locale e
	 * inserisce le comunicazioni mail
	 * 
	 * @param pratica
	 * @param utenteRichiedente
	 * @return le informazioni sulle richieste pareri inserite
	 */
	public RichiestaParereDTO insertRichiestaVerificaRipristinoLuoghi(Pratica pratica, Utente utenteRichiedente);

	public List<RichiestaParere> findAllByProtocollo(String protocollo);
}
