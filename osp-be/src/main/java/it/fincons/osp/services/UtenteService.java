package it.fincons.osp.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.fincons.osp.dto.UtenteDTO;
import it.fincons.osp.model.Utente;
import it.fincons.osp.payload.request.ModificaPasswordRequest;
import it.fincons.osp.payload.request.UtenteEditRequest;
import it.fincons.osp.payload.request.UtenteInsertRequest;

public interface UtenteService {

	/**
	 * Aggiorna la data dell'ultimo login dell'utente
	 * 
	 * @param username
	 * @return l'utente aggiornato
	 */
	public Utente updateLastLoginByUsername(String username);

	/**
	 * Genera una nuova password per l'utente
	 * 
	 * @param utente
	 * @return la password generata
	 */
	public String generateNewPassword(Utente utente);

	/**
	 * Ritorna la lista di utenti filtrati in base ai campi di ricerca impostati
	 * 
	 * @param id
	 * @param username
	 * @param nome
	 * @param cognome
	 * @param pageable
	 * @return la lista di utenti paginata
	 */
	public Page<UtenteDTO> getUtenti(Long id, String username, String nome, String cognome, Pageable pageable);

	/**
	 * Ritorna la lista di utenti filtrata per gruppo e municipio
	 * 
	 * @param idGruppo
	 * @param idMunicipio
	 * @return la lista di utenti filtrata
	 */
	public List<UtenteDTO> getUtentiGruppoMunicipio(Integer idGruppo, Integer idMunicipio);

	/**
	 * Ritorna la lista di utenti filtrata per gruppo
	 * 
	 * @param idGruppo
	 * @return la lista di utenti filtrata
	 */
	public List<UtenteDTO> getUtentiGruppo(Integer idGruppo);

	/**
	 * Ricerca un utente a partire dal suo id
	 * 
	 * @param id
	 * @return le informazioni sull'utente
	 */
	public UtenteDTO getUtente(Long id);

	/**
	 * Inserisce un nuovo utente inserendo le informazioni ricevute nella request
	 * 
	 * @param utenteRequest
	 * @return le informazioni sull'oggetto inserito
	 */
	public UtenteDTO insertUtente(UtenteInsertRequest utenteRequest);

	/**
	 * Modifica un utente inserendo le informazioni ricevute nella request
	 * 
	 * @param utenteRequest
	 */
	public void editUtente(UtenteEditRequest utenteRequest);

	/**
	 * Modifica la password dell'utente
	 * 
	 * @param modificaPasswordRequest
	 */
	public void editPassword(ModificaPasswordRequest modificaPasswordRequest);

	/**
	 * Elimina un utente se non è associato a nessuna entità
	 * 
	 * @param id
	 */
	public void deleteUtente(Long id);

	/**
	 * Disabilita l'utente
	 *
	 * @param id
	 */
	public void disableUtente(Long id);
}
