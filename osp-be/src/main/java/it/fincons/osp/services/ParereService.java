package it.fincons.osp.services;

import it.fincons.osp.dto.ParereDTO;
import it.fincons.osp.payload.request.ParereInsertRequest;

public interface ParereService {

	/**
	 * Inizializza e salva un nuovo oggetto {@link parere} ed invia la mail con
	 * l'ordinanza se necessario
	 * 
	 * @param parereInsertRequest
	 * @return le informazioni del parere inserito
	 */
	public ParereDTO insertParere(ParereInsertRequest parereInsertRequest, boolean isRipristinoLuoghi);

	/**
	 * Ricerca un parere a partire dal suo id
	 * 
	 * @param id
	 * @return le informazioni sul parere
	 */
	public ParereDTO getParere(Long id);
}
