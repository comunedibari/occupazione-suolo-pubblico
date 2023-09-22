package it.fincons.osp.services;

import java.util.List;

import it.fincons.osp.dto.NotificaScadenzarioDTO;

public interface NotificaScadenzarioService {

	/**
	 * Verifica quali sono le pratiche per le quali è necessario inviare notifiche o
	 * eseguire azioni, ed inserisce le relative notifiche o compie le relative
	 * azioni
	 */
	public void inserimentoNotifiche();

	/**
	 * Ritorna la lista delle notifiche associate all'utente
	 * 
	 * @param idUtente
	 * @return la lista delle notifiche
	 */
	public List<NotificaScadenzarioDTO> getNotificheScadenzario(Long idUtente);
	
	/**
	 * Ritorna il numero delle notifiche associate all'utente
	 * 
	 * @param idUtente
	 * @return il numero di notifiche
	 */
	public long countNotificheScadenzario(Long idUtente);
}
