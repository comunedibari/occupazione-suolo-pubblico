package it.fincons.osp.services;

import java.util.List;

import it.fincons.osp.dto.AllegatoDTO;
import it.fincons.osp.dto.AllegatoSimplifiedDTO;
import it.fincons.osp.model.Allegato;
import it.fincons.osp.model.Gruppo;
import it.fincons.osp.model.Integrazione;
import it.fincons.osp.model.Parere;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.StatoPratica;
import it.fincons.osp.model.TipoAllegato;
import it.fincons.osp.model.TipoProcesso;
import it.fincons.osp.payload.request.*;

public interface AllegatoService {

	/**
	 * Inserisce un nuovo allegato della pratica inserendo le informazioni ricevute
	 * nella request
	 * 
	 * @param allegatoPraticaInsertRequest
	 * @return le informazioni sull'oggetto inserito
	 */
	public AllegatoDTO insertAllegatoPratica(AllegatoPraticaInsertRequest allegatoPraticaInsertRequest);

	/**
	 * Inserisce un nuovo allegato del parere inserendo le informazioni ricevute
	 * nella request
	 * 
	 * @param allegatoParereInsertRequest
	 * @param usernameUtente
	 * @return le informazioni sull'oggetto inserito
	 */
	public AllegatoDTO insertAllegatoParere(AllegatoParereInsertRequest allegatoParereInsertRequest, String usernameUtente);

	/**
	 * Inserisce un nuovo allegato dell'integrazione inserendo le informazioni
	 * ricevute nella request
	 * 
	 * @param allegatoIntegrazioneInsertRequest
	 * @return le informazioni sull'oggetto inserito
	 */
	public AllegatoDTO insertAllegatoIntegrazione(AllegatoIntegrazioneInsertRequest allegatoIntegrazioneInsertRequest);

	/**
	 * Inserisce un nuovo allegato per la richiesta di integrazione inserendo le informazioni
	 * ricevute nella request
	 * @param allegatoRichiestaIntegrazioneRequest
	 * @return
	 */
	AllegatoDTO insertAllegatoRichiestaIntegrazione(AllegatoRichiestaIntegrazioneRequest allegatoRichiestaIntegrazioneRequest);

	/**
	 * Ritorna la lista di allegati della pratica filtrati per Stato Pratica o per
	 * Tipo Processo
	 * 
	 * @param idPratica
	 * @param idStatoPratica
	 * @param idTipoProcesso
	 * @return la lista di allegati
	 */
	public List<AllegatoSimplifiedDTO> getAllegati(Long idPratica, Integer idStatoPratica, Integer idTipoProcesso);

	/**
	 * Ritorna la lista di allegati associati alla richiesta parere
	 * 
	 * @param idRichiestaParere
	 * @return la lista di allegati
	 */
	public List<AllegatoSimplifiedDTO> getAllegatiRichiestaParere(Long idRichiestaParere);

	/**
	 * Ritorna la lista di allegati associati al parere
	 * 
	 * @param idParere
	 * @return la lista di allegati
	 */
	public List<AllegatoSimplifiedDTO> getAllegatiParere(Long idParere);

	/**
	 * Ritorna la lista di allegati associati alla richiesta integrazione
	 * 
	 * @param idRichiestaIntegrazione
	 * @return la lista di allegati
	 */
	public List<AllegatoSimplifiedDTO> getAllegatiRichiestaIntegrazione(Long idRichiestaIntegrazione);

	/**
	 * Ricerca un allegato per id
	 * 
	 * @param id
	 * @return le informazioni dell'allegato
	 */
	public AllegatoDTO getAllegato(Long id);

	/**
	 * Elimina un allegato per id
	 * 
	 * @param id
	 */
	public void deleteAllegato(Long id);

	/**
	 * Controlla se per la pratica sono stati caricati tutti gli allegati
	 * obbligatori per l'inserimento della stessa pratica
	 * 
	 * @param pratica
	 * @param gruppo
	 * @return true se gli allegati sono presenti, false altrimenti
	 */
	public boolean checkAllegatiObbligatoriInserimento(Pratica pratica, Gruppo gruppo);

	/**
	 * Controlla se per la pratica sono stati caricati tutti gli allegati
	 * obbligatori per l'inserimento della stessa pratica
	 *
	 * @param pratica
	 * @param praticaInsertRequest
	 * @return true se gli allegati sono presenti, false altrimenti
	 */
	public boolean checkAllegatiObbligatoriAggiornamento(Pratica pratica, PraticaEgovEditRequest praticaInsertRequest);


	/**
	 * Controlla se per la pratica sono stati caricati tutti gli allegati
	 * obbligatori per lo stato attuale in cui si trova la pratica e per lo
	 * specifico gruppo
	 * 
	 * @param pratica
	 * @param statoPratica
	 * @param tipoProcesso
	 * @param gruppo
	 * @return true se gli allegati sono presenti, false altrimenti
	 */
	public boolean checkAllegatiObbligatori(Pratica pratica, StatoPratica statoPratica, TipoProcesso tipoProcesso, Gruppo gruppo);

	/**
	 * Effettua l'update degli allegati dell'integrazione, andando a inserire l'id
	 * dell'integrazione e togliendo l'id della richiesta integrazione
	 * 
	 * @param integrazione
	 */
	public void updateAllegatiIntegrazione(Integrazione integrazione);

	/**
	 * Effettua l'update degli allegati del parere, andando a inserire l'id del
	 * parere e togliendo l'id della richiesta parere, e inserendo il codice del
	 * protocollo
	 * 
	 * @param parere
	 */
	public void updateAllegatiParere(Parere parere);

	/**
	 * Ricerca un allegato di un parere di un determinato tipo
	 * 
	 * @param parere
	 * @param tipoAllegato
	 * @return l'allegato trovato
	 */
	public Allegato getAllegatoParereByTipo(Parere parere, TipoAllegato tipoAllegato);
	
	/**
	 * Ricerca un allegato di una pratica di un determinato tipo
	 * 
	 * @param pratica
	 * @param tipoAllegato
	 * @return l'allegato trovato
	 */
	public Allegato getAllegatoPraticaByTipo(Pratica pratica, TipoAllegato tipoAllegato);

	/**
	 * Inserisce un nuovo allegato dell'integrazione, ricercando la pratica a cui fa
	 * riferimento per id o codice protocollo ed inserendo le informazioni ricevute
	 * nella request
	 * 
	 * @param allegatoIntegrazioneEgovInsertRequest
	 * @return le informazioni sull'oggetto inserito
	 */
	public AllegatoDTO insertAllegatoIntegrazioneEgov(
			AllegatoIntegrazioneEgovInsertRequest allegatoIntegrazioneEgovInsertRequest);

	/**
	 * Inserisce il codice protocollo per tutti gli allegati della pratica che hanno
	 * come tipo allegato solo i tipi configurati per lo specifico stato passato in
	 * input
	 * 
	 * @param pratica
	 * @param statoPraticaAllegati
	 * @param codiceProtocollo
	 */
	public void insertCodiceProtocolloAllegatiPratica(Pratica pratica, StatoPratica statoPraticaAllegati,
			String codiceProtocollo);
	
	/**
	 * Inserisce il codice protocollo per gli allegati della pratica di un determinato tipo
	 * 
	 * @param pratica
	 * @param idTipoAllegato
	 * @param codiceProtocollo
	 */
	public void insertCodiceProtocolloAllegatiPraticaByIdTipo(Pratica pratica, Integer idTipoAllegato,
			String codiceProtocollo);
	
	/**
	 * Verifica se e' stato inserito l'allegato di determina rettifica
	 * 
	 * @param pratica
	 * @return true se l'allegato è presente, false altrimenti
	 */
	public boolean checkAllegatoDeterminaRettifica(Pratica pratica);
	
	/**
	 * Ritorna la lista di tutti gli allegati della pratica, compresi gli allegati dei pareri
	 * 
	 * @param idPratica
	 * @return la lista di allegati
	 */
	public List<AllegatoSimplifiedDTO> getAllegatiDocumentalePratica(Long idPratica);

}
