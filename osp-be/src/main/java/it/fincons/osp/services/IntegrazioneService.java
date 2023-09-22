package it.fincons.osp.services;

import it.fincons.osp.dto.IntegrazioneDTO;
import it.fincons.osp.payload.request.IntegrazioneCompletaEgovInsertRequest;
import it.fincons.osp.payload.request.IntegrazioneEgovInsertRequest;
import it.fincons.osp.payload.request.RettificaDateEgovInsertRequest;

public interface IntegrazioneService {

	/**
	 * Inizializza e salva un nuovo oggetto {@link integrazione}, effettua il
	 * passaggio della pratica dallo stato "Necessaria integrazione"/"Preavviso
	 * diniego" allo stato "Verifica formale", inviando la mail di comunicazione
	 * all'istruttore di municipio
	 * 
	 * @param integrazioneRequest
	 * @return le informazioni dell'integrazione inserita
	 */
	public IntegrazioneDTO insertIntegrazione(IntegrazioneDTO integrazioneRequest);

	/**
	 * Ricerca una integrazione a partire dal suo id
	 * 
	 * @param id
	 * @return le informazioni sull'integrazione
	 */
	public IntegrazioneDTO getIntegrazione(Long id);

	/**
	 * Inserisce gli allegati dell'integrazione ed inizializza e salva un nuovo
	 * oggetto {@link integrazione}, effettua il passaggio della pratica dallo stato
	 * "Necessaria integrazione"/"Preavviso diniego" allo stato "Verifica formale",
	 * inviando la mail di comunicazione all'istruttore di municipio
	 * 
	 * @param integrazioneEgovInsertRequest
	 * @return le informazioni dell'integrazione inserita
	 */
	public IntegrazioneDTO insertIntegrazioneCompletaEgov(
			IntegrazioneCompletaEgovInsertRequest integrazioneCompletaEgovInsertRequest);

	/**
	 * Inizializza e salva un nuovo oggetto {@link integrazione} per l'ultima
	 * richiesta effettuata, effettua il passaggio della pratica dallo stato
	 * "Necessaria integrazione"/"Preavviso diniego" allo stato "Verifica formale",
	 * inviando la mail di comunicazione all'istruttore di municipio
	 * 
	 * @param integrazioneEgovInsertRequest
	 * @return le informazioni dell'integrazione inserita
	 */
	public IntegrazioneDTO insertIntegrazioneEgov(IntegrazioneEgovInsertRequest integrazioneEgovInsertRequest);

	/**
	 * Inizializza e salva un nuovo oggetto {@link integrazione}, effettua il
	 * passaggio della pratica dallo stato "Rettifica date" allo stato "Verifica
	 * formale", inviando la mail di comunicazione all'istruttore di municipio e
	 * agli attori coinvolti in caso ci fosse una richiesta pareri attiva
	 * 
	 * @param integrazioneRequest
	 * @return le informazioni dell'integrazione inserita
	 */
	public IntegrazioneDTO insertRettificaDate(IntegrazioneDTO integrazioneRequest);

	/**
	 * Inizializza e salva un nuovo oggetto {@link integrazione} per l'ultima
	 * richiesta effettuata, effettua il passaggio della pratica dallo stato
	 * "Rettifica date" allo stato "Verifica formale", inviando la mail di
	 * comunicazione all'istruttore di municipio e agli attori coinvolti in caso ci
	 * fosse una richiesta pareri attiva
	 * 
	 * @param rettificaDateEgovInsertRequest
	 * @return le informazioni dell'integrazione inserita
	 */
	public IntegrazioneDTO insertRettificaDateEgov(RettificaDateEgovInsertRequest rettificaDateEgovInsertRequest);
}
