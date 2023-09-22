package it.fincons.osp.services;

import java.util.List;

import it.fincons.osp.dto.RichiedenteDTO;
import it.fincons.osp.dto.RichiedenteRettificaDTO;
import it.fincons.osp.model.Richiedente;

public interface RichiedenteService {

	/**
	 * Inizializza e salva un nuovo oggetto {@link Richiedente}
	 * 
	 * @param richiedenteRequest
	 * @return l'oggetto {@link Richiedente} creato
	 */
	public Richiedente insertRichiedente(RichiedenteDTO richiedenteRequest);

	/**
	 * Modifica l'oggetto {@link Richiedente} con le nuove informazioni
	 * 
	 * @param richiedente
	 * @param richiedenteRequest
	 */
	public void editRichiedente(Richiedente richiedente, RichiedenteDTO richiedenteRequest);

	/**
	 * Ricerca i destinatari secondo i filtri di ricerca passati in input
	 * 
	 * @param nome
	 * @param cognome
	 * @param denominazioneRagSoc
	 * @param codFiscalePIva
	 * @return List<Richiedente>
	 */
	public List<Richiedente> searchDestinatari(String nome, String cognome, String denominazioneRagSoc,
			String codFiscalePIva);

	/**
	 * Ricerca i richiedenti per codice fiscale o p.iva
	 * 
	 * @param codFiscalePIva
	 * @return List<Richiedente>
	 */
	public List<Richiedente> searchRichiedentiByCodiceFiscale(String codFiscalePIva);

	/**
	 * Effettua l'aggiornamento del richiedente (firmatario o destinatario), solo
	 * per i campi di cui e' possibile effettuare la rettifica
	 * 
	 * @param datiRichiesta
	 * @param datiRichiestaRettifica
	 */
	public void updateRichiedenteRettifica(Richiedente richiedente, RichiedenteRettificaDTO richiedenteRettifica);
}
