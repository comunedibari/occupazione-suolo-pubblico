package it.fincons.osp.services;

import it.fincons.osp.dto.DatiRichiestaDTO;
import it.fincons.osp.dto.DatiRichiestaRettificaDTO;
import it.fincons.osp.dto.GeoMultiPointDTO;
import it.fincons.osp.model.DatiRichiesta;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.TipoProcesso;
import it.fincons.osp.model.TipoOperazioneVerificaOccupazione;
import it.fincons.osp.payload.request.DateOccupazioneEditRequest;

import java.util.List;

public interface DatiRichiestaService {

	/**
	 * Inizializza e salva un nuovo oggetto {@link DatiRichiesta}
	 * 
	 * @param datiRichiestaRequest
	 * @param tipoProcesso
	 * @param praticaOriginaria
	 * @return l'oggetto {@link DatiRichiesta} creato
	 */
	public DatiRichiesta insertDatiRichiesta(DatiRichiestaDTO datiRichiestaRequest, TipoProcesso tipoProcesso,
			Pratica praticaOriginaria);

	/**
	 * Modifica l'oggetto {@link DatiRichiesta} con le nuove informazioni
	 * 
	 * @param datiRichiesta
	 * @param datiRichiestaRequest
	 * @param tipoProcesso
	 * @param praticaOriginaria
	 */
	public void editDatiRichiesta(DatiRichiesta datiRichiesta, DatiRichiestaDTO datiRichiestaRequest,
			TipoProcesso tipoProcesso, Pratica praticaOriginaria);

	/**
	 * Modifica le date di occupazione
	 * 
	 * @param pratica
	 * @param dateOccupazioneEditRequest
	 */
	public void editDateOccupazione(Pratica pratica, DateOccupazioneEditRequest dateOccupazioneEditRequest);

	/**
	 * Controlla l'esistenza di pratiche attive che per lo stesso periodo temporale
	 * hanno le stesse coordinate dell'ubicazione
	 * 
	 * @param datiRichiesta
	 * @param tipoOperazione
	 * @param coordUbicazioneDefinitiva
	 * @return true se ci sono già coordinate che si sovrappongono con quelle
	 *         passate in input, false altrimenti
	 */
	public List<String> existSovrapposizioneCoordUbicazione(DatiRichiesta datiRichiesta,
			TipoOperazioneVerificaOccupazione tipoOperazione, GeoMultiPointDTO coordUbicazioneDefinitiva);

	/**
	 * Effettua l'operazione di verifica dell'occupazione gestendo i diversi tipi di
	 * operazione consentiti e salvando le coordinate dell'ubicazione definitva
	 * 
	 * @param datiRichiesta
	 * @param tipoOperazione
	 * @param coordUbicazioneDefinitiva
	 */
	public void verificaOccupazione(DatiRichiesta datiRichiesta, TipoOperazioneVerificaOccupazione tipoOperazione,
			GeoMultiPointDTO coordUbicazioneDefinitiva);

	/**
	 * Effettua l'aggiornamento dei dati richiesta, solo per i campi di cui e'
	 * possibile effettuare la rettifica
	 * 
	 * @param datiRichiesta
	 * @param datiRichiestaRettifica
	 */
	public void updateDatiRichiestaRettifica(DatiRichiesta datiRichiesta,
			DatiRichiestaRettificaDTO datiRichiestaRettifica);
}
