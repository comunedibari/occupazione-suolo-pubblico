package it.fincons.osp.services;

import java.time.LocalDate;
import java.util.List;

import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.Protocollo;
import it.fincons.osp.model.StatoPratica;
import it.fincons.osp.model.TipoOperazioneProtocollo;
import it.fincons.osp.payload.protocollazione.response.ProtocolloResponse;

public interface ProtocolloService {

	/**
	 * Inserisce un oggetto {@link Protocollo}
	 * 
	 * @param pratica
	 * @param statoPratica
	 * @param protocolloResponse
	 * @param tipoOperazione
	 * @return il protocollo inserito
	 */
	public Protocollo insertProtocollo(Pratica pratica, StatoPratica statoPratica,
			ProtocolloResponse protocolloResponse, TipoOperazioneProtocollo tipoOperazione);

	/**
	 * Cerca e restituisce un pretocollo per pratica e per stato pratica
	 * 
	 * @param pratica
	 * @param statoPratica
	 * @return il protocollo trovato
	 */
	public Protocollo getProtocollo(Pratica pratica, StatoPratica statoPratica);

	/**
	 * Cerca e restituisce il pratocollo dell'inserimento della pratica
	 * 
	 * @param pratica
	 * @return il protocollo dell'inserimento della pratica
	 */
	public Protocollo getProtocolloInserimento(Pratica pratica);

	/**
	 * Cerca e restituisce un pretocollo per il suo codice
	 * 
	 * @param codiceProtocollo
	 * @return il protocollo trovato
	 */
	public Protocollo getProtocolloByCodice(String codiceProtocollo);

	/**
	 * Cerca e restituisce un protocollo per il suo numero protocollo
	 *
	 * @param numeroProtocollo
	 * @return il protocollo trovato
	 */
	public Protocollo getProtocolloByNumeroProtcollo(String numeroProtocollo);

	/**
	 * Cerca e restituisce una lista di protocolli filtrati numero protocollo
	 *
	 * @param numeroProtocollo
	 * @return
	 */
	public List<Protocollo> getProtocolliByNumeroProtocollo(String numeroProtocollo);

	/**
	 * Inserisce le informazioni della determina di rettifica nel protocollo
	 * 
	 * @param protocollo
	 * @param codiceDeterminaRettifica
	 * @param dataEmissioneDeterminaRettifica
	 */
	public void insertDeterminaRettifica(Protocollo protocollo, String codiceDeterminaRettifica,
			LocalDate dataEmissioneDeterminaRettifica);
}
