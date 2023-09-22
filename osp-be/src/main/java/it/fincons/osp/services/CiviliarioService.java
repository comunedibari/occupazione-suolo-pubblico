package it.fincons.osp.services;

import it.fincons.osp.exceptions.CiviliarioServiceException;
import it.fincons.osp.payload.civiliario.response.DataSingoloMunicipioResponse;

public interface CiviliarioService {

	/**
	 * Effettua una chiamata ai servizi del Civiliario di Bari per ottenere le
	 * informazioni degli indirizzi corrispondenti
	 * 
	 * @param indirizzo   - Indirizzo da ricercare
	 * @param numero      - Numero della via
	 * @param idMunicipio - Id del municipio per il quale ricercare risultati
	 * @return {@link DataSingoloMunicipioResponse}
	 * @throws CiviliarioServiceException
	 */
	public DataSingoloMunicipioResponse getDataSingoloMunicipio(String indirizzo, String numero, String idMunicipio)
			throws CiviliarioServiceException;

}
