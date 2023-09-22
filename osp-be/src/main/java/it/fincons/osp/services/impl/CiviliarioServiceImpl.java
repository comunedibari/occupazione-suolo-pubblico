package it.fincons.osp.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.exceptions.CiviliarioServiceException;
import it.fincons.osp.payload.civiliario.request.DataSingoloMunicipioRequest;
import it.fincons.osp.payload.civiliario.request.LoginRequest;
import it.fincons.osp.payload.civiliario.response.CiviliarioResponse;
import it.fincons.osp.payload.civiliario.response.DataSingoloMunicipioResponse;
import it.fincons.osp.payload.civiliario.response.LoginResponse;
import it.fincons.osp.services.CiviliarioService;
import it.fincons.osp.utils.Coordinates;
import it.fincons.osp.utils.GeographicUnitsConverter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CiviliarioServiceImpl implements CiviliarioService {

	@Autowired
	RestTemplate restTemplateCiviliario;

	@Value("${osp.app.civiliario.api.baseurl}")
	private String apiBaseUrl;

	@Value("${osp.app.civiliario.api.user}")
	private String user;

	@Value("${osp.app.civiliario.api.pass}")
	private String password;

	@Value("${osp.app.civiliario.api.timeout}")
	private Integer tokenTimeoutSeconds;

	@Value("${osp.app.civiliario.maxResult}")
	private Integer maxResult;

	private String token = "";

	private LocalDateTime tokenTime = LocalDateTime.now();

	private HttpHeaders headers = new HttpHeaders();

	@Override
	@LogEntryExit(showArgs = true, showResult = true)
	public DataSingoloMunicipioResponse getDataSingoloMunicipio(String indirizzo, String numero, String idMunicipio)
			throws CiviliarioServiceException {
		this.checkToken();

		if (StringUtils.isBlank(indirizzo)) {
			throw new ValidationException("Errore: indirizzo vuoto");
		}

		indirizzo = indirizzo.replaceAll("/[^a-zA-Z' ]/g", "");
		StringBuilder filterBuilder = new StringBuilder();
		filterBuilder.append("nome_via|ILIKE|%").append(indirizzo).append("%");

		if (numero != null) {
			String[] split = numero.split("/");
			if(split.length > 1) {
				filterBuilder.append(";numero|EQ|").append(split[0]);
				filterBuilder.append(";esponente|ILIKE|%").append(split[1]);
			} else {
				filterBuilder.append(";numero|EQ|").append(split[0]);
			}
		}

		DataSingoloMunicipioRequest request = new DataSingoloMunicipioRequest(filterBuilder.toString());
		HttpEntity<DataSingoloMunicipioRequest> entity = new HttpEntity<>(request, headers);

		DataSingoloMunicipioResponse response = restTemplateCiviliario.postForObject(apiBaseUrl + "/civico/master",
				entity, DataSingoloMunicipioResponse.class);

		if (response != null && response.getResult() != null && !response.getResult().isEmpty()) {

			List<CiviliarioResponse> result = new ArrayList<>();

			List<CiviliarioResponse> listaIndirizzi;

			if(idMunicipio != null && !idMunicipio.isEmpty()) {
				// filtro indirizzi per municipio
				listaIndirizzi = response.getResult().stream()
						.filter(c -> c.getMunicipio().endsWith(idMunicipio)).collect(Collectors.toList());
			} else {
				listaIndirizzi = response.getResult();
			}

			this.searchAndBuildIndirizziFittizi(listaIndirizzi, result);

			// gestione size lista risultati finale
			if (result.size() > maxResult) {
				result = result.subList(0, maxResult);
			} else {
				if (listaIndirizzi.size() > maxResult - result.size()) {
					listaIndirizzi = listaIndirizzi.subList(0, maxResult - result.size());
				}

				result.addAll(listaIndirizzi);
			}

			// conversione coordinate geografiche
			for (CiviliarioResponse currentIndirizzo : result) {
				Coordinates coord = GeographicUnitsConverter.utmToWgs(Double.parseDouble(currentIndirizzo.getX()),
						Double.parseDouble(currentIndirizzo.getY()), 33, false);

				currentIndirizzo.setLat(coord.getLatitude());
				currentIndirizzo.setLon(coord.getLongitude());
			}

			response.setResult(result);
		}

		return response;
	}

	/**
	 * Controlla se il token e' ancora valido. In caso contrario effettua la login
	 * per ottenere un nuovo token.
	 * 
	 * @throws CiviliarioServiceException
	 */
	private void checkToken() throws CiviliarioServiceException {
		LocalDateTime tokenTimeout = tokenTime.plusSeconds(tokenTimeoutSeconds);

		if (LocalDateTime.now().isAfter(tokenTimeout) || StringUtils.isEmpty(token)) {
			// Require new token
			log.info("Timeout Login: generating new token in progress...");
			this.login();
		}
	}

	/**
	 * Effettua la login recuperando il token di autenticazione da utilizzare per le
	 * chiamate ai servizi del Civiliario di Bari
	 * 
	 * @throws CiviliarioServiceException
	 */
	private void login() throws CiviliarioServiceException {

		String signature = DigestUtils.md5DigestAsHex((user + password).getBytes());
		LoginRequest request = new LoginRequest(signature);
		LoginResponse response = restTemplateCiviliario.postForObject(apiBaseUrl + "/login/doLoginNew", request,
				LoginResponse.class);
		if (response == null) {
			throw new CiviliarioServiceException("Errore durante la login al servizio del Civiliario di Bari");
		}
		this.token = response.getResult();
		headers.clear();
		headers.add("it_app_auth", token);
		this.tokenTime = LocalDateTime.now();

	}

	/**
	 * Calcola gli indirizzi fittizi composti da solo la via senza il numero civico,
	 * andando a trovare il numero civico medio della via e utilizzando
	 * quell'indirizzo per le coordinate geografiche. Infine aggiunge l'indirizzo
	 * calcolato alla lista dei risultati
	 * 
	 * @param listaIndirizzi
	 * @param result
	 */
	private void searchAndBuildIndirizziFittizi(List<CiviliarioResponse> listaIndirizzi,
			List<CiviliarioResponse> result) {

		// Comparatore custom per comparare il numero civico ed anche l'esponente
		Comparator<CiviliarioResponse> compareByCivico = Comparator
				.comparing(CiviliarioResponse::getNumeroCivico, Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(CiviliarioResponse::getEsponente, Comparator.nullsLast(Comparator.naturalOrder()));

		// setto il campo di appoggio del numero civico convertendolo in intero, in
		// quanto serve per calcolarsi il numero civico medio nella via per la
		// costruzione dell'indirizzo fittizio senza numero civico
		for (CiviliarioResponse currentCiviliarioResponse : listaIndirizzi) {
			try {
				currentCiviliarioResponse.setNumeroCivico(Integer.parseInt(currentCiviliarioResponse.getNumero()));
			} catch (NumberFormatException e) {
				log.warn("Numero civico non convertibile in intero [" + currentCiviliarioResponse.getNumero() + "]");
			}
		}

		// ordino tutti gli indirizzi
		listaIndirizzi.sort(compareByCivico);

		// raggruppo indirizzi per nome via e localita'
		Map<Pair<String, String>, List<CiviliarioResponse>> indirizziPerNomeViaAndLocalita = listaIndirizzi.stream()
				.collect(Collectors.groupingBy(c -> new ImmutablePair<>(c.getNome_via(), c.getLocalita())));

		// individuazione indirizzo medio della via da usare come indirizzo fittizio
		// senza numero civico
		for (Map.Entry<Pair<String, String>, List<CiviliarioResponse>> entry : indirizziPerNomeViaAndLocalita
				.entrySet()) {

			List<CiviliarioResponse> currentListaIndirizzi = entry.getValue();

			// ordino per numero civico
			currentListaIndirizzi.sort(compareByCivico);

			// recupero indirizzo con numero civico medio
			CiviliarioResponse indirizzoMedianoTrovato = currentListaIndirizzi.size() == 1
					? currentListaIndirizzi.get(0)
					: currentListaIndirizzi
							.get((currentListaIndirizzi.size() / 2) + (currentListaIndirizzi.size() % 2));

			CiviliarioResponse indirizzoMediano = new CiviliarioResponse(indirizzoMedianoTrovato);

			indirizzoMediano.setNumero(null);
			indirizzoMediano.setEsponente(null);
			indirizzoMediano.setNumeroCivico(null);

			result.add(indirizzoMediano);
		}
	}
}
