package it.fincons.osp.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.fincons.osp.dto.AutomiProtocolloDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.PraticaMapper;
import it.fincons.osp.model.*;
import it.fincons.osp.payload.protocollazione.request.*;
import it.fincons.osp.payload.protocollazione.response.ProtocolloResponse;
import it.fincons.osp.services.AutomiProtocolloService;
import it.fincons.osp.services.ProtocollazioneService;
import it.fincons.osp.services.UtilsService;
import it.fincons.osp.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.docx4j.Docx4J;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "osp.app.protocollo.api", name = "mocked", havingValue = "false")
public class ProtocollazioneServiceImpl implements ProtocollazioneService {

	@Value("${osp.app.protocollo.api.baseurl}")
	private String apiBaseUrl;

	@Value("${osp.app.protocollo.api.codiceAmministrazione}")
	private String codiceAmministrazione;

	@Value("${osp.app.format.date}")
	private String dateFormatterPattern;

	@Autowired
	private RestTemplate restTemplateProtocollazione;

	@Autowired
	private PraticaMapper praticaMapper;

	@Autowired
	private UtilsService utilsService;

	@Autowired
	private AutomiProtocolloService automiProtocolloService;

	@Override
	public ProtocolloResponse getNumeroProtocolloEntrata(Pratica pratica, Utente utente, String evento, String templateName, List<Allegato> allegati, PassaggioStato passaggioStato) {
		String[] documentiAllegati=null;

		if(allegati!=null && allegati.size()>0){
			documentiAllegati=new String[allegati.size()];

			for(int i=0;i<allegati.size();i++){
				documentiAllegati[i]= allegati.get(i).getNomeFile()==null ? "file_"+i : allegati.get(i).getNomeFile();
			}

		}

		// costruisco request
		ProtocolloEntrataRequest request = this.buildProtocolloEntrataRequest(pratica, utente, evento, templateName, allegati, passaggioStato);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		// log del JSON della request
		try {
			log.debug("Request protocollo entrata: " + objectMapper.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Errore nella conversione della request in json");
		}

		HttpHeaders headers = new HttpHeaders();

		// ContentType
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();

		// Custom ContentType.
		multipartBodyBuilder.part("protocolloEntrataRequest", request, MediaType.APPLICATION_JSON);

		if(allegati!=null && allegati.size()>0){
			allegati.forEach(allegato->{

				byte[] fileContents=allegato.getFileAllegato();

				fileContents=Base64.getEncoder().encode(fileContents);

				ByteArrayResource contentsAsResource = new ByteArrayResource(fileContents) {
					@Override
					public String getFilename() {
						return allegato.getNomeFile();
					}
				};

				multipartBodyBuilder.part("allegati", contentsAsResource);

			});
		}

		// multipart/form-data request body
		MultiValueMap<String, HttpEntity<?>> multipartBody = multipartBodyBuilder.build();

		// The complete http request body.
		HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity = new HttpEntity<>(multipartBody, headers);

		String urlProcolloEntrata = apiBaseUrl + "/middleware/v1/protocollo/richiesta/entrata/osp";

		log.info("Url servizio protocollo entrata: " + urlProcolloEntrata);

		ProtocolloResponse response = null;

		try {
			// invoco servizio protocollazione in entrata
			ResponseEntity<ProtocolloResponse> responseEntity = restTemplateProtocollazione.postForEntity(urlProcolloEntrata, httpEntity, ProtocolloResponse.class);

			response=responseEntity.getBody();

		} catch (Exception e) {
			log.error("Errore durante la chiamata del serivizio di protocollazione in entrata", e);
			throw new BusinessException(ErrorCode.E14, e.getMessage()!=null?e.getMessage():"Errore: non è stato possibile effettuare la protocollazione");
		}

		if (response == null || response.getNumeroProtocollo() == null ||response.getAnno() == null||response.getDataProtocollo() == null) {
			throw new BusinessException(ErrorCode.E14, "Errore: non è stato possibile effettuare la protocollazione");
		}

		// log del JSON della response
		try {
			log.debug("Response protocollo entrata: " + objectMapper.writeValueAsString(response));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Errore nella conversione della response in json");
		}

		return response;
	}

	@Override
	public ProtocolloResponse getNumeroProtocolloUscita(
			Pratica pratica,
			Utente utente,
			String evento,
			List<Utente> destinatari,
			boolean comunicazioneCittadino,
			String templateName,
			List<Allegato> allegati,
			PassaggioStato passaggioStato,
			String infoPassaggioStato
	) {

		// costruisco request
		ProtocolloUscitaRequest request = this.buildProtocolloUscitaRequest(pratica, utente, evento, destinatari, comunicazioneCittadino, templateName, allegati, passaggioStato, infoPassaggioStato);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		// log del JSON della request
		try {
			log.debug("Request protocollo uscita: " + objectMapper.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Errore nella conversione della request in json");
		}

		HttpHeaders headers = new HttpHeaders();

		// ContentType
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();

		// Custom ContentType.
		multipartBodyBuilder.part("protocolloUscitaRequest", request, MediaType.APPLICATION_JSON);

		if(allegati!=null && allegati.size()>0){
			allegati.forEach(allegato->{

				ByteArrayResource contentsAsResource = new ByteArrayResource(Base64.getEncoder().encode(allegato.getFileAllegato())) {
					@Override
					public String getFilename() {
						return allegato.getNomeFile();
					}
				};

				multipartBodyBuilder.part("allegati", contentsAsResource);

			});
		}

		// multipart/form-data request body
		MultiValueMap<String, HttpEntity<?>> multipartBody = multipartBodyBuilder.build();

		// The complete http request body.
		HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity = new HttpEntity<>(multipartBody, headers);

		String urlProcollo = apiBaseUrl + "/middleware/v1/protocollo/richiesta/uscita/osp";

		ProtocolloResponse response = null;

		try {
			// invoco servizio protocollazione in uscita
			ResponseEntity<ProtocolloResponse> responseEntity = restTemplateProtocollazione.postForEntity(urlProcollo, httpEntity, ProtocolloResponse.class);

			response=responseEntity.getBody();

		} catch (Exception e) {
			log.error("Errore durante la chiamata del serivizio di protocollazione in uscita", e);
			throw new BusinessException(ErrorCode.E14, e.getMessage()!=null?e.getMessage():"Errore: non è stato possibile effettuare la protocollazione");
		}

		if (response == null || response.getNumeroProtocollo() == null ||response.getAnno() == null||response.getDataProtocollo() == null) {
			throw new BusinessException(ErrorCode.E14, "Errore: non è stato possibile effettuare la protocollazione");
		}

		// log del JSON della response
		try {
			log.debug("Response protocollo uscita: " + objectMapper.writeValueAsString(response));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Errore nella conversione della response in json");
		}

		return response;
	}

	@Override
	public ProtocolloResponse getNumeroProtocolloUscitaV2(Pratica pratica, Utente utente, String evento, List<Utente> destinatari, boolean comunicazioneCittadino, byte[] docPrincipaleBase64, String docPrincipaleExtension, List<Allegato> allegati) {
		// costruisco request
		ProtocolloUscitaRequest request = this.buildProtocolloUscitaRequest(pratica, utente, evento, destinatari, comunicazioneCittadino, docPrincipaleBase64, docPrincipaleExtension);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		// log del JSON della request
		try {
			log.debug("Request protocollo uscita: " + objectMapper.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Errore nella conversione della request in json");
		}

		HttpHeaders headers = new HttpHeaders();

		// ContentType
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();

		// Custom ContentType.
		multipartBodyBuilder.part("protocolloUscitaRequest", request, MediaType.APPLICATION_JSON);

		if(allegati!=null && allegati.size()>0){
			allegati.forEach(allegato->{

				ByteArrayResource contentsAsResource = new ByteArrayResource(Base64.getEncoder().encode(allegato.getFileAllegato())) {
					@Override
					public String getFilename() {
						return allegato.getNomeFile();
					}
				};

				multipartBodyBuilder.part("allegati", contentsAsResource);

			});
		}

		// multipart/form-data request body
		MultiValueMap<String, HttpEntity<?>> multipartBody = multipartBodyBuilder.build();

		// The complete http request body.
		HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity = new HttpEntity<>(multipartBody, headers);

		String urlProcollo = apiBaseUrl + "/middleware/v1/protocollo/richiesta/uscita/osp";

		ProtocolloResponse response = null;

		try {
			// invoco servizio protocollazione in uscita
			ResponseEntity<ProtocolloResponse> responseEntity = restTemplateProtocollazione.postForEntity(urlProcollo, httpEntity, ProtocolloResponse.class);

			response=responseEntity.getBody();

		} catch (Exception e) {
			log.error("Errore durante la chiamata del serivizio di protocollazione in uscita", e);
			throw new BusinessException(ErrorCode.E14, "Errore: non è stato possibile effettuare la protocollazione");
		}

		if (response == null || response.getNumeroProtocollo() == null ||response.getAnno() == null||response.getDataProtocollo() == null) {
			throw new BusinessException(ErrorCode.E14, "Errore: non è stato possibile effettuare la protocollazione");
		}

		// log del JSON della response
		try {
			log.debug("Response protocollo uscita: " + objectMapper.writeValueAsString(response));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Errore nella conversione della response in json");
		}

		return response;
	}

	@Deprecated
	@Override
	public ProtocolloResponse getNumeroProtocolloUscita(Pratica pratica, Utente utente, String evento, List<Utente> destinatari, boolean comunicazioneCittadino) {

		// costruisco request
		ProtocolloUscitaRequest request = this.buildProtocolloUscitaRequest(pratica, utente, evento, destinatari, comunicazioneCittadino);
		HttpEntity<ProtocolloUscitaRequest> entity = new HttpEntity<>(request);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		// log del JSON della request
		try {
			log.info("Request protocollo uscita: " + objectMapper.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Errore nella conversione della request in json");
		}

		ProtocolloResponse response = null;

		String urlProcolloUscita = apiBaseUrl + "/middleware/v1/protocollo/richiesta/uscita";
		log.info("Url servizio protocollo uscita: " + urlProcolloUscita);

		try {
			// invoco servizio protocollazione in entrata
			response = restTemplateProtocollazione.postForObject(urlProcolloUscita, entity, ProtocolloResponse.class);
		} catch (Exception e) {
			log.error("Errore durante la chiamata del serivizio di protocollazione in uscita", e);
			throw new BusinessException(ErrorCode.E14, "Errore: non è stato possibile effettuare la protocollazione");
		}

		// DEBUG
//		response = new ProtocolloResponse();
//		response.setNumeroProtocollo("127");
//		response.setAnno("2022");
//		response.setDataProtocollo(LocalDateTime.now());

		if (response == null || response.getNumeroProtocollo() == null ||response.getAnno() == null||response.getDataProtocollo() == null) {
			throw new BusinessException(ErrorCode.E14, "Errore: non è stato possibile effettuare la protocollazione");
		}

		// log del JSON della response
		try {
			log.info("Response protocollo uscita: " + objectMapper.writeValueAsString(response));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Errore nella conversione della response in json");
		}

		return response;
	}

	/**
	 * Costruisce la request per la protocollazione della pratica in entrata
	 *
	 * @param pratica
	 * @param utente
	 * @return la request per il protocollo in entrata
	 */
	private ProtocolloEntrataRequest buildProtocolloEntrataRequest(Pratica pratica, Utente utente, String evento) {
		ProtocolloEntrataRequest protocolloEntrata = new ProtocolloEntrataRequest();

		ProtocolloRequest protocolloRequest=new ProtocolloRequest();
		protocolloRequest.setMittente(this.buildMittenteEntrataRequest(pratica.getFirmatario()));
		protocolloRequest.setDocumento(this.buildDocumentoRequest(pratica));
		protocolloRequest.setAreaOrganizzativaOmogenea(codiceAmministrazione);
		protocolloRequest.setAmministrazione(codiceAmministrazione);
		protocolloRequest.setOggetto(pratica.getTipoProcesso().getDescrizione() + " "
				+ pratica.getFirmatario().getCodiceFiscalePartitaIva() + " - Stato "
				+ pratica.getStatoPratica().getDescrizione() + " - Mittente " + utente.getRagioneSociale());
		protocolloRequest.setIdUtente(utente.getUoId());

		protocolloEntrata.setProtocolloRequest(protocolloRequest);

		return protocolloEntrata;
	}

	/**
	 * Costruisce la request per la protocollazione della pratica in entrata
	 *
	 * @param pratica
	 * @param utente
	 * @param evento
	 * @param templateName
	 * @return
	 */
	private ProtocolloEntrataRequest buildProtocolloEntrataRequest(Pratica pratica, Utente utente, String evento, String templateName, List<Allegato> allegati, PassaggioStato passaggioStato) {

		ProtocolloRequest protocolloRequest=new ProtocolloRequest();
		protocolloRequest.setMittente(this.buildMittenteEntrataRequest(pratica.getFirmatario()));
		protocolloRequest.setDocumento(this.buildDocumentoRequest(pratica,templateName,allegati,passaggioStato,null));
		protocolloRequest.setAreaOrganizzativaOmogenea(codiceAmministrazione);
		protocolloRequest.setAmministrazione(codiceAmministrazione);
		//passati in multipart
		//protocolloRequest.setAllegati(buildAllegati(allegati));
		protocolloRequest.setOggetto(buildOggetto(pratica));

		protocolloRequest.setIdUtente(utente.getUoId());

		ProtocolloEntrataRequest protocolloEntrata = new ProtocolloEntrataRequest();
		protocolloEntrata.setProtocolloRequest(protocolloRequest);

		return protocolloEntrata;
	}

	/**
	 * Costruisce un oggetto {@link DocumentoDTO} a partire dalle informazioni della
	 * pratica, convertendola in JSON
	 *
	 * @param pratica
	 * @return le informazioni del documento
	 */
	private DocumentoDTO buildDocumentoRequest(Pratica pratica) {
		String praticaJsonString;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		try {
			praticaJsonString = objectMapper.writeValueAsString(praticaMapper.entityToDto(pratica));
			log.info("JSON pratica: " + praticaJsonString);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Errore nella conversione della pratica in json");
		}

		DocumentoDTO documento = new DocumentoDTO();
		documento.setNomeFile(
				CaseUtils.toCamelCase("Pratica " + pratica.getStatoPratica().getDescrizione() + ".json", true, ' '));
		documento.setContenuto(
				new String(Base64.getEncoder().encode(praticaJsonString.getBytes(StandardCharsets.UTF_8))).getBytes());
		return documento;
	}


	private String buildOggetto(Pratica pratica){
		StringBuilder oggetto=new StringBuilder();

		if(pratica.getDestinatario()!=null){
			String denominazione=null;

			if(pratica.getDestinatario().getCognome()!=null&&pratica.getDestinatario().getNome()!=null){
				denominazione=pratica.getDestinatario().getCognome()+" "+pratica.getDestinatario().getNome();
			}else{
				denominazione=pratica.getDestinatario().getDenominazione();
			}

			oggetto.append(String.format("Occupazione Suolo Pubblico - %s - %s - %s", pratica.getTipoProcesso().getDescrizione(), pratica.getDatiRichiesta().getUbicazioneOccupazione(), denominazione));
		}else{
			oggetto.append(String.format("Occupazione Suolo Pubblico - %s - %s - %s %s", pratica.getTipoProcesso().getDescrizione(),pratica.getDatiRichiesta().getUbicazioneOccupazione(),pratica.getFirmatario().getNome(), pratica.getFirmatario().getCognome()));
		}

		return oggetto.toString();
	}

	/*TODO remove
	private String buildOggetto(Pratica pratica){
		String oggetto=null;
		//TODO verificare indirizzo
		if(pratica.getFirmatario().getDenominazione() !=null){
			oggetto=String.format("Occupazione Suolo Pubblico - %s - %s - %s", pratica.getTipoProcesso(), pratica.getDatiRichiesta().getNomeVia(), pratica.getFirmatario().getDenominazione());
		}else{
			oggetto=String.format("Occupazione Suolo Pubblico - %s - %s - %s %s", pratica.getTipoProcesso(), pratica.getDatiRichiesta().getNomeVia(),pratica.getFirmatario().getNome(), pratica.getFirmatario().getCognome());
		}
		return oggetto;
	}
*/
	private String buildNomeFile(Pratica pratica, String extension) {
		StringBuilder nomeFile=new StringBuilder();

		nomeFile.append(pratica.getTipoProcesso().getDescrizione());
		nomeFile.append(" ");

		if(pratica.getDestinatario() != null){
			if (pratica.getDestinatario().getDenominazione() != null) {
				nomeFile.append(pratica.getDestinatario().getDenominazione());
				nomeFile.append(" ");
			} else {
				nomeFile.append(pratica.getDestinatario().getNome()+ " " + pratica.getDestinatario().getCognome());
				nomeFile.append(" ");
			}
		} else {
			nomeFile.append(pratica.getFirmatario().getNome()+ " " + pratica.getFirmatario().getCognome());
			nomeFile.append(" ");
		}

		nomeFile.append(System.currentTimeMillis() + (extension.startsWith(".") ? extension : "." + extension));

		return nomeFile.toString();
	}

	/**
	 * Costruisce un oggetto {@link DocumentoDTO} a partire dalle informazioni della
	 * pratica e del template, convertendola in PDF
	 * @param pratica
	 * @param templateName
	 * @param allegati
	 * @param passaggioStato
	 * @return
	 */
	private DocumentoDTO buildDocumentoRequest(
			Pratica pratica,
			String templateName,
			List<Allegato> allegati,
			PassaggioStato passaggioStato,
			String infoPassaggioStato
	) {
		DocumentoDTO documento = new DocumentoDTO();
		documento.setNomeFile(buildNomeFile(pratica, ".pdf"));
		documento.setContenuto(new String(Base64.getEncoder().encode(buildPdfFromTemplate(pratica, templateName, allegati, passaggioStato, infoPassaggioStato))).getBytes());
		return documento;
	}

	/**
	 * Costruisce un oggetto {@link DocumentoDTO} a partire dalle informazioni della
	 * pratica, convertendola in PDF
	 * @param pratica
	 * @param docPrincipaleBase64
	 * @return
	 */
	private DocumentoDTO buildDocumentoRequest(Pratica pratica, byte[] docPrincipaleBase64, String extensionDocPrincipale) {
		DocumentoDTO documento = new DocumentoDTO();
		documento.setNomeFile(buildNomeFile(pratica, extensionDocPrincipale));
		documento.setContenuto(docPrincipaleBase64);
		return documento;
	}

	private byte[] buildPdfFromTemplate(Pratica pratica, String templateName, List<Allegato> allegati, PassaggioStato passaggioStato, String infoPassaggioStato) {
		byte[] pdf=null;

		try {
			File file = ResourceUtils.getFile("classpath:templates/"+templateName);

			pdf = Files.readAllBytes(file.toPath());

		} catch (IOException e) {
			throw new BusinessException(ErrorCode.E31, "Errore in fase di lettura template "+templateName);
		}

		// costruisco mappa valori da utilizzare nel replace dei placeholder dei
		// template
		Map<String, String> valueMap = new HashMap<>();

		utilsService.objectToMap("pratica", pratica, valueMap);

		// gestione valori custom non parsati in automatico

		// gestione campi destinatario
		if(pratica.getDestinatario()==null){
			valueMap.put("pratica.destinatario.cognome", "");
			valueMap.put("pratica.destinatario.nome", "");
			valueMap.put("pratica.destinatario.denominazione", "");
			valueMap.put("pratica.destinatario.codiceFiscalePartitaIva", "");
			valueMap.put("pratica.destinatario.recapitoTelefonico", "");
			valueMap.put("pratica.destinatario.email", "");
		}

		// gestione orari null
		if (valueMap.get("pratica.datiRichiesta.oraInizioOccupazione") == null) {
			valueMap.put("pratica.datiRichiesta.oraInizioOccupazione", "");
		}
		if (valueMap.get("pratica.datiRichiesta.oraScadenzaOccupazione") == null) {
			valueMap.put("pratica.datiRichiesta.oraScadenzaOccupazione", "");
		}

		// gestione tipo manufatto altro
		if (pratica.getDatiRichiesta().getManufatto() == null) {
			valueMap.put("pratica.datiRichiesta.manufatto.descrizione", "");
		}

		if(pratica.getProtocolli()!=null&&pratica.getProtocolli().size()>0){
			Comparator<Protocollo> comparator = Comparator.comparing( Protocollo::getNumeroProtocollo );

			Protocollo min = pratica.getProtocolli().stream().min(comparator).get();

			if(min!=null){
				valueMap.put("pratica.numeroProtocollo", min.getCodiceProtocollo());
			}

		}

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormatterPattern);

		valueMap.put("dataDocumento", dateFormatter.format(LocalDate.now()));

		String[] documentiAllegati=null;

		if(allegati!=null&&allegati.size()>0){

			documentiAllegati=new String[allegati.size()];

			for(int i=0;i<allegati.size();i++){
				String nomeFile=allegati.get(i).getTipoAllegato().getDescrizione()+"_"+(allegati.get(i).getNomeFile()==null?"file_"+i:allegati.get(i).getNomeFile());
				documentiAllegati[i]= nomeFile;
			}

			//valueMap.put("documentiAllegati", String.join(",", documentiAllegati));
		}

		if(infoPassaggioStato != null) {
			valueMap.put("infoPassaggioStato", infoPassaggioStato);
		} else {
			valueMap.put("infoPassaggioStato", passaggioStato.getDescStatoPratica());
		}

		valueMap.put("pratica.datiRichiesta.presScivoliDiversamenteAbili",pratica.getDatiRichiesta().getPresScivoliDiversamenteAbili()==null||!pratica.getDatiRichiesta().getPresScivoliDiversamenteAbili()?"NO":"SI");
		valueMap.put("pratica.datiRichiesta.presPassiCarrabiliDiversamenteAbili",pratica.getDatiRichiesta().getPresPassiCarrabiliDiversamenteAbili()==null||!pratica.getDatiRichiesta().getPresPassiCarrabiliDiversamenteAbili()?"NO":"SI");
		valueMap.put("pratica.datiRichiesta.stalloDiSosta",pratica.getDatiRichiesta().getStalloDiSosta()==null||!pratica.getDatiRichiesta().getStalloDiSosta()?"NO":"SI");

		/*
		for (Map.Entry<String, String> entry : valueMap.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		 */

		// processo il file
		System.out.println("====================================BEGIN processTemplate");

		pdf=utilsService.processTemplate(pdf, valueMap);

		System.out.println("====================================END processTemplate");
		try {
			System.out.println("====================================BEGIN docxToPdf");

			pdf = docxToPdf(pdf, documentiAllegati);

			System.out.println("====================================END docxToPdf");
		} catch (Docx4JException e) {
			throw new BusinessException(ErrorCode.E22, "Errore conversione docx in pdf "+ templateName);
		}

		return pdf;
	}

	private List<AllegatoDTO> buildAllegati(List<Allegato> allegati) {
		List<AllegatoDTO> _allegati = new ArrayList<AllegatoDTO>();
		if (allegati != null && allegati.size() > 0) {
			allegati.forEach(_allegato -> {

				AllegatoDTO allegato = new AllegatoDTO();

				DocumentoDTO documento = new DocumentoDTO();
				documento.setContenuto(_allegato.getFileAllegato());
				documento.setTitolo(_allegato.getNomeFile());
				documento.setNomeFile(_allegato.getNomeFile());

				allegato.setDocumento(documento);

				_allegati.add(allegato);
			});
		}
		return _allegati;
	}

	/**
	 * Costruisce un oggetto {@link MittenteDTO} a partire dalle informazioni del
	 * firmatario, e a seconda che si tratti di una persona giuridica oppure fisica
	 *
	 * @param firmatario
	 * @return le informazioni del mittente
	 */
	private MittenteDTO buildMittenteEntrataRequest(Richiedente firmatario) {
		if (firmatario.getDenominazione() != null) {
			PersonaGiuridicaDTO personaGiuridica = new PersonaGiuridicaDTO();
			personaGiuridica.setRagioneSociale(firmatario.getDenominazione());
			personaGiuridica.setPiva(firmatario.getCodiceFiscalePartitaIva());

			PersonaGiuridicaContainerDTO personaGiuridicaContainer = new PersonaGiuridicaContainerDTO();
			personaGiuridicaContainer.setPersonaGiuridica(personaGiuridica);
			return personaGiuridicaContainer;
		} else {
			PersonaFisicaDTO personaFisica = new PersonaFisicaDTO();
			personaFisica.setNome(firmatario.getNome());
			personaFisica.setCognome(firmatario.getCognome());
			personaFisica.setCodiceFiscale(firmatario.getCodiceFiscalePartitaIva());

			PersonaFisicaContainerDTO personaFisicaContainer = new PersonaFisicaContainerDTO();
			personaFisicaContainer.setPersonaFisica(personaFisica);
			return personaFisicaContainer;
		}
	}

	/**
	 * Costruisce la request per la protocollazione della pratica in uscita
	 * @param pratica
	 * @param utente
	 * @param evento
	 * @param destinatari
	 * @param comunicazioneCittadino
	 * @param templateName
	 * @return
	 */
	private ProtocolloUscitaRequest buildProtocolloUscitaRequest(Pratica pratica, Utente utente, String evento, List<Utente> destinatari, boolean comunicazioneCittadino, String templateName, List<Allegato> allegati, PassaggioStato passaggioStato, String infoPassaggioStato) {
		ProtocolloRequest protocolloRequest = new ProtocolloRequest();
		protocolloRequest.setDestinatari(this.buildDestinatariUscitaRequest(pratica.getFirmatario(), destinatari, comunicazioneCittadino, utente));
		protocolloRequest.setDocumento(this.buildDocumentoRequest(pratica, templateName, allegati, passaggioStato, infoPassaggioStato));
		protocolloRequest.setAreaOrganizzativaOmogenea(codiceAmministrazione);
		protocolloRequest.setAmministrazione(codiceAmministrazione);
		protocolloRequest.setOggetto(buildOggetto(pratica));
		protocolloRequest.setIdUtente(utente.getUoId());

		/*
		DocumentoDTO documento = new DocumentoDTO();
		documento.setNomeFile(CaseUtils.toCamelCase("Pratica.pdf", true, ' '));
		documento.setContenuto(new String("ciao").getBytes());

		protocolloRequest.setDocumento(documento);*/

		ProtocolloUscitaRequest protocolloUscita = new ProtocolloUscitaRequest();
		protocolloUscita.setProtocolloUscitaRequest(protocolloRequest);

		return protocolloUscita;
	}

	/**
	 *
	 * Costruisce la request per la protocollazione della pratica in uscita
	 *
	 * @param pratica
	 * @param utente
	 * @param evento
	 * @param destinatari
	 * @param comunicazioneCittadino
	 * @param docPrincipaleBase64
	 * @return
	 */
	private ProtocolloUscitaRequest buildProtocolloUscitaRequest(Pratica pratica, Utente utente, String evento, List<Utente> destinatari, boolean comunicazioneCittadino, byte[] docPrincipaleBase64, String extensionDocPrincipale) {
		ProtocolloRequest protocolloRequest = new ProtocolloRequest();
		protocolloRequest.setDestinatari(this.buildDestinatariUscitaRequest(pratica.getFirmatario(), destinatari, comunicazioneCittadino, utente));
		protocolloRequest.setDocumento(this.buildDocumentoRequest(pratica, docPrincipaleBase64, extensionDocPrincipale));
		protocolloRequest.setAreaOrganizzativaOmogenea(codiceAmministrazione);
		protocolloRequest.setAmministrazione(codiceAmministrazione);
		protocolloRequest.setOggetto(buildOggetto(pratica));
		protocolloRequest.setIdUtente(utente.getUoId());

		ProtocolloUscitaRequest protocolloUscita = new ProtocolloUscitaRequest();
		protocolloUscita.setProtocolloUscitaRequest(protocolloRequest);

		if (protocolloRequest.getDestinatari() != null) {
			for(DestinatarioDTO d: protocolloRequest.getDestinatari()) {
				log.info("-----------------------------------------------------------------");
				log.info("-----------------------------------------------------------------");
				if (d instanceof PersonaFisicaDTO) {
					log.info(((PersonaFisicaDTO) d).getNome() + " " + ((PersonaFisicaDTO) d).getCognome() + " " + ((PersonaFisicaDTO) d).getCodiceFiscale());
				} else if (d instanceof PersonaGiuridicaDTO) {
					log.info(((PersonaGiuridicaDTO) d).getRagioneSociale() + " " + ((PersonaGiuridicaDTO) d).getPiva());
				}
				log.info("-----------------------------------------------------------------");
				log.info("-----------------------------------------------------------------");
			}
		}

		return protocolloUscita;
	}

	/**
	 * Costruisce la request per la protocollazione della pratica in uscita
	 *
	 * @param pratica
	 * @param utente
	 * @param evento
	 * @param destinatari
	 * @param comunicazioneCittadino
	 * @return la request per il protocollo in uscita
	 */
	private ProtocolloUscitaRequest buildProtocolloUscitaRequest(Pratica pratica, Utente utente, String evento, List<Utente> destinatari, boolean comunicazioneCittadino) {
		ProtocolloRequest protocolloRequest = new ProtocolloRequest();
		protocolloRequest.setDestinatari(this.buildDestinatariUscitaRequest(pratica.getFirmatario(), destinatari, comunicazioneCittadino, utente));
		protocolloRequest.setDocumento(this.buildDocumentoRequest(pratica));
		protocolloRequest.setAreaOrganizzativaOmogenea(codiceAmministrazione);
		protocolloRequest.setAmministrazione(codiceAmministrazione);
		protocolloRequest.setOggetto(pratica.getTipoProcesso().getDescrizione() + " "
				+ pratica.getFirmatario().getCodiceFiscalePartitaIva() + " - Stato "
				+ pratica.getStatoPratica().getDescrizione() + " - Mittente " + utente.getRagioneSociale());
		protocolloRequest.setIdUtente(utente.getUoId());

		ProtocolloUscitaRequest protocolloUscita = new ProtocolloUscitaRequest();

		return protocolloUscita;
	}

	/**
	 * Costruisce la lista dei destinatari, a seconda se si tratti di cittadini o
	 * utenti del sistema
	 *
	 * @param firmatario
	 * @param destinatari
	 * @param comunicazioneCittadino
	 * @param utente
	 * @return la lista dei destinatari
	 */
	private List<DestinatarioDTO> buildDestinatariUscitaRequest(Richiedente firmatario, List<Utente> destinatari,
																boolean comunicazioneCittadino, Utente utente) {

		List<DestinatarioDTO> result = new ArrayList<>();

		if (comunicazioneCittadino) {
			if (firmatario.getDenominazione() != null) {
				PersonaGiuridicaDTO personaGiuridica = new PersonaGiuridicaDTO();
				personaGiuridica.setRagioneSociale(firmatario.getDenominazione());
				personaGiuridica.setPiva(firmatario.getCodiceFiscalePartitaIva());
				result.add(personaGiuridica);
			} else {
				PersonaFisicaDTO personaFisica = new PersonaFisicaDTO();
				personaFisica.setNome(firmatario.getNome());
				personaFisica.setCognome(firmatario.getCognome());
				personaFisica.setCodiceFiscale(firmatario.getCodiceFiscalePartitaIva());
				result.add(personaFisica);
			}
		}

		if (!destinatari.isEmpty()) {

			// rimozione comunicazioni interne al municipio (gli utenti dello stesso
			// municipio hanno lo stesso UO id)
			/*destinatari = destinatari.stream().filter(u -> u.getUoId()!=null && utente.getUoId()!=null && !u.getUoId().equals(utente.getUoId()))
					.collect(Collectors.toList());*/
			destinatari = destinatari.stream().filter(
				u ->
					u.getGruppo().getId().equals(Constants.ID_GRUPPO_CONCESSIONARIO) ||
					(
						u.getUoId()!=null && utente.getUoId()!=null && !u.getUoId().equals(utente.getUoId())
					)
			).collect(Collectors.toList());
			List<String> alreadyPresentUoid = new ArrayList<>();
			for (Utente destinatario : destinatari) {
				if(!alreadyPresentUoid.contains(destinatario.getUoId())) {
					alreadyPresentUoid.add(destinatario.getUoId());
					PersonaGiuridicaDTO personaGiuridica = new PersonaGiuridicaDTO();
					//concessionario
					if (destinatario.getGruppo().getId().equals(Constants.ID_GRUPPO_CONCESSIONARIO)) {
						personaGiuridica.setRagioneSociale(destinatario.getRagioneSociale());
						personaGiuridica.setPiva(destinatario.getCodiceFiscale());
					}else{
						if(destinatario.getUoId()!=null&&!destinatario.getUoId().equals("")){
							AutomiProtocolloDTO automiProtocolloDTO=automiProtocolloService.getAutomaProtocolloDTOByUoid(destinatario.getUoId());

							if(automiProtocolloDTO!=null){
								personaGiuridica.setRagioneSociale(automiProtocolloDTO.getDenominazione());
							}
						}
					}

					result.add(personaGiuridica);
				}
			}
		}

		if (result.isEmpty()) {
			throw new BusinessException(ErrorCode.E14, "Errore: impossibile effettuare la protocollazione perché la lista dei destinatari risulta vuota. Verificare la configurazione utenti in particolare l'associazione del corretto uoid.");
		}

		return result;
	}


	@Deprecated
	@Override
	public ProtocolloResponse getNumeroProtocolloEntrata(Pratica pratica, Utente utente, String evento) {

		// costruisco request
		ProtocolloEntrataRequest request = this.buildProtocolloEntrataRequest(pratica, utente, evento);
		HttpEntity<ProtocolloEntrataRequest> entity = new HttpEntity<>(request);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		// log del JSON della request
		try {
			log.info("Request protocollo entrata: " + objectMapper.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Errore nella conversione della request in json");
		}

		ProtocolloResponse response = null;

		String urlProcolloEntrata = apiBaseUrl + "/middleware/v1/protocollo/richiesta/entrata";
		log.info("Url servizio protocollo entrata: " + urlProcolloEntrata);

		try {
			// invoco servizio protocollazione in entrata
			response = restTemplateProtocollazione.postForObject(urlProcolloEntrata, entity, ProtocolloResponse.class);
		} catch (Exception e) {
			log.error("Errore durante la chiamata del serivizio di protocollazione in entrata", e);
			throw new BusinessException(ErrorCode.E14, "Errore: non è stato possibile effettuare la protocollazione");
		}

		// DEBUG
//		response = new ProtocolloResponse();
//		response.setNumeroProtocollo("375");
//		response.setAnno("2022");
//		response.setDataProtocollo(LocalDateTime.now());

		if (response == null || response.getNumeroProtocollo() == null ||response.getAnno() == null||response.getDataProtocollo() == null) {
			throw new BusinessException(ErrorCode.E14, "Errore: non è stato possibile effettuare la protocollazione");
		}

		// log del JSON della response
		try {
			log.info("Response protocollo entrata: " + objectMapper.writeValueAsString(response));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Errore nella conversione della response in json");
		}

		return response;
	}

	private byte[] docxToPdf(byte[] docx, String[] documentiAllegati ) throws Docx4JException {
		InputStream is = new ByteArrayInputStream(docx);
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(is);

			if(documentiAllegati!=null&&documentiAllegati.length>0){
				MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();

				int i=1;
				for(String allegato:documentiAllegati){
					addParagraph(mainDocumentPart, (i++)+") "+allegato);
				}

			}

			Docx4J.toPDF(wordMLPackage, os);
		}
		finally {
			try {
				os.flush();
				os.close();
			} catch (IOException ioex) {}
		}

		return os.toByteArray();
	}

	private void addParagraph(MainDocumentPart mainDocumentPart, String text){
		HpsMeasure size = new HpsMeasure();
		size.setVal(BigInteger.valueOf(19));

		ObjectFactory factory = Context.getWmlObjectFactory();
		P p = factory.createP();
		R r = factory.createR();

		Text t = factory.createText();
		t.setValue(text);

		r.getContent().add(t);
		p.getContent().add(r);
		RPr rpr = factory.createRPr();
		rpr.setSz(size);
		r.setRPr(rpr);

		mainDocumentPart.getContent().add(p);
	}


}
