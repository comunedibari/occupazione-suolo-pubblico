package it.fincons.osp.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import it.fincons.osp.dto.*;
import it.fincons.osp.payload.request.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.fincons.osp.model.DatiRichiesta;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.TipoDetermina;

public interface PraticaService {

	/**
	 * Inizializza e salva un nuovo oggetto {@link Pratica}, inizializzando e
	 * creando anche il firmatario, l'eventuale destinatario e i dati della
	 * richiesta
	 * 
	 * @param praticaInsertRequest
	 * @return le informazioni della pratica inserita
	 */
	public PraticaDTO insertPratica(PraticaInsertEditRequest praticaInsertRequest);

	/**
	 * Modifica i dati della pratica e i dati del firmatario, destinatario e i dati
	 * della richiesta
	 * 
	 * @param praticaInsertRequest
	 */
	public void editPratica(PraticaInsertEditRequest praticaInsertRequest);

	/**
	 * Elimina una pratica
	 * 
	 * @param id
	 */
	public void deletePratica(Long id);

	/**
	 * Ritorna la lista di pratiche filtrata in base ai campi di ricerca impostati
	 * 
	 * @param username
	 * @param idsMunicipi
	 * @param idsStatiPratica
	 * @param filtriRicerca
	 * @param richiestaVerificaRipristinoLuoghi
	 * @param pageable
	 * @return la lista di pratiche paginata
	 */
	public Page<PraticaDTO> getPratiche(String username, List<Integer> idsMunicipi, List<Integer> idsStatiPratica,
			FiltriRicercaPraticaDTO filtriRicerca, Boolean richiestaVerificaRipristinoLuoghi, Pageable pageable);

	/**
	 * Ricerca una pratica a partire dal suo id
	 * 
	 * @param id
	 * @return le informazioni sulla pratica
	 */
	public PraticaDTO getPratica(Long id);

	/**
	 * Modifica le date e gli orari di occupazione relative ai {@link DatiRichiesta}
	 * di una Pratica
	 * 
	 * @param usernameUtente
	 * @param dateOccupazioneEditRequest
	 */
	public void editDateOccupazione(String usernameUtente, DateOccupazioneEditRequest dateOccupazioneEditRequest);


	/**
	 * Effettua il passaggio della pratica allo stato "Inserita", verificando la
	 * presenza degli allegati obbligatori, assegnando il numero di protocollo alla
	 * pratica ed inviando la mail di notifica agli attori coinvolti
	 * 
	 * @param idPratica
	 * @param idUtente
	 * @return le informazioni della pratica modificata
	 */
	public PraticaDTO switchToStatoInserita(Long idPratica, Long idUtente, String numeroProtocollo, String anno, LocalDateTime dataProtocollo);

	/**
	 * Effettua la presa in carico di una pratica, modificando lo stato in "Verifica
	 * formale", assegnando il numero di protocollo per il passaggio di stato ed
	 * inviando le mail di notifica agli attori coinvolti
	 * 
	 * @param idPratica
	 * @param idUtenteIstruttore
	 * @param idUtenteDirettore
	 * @return le informazioni della pratica modificata
	 */
	public PraticaDTO presaInCarico(Long idPratica, Long idUtenteIstruttore, Long idUtenteDirettore);

	/**
	 * Effettua il passaggio della pratica allo stato "Aprovata"
	 * 
	 * @param idPratica
	 * @param idUtente
	 * @return le informazioni della pratica modificata
	 */
	public PraticaDTO approvazione(Long idPratica, Long idUtente);

	/**
	 * Effettua l'operazione di rigetto di una richiesta, inserendo la richiesta di
	 * integrazione ed effettuando il passaggio di stato della pratica in preavviso
	 * diniego, o in alternativa in pratica da rigettare
	 * 
	 * @param richiestaIntegrazioneRequest
	 * @return le informazioni della pratica modificata
	 */
	public PraticaDTO rigettoRichiesta(RichiestaIntegrazioneDTO richiestaIntegrazioneRequest);

	/**
	 * Effettua l'inserimento del codice della determina, il passaggio di stato
	 * della pratica in "Attesa di pagamento" e l'invio delle mail di notifica agli
	 * attori coinvolti
	 * 
	 * @param idPratica
	 * @param idUtente
	 * @param codiceDetermina
	 * @param dataEmissioneDetermina
	 * @return le informazioni della pratica modificata
	 */
	public PraticaDTO inserimentoDetermina(Long idPratica, Long idUtente, String codiceDetermina,
			LocalDate dataEmissioneDetermina);

	/**
	 * Effettua il passaggio della pratica dallo stato "Attesa di pagamento" allo
	 * stato "Pronto al rilascio", controllando che siano stati caricati gli
	 * allegati relativi alle ricevute di pagamento
	 * 
	 * @param idPratica
	 * @param idUtente
	 * @return le informazioni della pratica modificata
	 */
	public PraticaDTO switchToStatoProntoAlRilascio(Long idPratica, Long idUtente);

	/**
	 * Effettua il passaggio della pratica dallo stato "Pronto al rilascio" allo
	 * stato "Concessione valida", assegnando il numero di protocollo ed inviando le
	 * mail di comunicazione
	 * 
	 * @param idPratica
	 * @param idUtente
	 * @return le informazioni della pratica modificata
	 */
	public PraticaDTO switchToStatoConcessioneValida(Long idPratica, Long idUtente);

	/**
	 * Effettua l'inserimento del codice della determina di rigetto, il passaggio di
	 * stato della pratica da "Pratca da rigettare" a "Pratica rigettata" e l'invio
	 * delle mail di notifica al cittadino e agli attori coinvolti
	 * 
	 * @param idPratica
	 * @param idUtente
	 * @param codiceDeterminaRigetto
	 * @param dataEmissioneDetermina
	 * @return le informazioni della pratica modificata
	 */
	public PraticaDTO inserimentoDeterminaRigetto(Long idPratica, Long idUtente, String codiceDeterminaRigetto,
			LocalDate dataEmissioneDetermina);

	/**
	 * Ricerca e ritorna la lista dello storico di una pratica
	 * 
	 * @param idPratica
	 * @param pageable
	 * @return la lista dello storico della pratica paginata
	 */
	public Page<PraticaDTO> getStoricoPratica(Long idPratica, Pageable pageable);

	/**
	 * Effettua il passaggio della pratica allo stato "Archiviata" ed effettua la
	 * cancellazione di tutte le notifiche relative alla pratica
	 * 
	 * @param idPratica
	 * @param idUtente
	 * @return le informazioni della pratica modificata
	 */
	public PraticaDTO archiviazione(Long idPratica, Long idUtente);

	/**
	 * Effettua le verifiche di apertura della proroga
	 * 
	 * @param idPratica
	 * @return void
	 */
	public void prorogaVerificheApertura(Long idPratica);

	/**
	 * Ritorna le informazioni della pratica di proroga da inserire, con le date di
	 * occupazione aggiornate
	 * 
	 * @param idPratica
	 * @return le informazioni della pratica di proroga da inserire
	 */
	public PraticaDTO getPraticaPrecompilataProroga(Long idPratica);

	/**
	 * Controlla se esistono già pratiche attive per le stesse coordinate
	 * dell'ubicazione nello stesso periodo temporale
	 * 
	 * @param verificaOccupazione
	 */
	public void checkSovrapposizioneUbicazione(VerificaOccupazioneDTO verificaOccupazione);

	/**
	 * Effettua l'operazione di verifica occupazione
	 * 
	 * @param verificaOccupazione
	 * @return le informazioni della pratica modificata
	 */
	public PraticaDTO verificaOccupazione(VerificaOccupazioneDTO verificaOccupazione);

	/**
	 * Inizializza e salva un nuovo oggetto {@link Pratica}, inizializzando e
	 * creando anche il firmatario, l'eventuale destinatario e i dati della
	 * richiesta. Inoltre salva anche gli allegati alla pratica ed effettua il
	 * passaggio di stato in "inserita"
	 * 
	 * @param praticaEgovInsertRequest
	 * @return le informazioni della pratica inserita
	 */
	public PraticaDTO insertPraticaCompletaEgov(PraticaEgovInsertRequest praticaEgovInsertRequest);

	/**
	 * Recupera l'utente di EGOV, lo setta nella request, inizializza e salva un
	 * nuovo oggetto {@link Pratica}, inizializzando e creando anche il firmatario,
	 * l'eventuale destinatario e i dati della richiesta
	 * 
	 * @param praticaInsertRequest
	 * @return le informazioni della pratica inserita
	 */
	public PraticaDTO insertPraticaEgov(PraticaInsertEditRequest praticaInsertRequest);

	/**
	 * Recupera l'utente di EGOV, effettua il passaggio della pratica allo stato
	 * "Inserita", verificando la presenza degli allegati obbligatori, assegnando il
	 * numero di protocollo alla pratica ed inviando la mail di notifica agli attori
	 * coinvolti
	 * 
	 * @param statoInsertaEgovRequest
	 * @return le informazioni della pratica modificata
	 */
	@Deprecated
	public PraticaDTO switchToStatoInseritaEgov(StatoInseritaEgovRequest statoInsertaEgovRequest);

	/**
	 * Modifica le date e gli orari di occupazione relative ai {@link DatiRichiesta}
	 * di una Pratica
	 *
	 * @param dateOccupazioneEgovEditRequest
	 * @return
	 */
	public PraticaDTO editDateOccupazioneEgov(DateOccupazioneEgovEditRequest dateOccupazioneEgovEditRequest);

	/**
	 * Ricerca tutte le pratiche con firmatario/destinatario avente il codice
	 * fiscale in input e nel caso sia presente anche il codice protocollo, ritorna
	 * solo la pratica specificata
	 * 
	 * @param codiceFiscale
	 * @param codiceProtocollo
	 * @return la lista di pratiche trovate
	 */
	public List<PraticaDTO> getPraticheEgov(String codiceFiscale, String codiceProtocollo, Long idPratica);

	/**
	 * Ricerca tutte le pratiche con firmatario/destinatario avente il codice
	 * fiscale o piva in input
	 *
	 * @param codiceFiscalePiva
	 * @return la lista di pratiche trovate
	 */
	public List<PraticaDTO> getPraticheEgovAttesaPagamentoBollo(String codiceFiscalePiva);

	/**
	 * Ricerca tutte le pratiche con firmatario/destinatario avente il codice
	 * fiscale o piva in input
	 *
	 * @param codiceFiscalePiva
	 * @return la lista di pratiche trovate
	 */
	public List<PraticaDTO> getPraticheEgovRettificaDate(String codiceFiscalePiva);

	/**
	 * Inizializza e salva un nuovo oggetto {@link Pratica}, per il processo di
	 * proroga, inizializzando i dati a partire dalla pratica originaria e inserendo
	 * solo i nuovi dati richiesti per il processo. Inoltre salva anche gli allegati
	 * alla pratica ed effettua il passaggio di stato in "inserita"
	 * 
	 * @param prorogaEgovInsertRequest
	 * @return le informazioni della pratica inserita
	 */
	public PraticaDTO insertProrogaCompletaEgov(ProrogaEgovInsertRequest prorogaEgovInsertRequest);

	/**
	 * Inizializza e salva un nuovo oggetto {@link Pratica}, per il processo di
	 * proroga, inizializzando i dati a partire dalla pratica originaria e inserendo
	 * solo i nuovi dati richiesti per il processo.
	 * 
	 * @param praticaProrogaEgovInsertEditRequest
	 * @return le informazioni della pratica inserita
	 */
	public PraticaDTO insertProrogaEgov(PraticaProrogaEgovInsertEditRequest praticaProrogaEgovInsertEditRequest);

	/**
	 * Ritorna le informazioni della pratica di rinuncia da inserire
	 * 
	 * @param idPratica
	 * @return le informazioni della pratica di rinuncia da inserire
	 */
	public PraticaDTO getPraticaPrecompilataRinuncia(Long idPratica);

	/**
	 * Inizializza e salva un nuovo oggetto {@link Pratica}, per il processo di
	 * rinuncia, inizializzando i dati a partire dalla pratica originaria e
	 * inserendo solo i nuovi dati richiesti per il processo. Inoltre salva anche
	 * gli allegati alla pratica ed effettua il passaggio di stato in "inserita"
	 * 
	 * @param rinunciaEgovInsertRequest
	 * @return le informazioni della pratica inserita
	 */
	public PraticaDTO insertRinunciaCompletaEgov(RinunciaEgovInsertRequest rinunciaEgovInsertRequest);

	/**
	 * Inizializza e salva un nuovo oggetto {@link Pratica}, per il processo di
	 * rinuncia, inizializzando i dati a partire dalla pratica originaria e
	 * inserendo solo i nuovi dati richiesti per il processo.
	 * 
	 * @param praticaRinunciaEgovInsertEditRequest
	 * @return le informazioni della pratica inserita
	 */
	public PraticaDTO insertRinunciaEgov(PraticaRinunciaEgovInsertEditRequest praticaRinunciaEgovInsertEditRequest);

	/**
	 * Effettua l'update dei dati della pratica, controlla che sia stato inserito
	 * l'allegato determina di rettifica, inserisce le informazioni della determina,
	 * richiede il protocollo ed invia le mail di notifica
	 * 
	 * @param praticaRettificaRequest
	 * @return le informazioni della pratica rettificata
	 */
	public PraticaDTO rettificaPratica(PraticaRettificaRequest praticaRettificaRequest);

	/**
	 * Effettua l'inserimento della nota al cittadino, il codice determina e la data
	 * di emissione della determina per i processi ad istanza d'ufficio, ovvero
	 * revoca, decadenza e annullamento. Inoltre effettua il contestuale passaggio
	 * di stato della pratica negli stati "Revocata", "Decaduta" e "Annullata",
	 * richiede il protocollo, manda le email al cittadino e agli attori coinvolti e
	 * manda una richiesta parere alla polizia locale per la verifica del ripristino
	 * dei luoghi.
	 * 
	 * @param idPratica
	 * @param idUtente
	 * @param notaAlCittadino
	 * @param codiceDetermina
	 * @param dataEmissioneDetermina
	 * @param tipoDetermina
	 * @return le informazioni della pratica modificata
	 */
	public PraticaDTO inserimentoDeterminaRda(Long idPratica, Long idUtente, String notaAlCittadino,
			String codiceDetermina, LocalDate dataEmissioneDetermina, TipoDetermina tipoDetermina);

	/**
	 *
	 * Restituisce la determina per la pratica avente stato 8/Attesa di pagamento
	 *
	 * @param idPratica
	 * @return
	 */
	DeterminaDTO getDeterminaInAttesaPagamento(Long idPratica);

	PraticaDTO editPraticaEgov(PraticaEgovEditRequest praticaEgovEditRequest);

	List<PraticaDTO> searchPraticheRettificabili(String codiceFiscalePartitaIva);

	/**
	 * Restituisce tutte le pratiche in cui lo stato pratica è concessione valida e
	 * la cui la data scadenza occupazione non sia minore di 15 giorni (proroga) oppure 60 giorni (rinuncia) dalla data della richiesta.
	 * Inoltre non deve essere stato avviato un altro processo post concessione (rinuncia o proroga)
	 *
	 * @param codiceFiscalePartitaIva
	 * @param idTipoProcesso
	 * @return
	 */
	List<PraticaDTO> searchPraticheAvviabiliPostConcessioone(String codiceFiscalePartitaIva, Integer idTipoProcesso);

	Long getCountPratiche(String username, List<Integer> idsStatiPratica, List<Integer> idsMunicipi);

	/**
	 *
	 * @param username
	 * @param flagEsenzioneCupEditRequest
	 */
	void editFlagEsenzioneCup(String username, FlagEsenzioneCupEditRequest flagEsenzioneCupEditRequest);

	Pratica switchToStatoTerminata(Long idPratica);
}
