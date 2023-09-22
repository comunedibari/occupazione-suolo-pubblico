package it.fincons.osp.services.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.TemplateDTO;
import it.fincons.osp.dto.TemplateSimplifiedDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.TemplateMapper;
import it.fincons.osp.model.Gruppo;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.Protocollo;
import it.fincons.osp.model.RichiestaParere;
import it.fincons.osp.model.StatoPratica;
import it.fincons.osp.model.Template;
import it.fincons.osp.model.TipoTemplate;
import it.fincons.osp.model.Utente;
import it.fincons.osp.repository.GruppoRepository;
import it.fincons.osp.repository.PraticaRepository;
import it.fincons.osp.repository.StatoPraticaRepository;
import it.fincons.osp.repository.TemplateRepository;
import it.fincons.osp.repository.TipoTemplateRepository;
import it.fincons.osp.repository.UtenteRepository;
import it.fincons.osp.services.ProtocolloService;
import it.fincons.osp.services.TemplateService;
import it.fincons.osp.services.UtilsService;
import it.fincons.osp.utils.Constants;
import lombok.extern.slf4j.Slf4j;

import static it.fincons.osp.utils.Constants.*;

@Slf4j
@Service
public class TemplateServiceImpl implements TemplateService {

	@Value("${osp.app.template.placeholder.start}")
	private String templatePlaceholderStart;

	@Value("${osp.app.template.placeholder.end}")
	private String templatePlaceholderEnd;

	@Value("${osp.app.format.date-time}")
	private String dateTimeFormatterPattern;
	
	@Value("${osp.app.format.date}")
	private String dateFormatterPattern;

	@Autowired
	TemplateRepository templateRepository;

	@Autowired
	TemplateMapper templateMapper;

	@Autowired
	TipoTemplateRepository tipoTemplateRepository;

	@Autowired
	PraticaRepository praticaRepository;

	@Autowired
	UtilsService utilsService;

	@Autowired
	UtenteRepository utenteRepository;

	@Autowired
	ProtocolloService protocolloService;

	@Autowired
	StatoPraticaRepository statoPraticaRepository;

	@Autowired
	GruppoRepository gruppoRepository;

	@Override
	@LogEntryExit
	public TemplateDTO insertTemplate(TemplateDTO templateRequest) {

		if (templateRequest.getUtenteModifica() == null || templateRequest.getUtenteModifica().getId() == null) {
			throw new BusinessException(ErrorCode.E1, "Errore: utente modifica o id utente modifica null");
		}

		Utente utente = utenteRepository.findById(templateRequest.getUtenteModifica().getId())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema",
						templateRequest.getUtenteModifica().getId()));

		if (templateRequest.getTipoTemplate() == null || templateRequest.getTipoTemplate().getId() == null) {
			throw new BusinessException(ErrorCode.E1, "Errore: tipo template o id tipo template null");
		}
		TipoTemplate tipoTemplate = tipoTemplateRepository.findById(templateRequest.getTipoTemplate().getId())
				.orElseThrow(
						() -> new BusinessException(ErrorCode.E1, "Errore: id tipo template non presente nel sistema",
								templateRequest.getTipoTemplate().getId()));

		if (templateRepository.findByTipoTemplate(tipoTemplate).isPresent()) {
			throw new BusinessException(ErrorCode.E7, "Errore: esiste già un template per questa tipologia",
					tipoTemplate.getId());
		}

		if (!templateRequest.getMimeType().equals(Constants.MIME_TYPE_DOCX)) {
			throw new BusinessException(ErrorCode.E20, "Errore: il template deve essere in formato .docx",
					templateRequest.getMimeType());
		}

		Template template = new Template();
		template.setTipoTemplate(tipoTemplate);
		template.setNomeFile(templateRequest.getNomeFile());
		template.setMimeType(templateRequest.getMimeType());
		template.setFileTemplate(
				Base64.getDecoder().decode(templateRequest.getFileTemplate().getBytes(StandardCharsets.UTF_8)));
		template.setDataInserimento(LocalDateTime.now().withNano(0));
		template.setUtenteModifica(utente);

		templateRepository.save(template);

		log.info("Template inserito correttamente");

		return templateMapper.entityToDto(template);
	}

	@Override
	@LogEntryExit
	public TemplateDTO updateTemplate(TemplateDTO templateRequest) {

		if (templateRequest.getId() == null) {
			throw new BusinessException(ErrorCode.E1, "Errore: id template null");
		}

		Template template = templateRepository.findById(templateRequest.getId())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: id template non presente nel sistema",
						templateRequest.getId()));

		if (templateRequest.getUtenteModifica() == null || templateRequest.getUtenteModifica().getId() == null) {
			throw new BusinessException(ErrorCode.E1, "Errore: utente modifica o id utente modifica null");
		}

		Utente utente = utenteRepository.findById(templateRequest.getUtenteModifica().getId())
				.orElseThrow(() -> new BusinessException(ErrorCode.E1, "Errore: ID utente non presente nel sistema",
						templateRequest.getUtenteModifica().getId()));

		if (!templateRequest.getMimeType().equals(Constants.MIME_TYPE_DOCX)) {
			throw new BusinessException(ErrorCode.E20, "Errore: il template deve essere in formato .docx",
					templateRequest.getMimeType());
		}

		template.setNomeFile(templateRequest.getNomeFile());
		template.setMimeType(templateRequest.getMimeType());
		template.setFileTemplate(
				Base64.getDecoder().decode(templateRequest.getFileTemplate().getBytes(StandardCharsets.UTF_8)));
		template.setDataModifica(LocalDateTime.now().withNano(0));
		template.setUtenteModifica(utente);

		templateRepository.save(template);

		log.info("Template aggiornato correttamente");

		return templateMapper.entityToDto(template);
	}

	@Override
	@LogEntryExit(showArgs = true)
	public TemplateDTO getTemplateByTipo(Integer idTipoTemplate) {

		TipoTemplate tipoTemplate = tipoTemplateRepository.findById(idTipoTemplate)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: id tipo template non presente nel sistema", idTipoTemplate));

		Template result = templateRepository.findByTipoTemplate(tipoTemplate)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: nessun template trovato per questo tipo", idTipoTemplate));

		return templateMapper.entityToDto(result);
	}

	@Override
	@LogEntryExit(showArgs = true)
	public TemplateDTO getTemplate(Integer id) {
		Template template = templateRepository.findById(id).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: id template non presente nel sistema", id));

		return templateMapper.entityToDto(template);
	}

	@Override
	@LogEntryExit(showArgs = true)
	public TemplateDTO getTemplateElaborato(Long idPratica, Integer idTipoTemplate, String notaParere) {

		Pratica pratica = praticaRepository.findById(idPratica).orElseThrow(
				() -> new BusinessException(ErrorCode.E1, "Errore: ID pratica non presente nel sistema", idPratica));

		if(
				!(ID_TIPO_TEMPLATE_ORDINANZA==idTipoTemplate||
				ID_TIPO_TEMPLATE_RELAZIONE_SERVIZIO==idTipoTemplate||
				ID_TIPO_TTEMPLATE_ISTRUTTORIA_IVOOPP_URBANIZZAZIONI_PRIMARIE==idTipoTemplate||
				ID_TIPO_TTEMPLATE_ISTRUTTORIA_IVOOPP_URBANIZZAZIONI_GIARDINI==idTipoTemplate||
				ID_TIPO_TTEMPLATE_ISTRUTTORIA_IVOOPP_INTERVENTI_TERRITORIO==idTipoTemplate||
				ID_TIPO_TTEMPLATE_ISTRUTTORIA_IVOOPP_INFRASTRUTTURE_RETE==idTipoTemplate||
				TID_TIPO_TEMPLATE_ISTRUTTORIA_URBANISTICA==idTipoTemplate||
				ID_TIPO_TTEMPLATE_ISTRUTTORIA_PATRIMONIO==idTipoTemplate|| ID_TIPO_TEMPLATE_DETERMINA_RIGETTO == idTipoTemplate || ID_TIPO_TEMPLATE_DETERMINA_RINUNCIA == idTipoTemplate ||
				ID_TIPO_TEMPLATE_DETERMINA_RETTIFICA == idTipoTemplate ||
				ID_TIPO_TEMPLATE_DETERMINA_DECADENZA == idTipoTemplate ||
				ID_TIPO_TEMPLATE_DETERMINA_REVOCA == idTipoTemplate ||
				ID_TIPO_TEMPLATE_DETERMINA_ANNULLAMENTO == idTipoTemplate ||
				ID_TIPO_TEMPLATE_DETERMINA_SOSPENSIONE == idTipoTemplate)
		){
			Boolean flagEsenzioneMarcaDaBollo = pratica.getDatiRichiesta().isFlagEsenzioneMarcaDaBollo();
			Boolean flagEsenzionePagamentoCUP = pratica.isFlagEsenzionePagamentoCUP();

			if(
					pratica.getTipoProcesso().getId().intValue()==Constants.ID_TIPO_PROCESSO_CONCESSIONE_TEMPORANEA.intValue() ||
							pratica.getTipoProcesso().getId().intValue()==Constants.ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA.intValue()
			){

				if(flagEsenzioneMarcaDaBollo&&flagEsenzionePagamentoCUP){
					if(pratica.getTipoProcesso().getId().intValue()==Constants.ID_TIPO_PROCESSO_CONCESSIONE_TEMPORANEA.intValue()){
						idTipoTemplate=Constants.ID_TIPO_TEMPLATE_DETERMINA_CONCESSIONE_ESENTE_BOLLO_CUP;
					}else{
						idTipoTemplate=Constants.ID_TIPO_TEMPLATE_DETERMINA_PROROGA_ESENTE_BOLLO_CUP;
					}
				}else if(flagEsenzioneMarcaDaBollo){
					if(pratica.getTipoProcesso().getId().intValue()==Constants.ID_TIPO_PROCESSO_CONCESSIONE_TEMPORANEA.intValue()){
						idTipoTemplate=Constants.ID_TIPO_TEMPLATE_DETERMINA_CONCESSIONE_ESENTE_BOLLO;
					}else{
						idTipoTemplate=Constants.ID_TIPO_TEMPLATE_DETERMINA_PROROGA_ESENTE_BOLLO;
					}
				}else if(flagEsenzionePagamentoCUP){
					if(pratica.getTipoProcesso().getId().intValue()==Constants.ID_TIPO_PROCESSO_CONCESSIONE_TEMPORANEA.intValue()){
						idTipoTemplate=Constants.ID_TIPO_TEMPLATE_DETERMINA_CONCESSIOONE_ESENTE_CUP;
					}else{
						idTipoTemplate=Constants.ID_TIPO_TEMPLATE_DETERMINA_PROROGA_ESENTE_CUP;
					}
				}
			}
		}

		Integer finalIdTipoTemplate = idTipoTemplate;

		TipoTemplate tipoTemplate = tipoTemplateRepository.findById(idTipoTemplate)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: ID tipo template non presente nel sistema", finalIdTipoTemplate));

		Template template = templateRepository.findByTipoTemplate(tipoTemplate)
				.orElseThrow(() -> new BusinessException(ErrorCode.E1,
						"Errore: nessun template trovato per questo tipo", tipoTemplate.getId()));


		// costruisco mappa valori da utilizzare nel replace dei placeholder dei
		// template
		Map<String, String> valueMap = new HashMap<>();
		utilsService.objectToMap("pratica", pratica, valueMap);

		// gestione valori custom non parsati in automatico

		// recupero stato pratica "inserita" per ricercare il numero di protocollo
		// iniziale
		StatoPratica statoPraticaInserita = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_INSERITA)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica INSERITA non trovato"));

		// recupero numero di protocollo dell'inserimento della pratica
		Protocollo protocollo = protocolloService.getProtocollo(pratica, statoPraticaInserita);
		valueMap.put("pratica.numeroProtocollo", protocollo.getCodiceProtocollo());

		valueMap.put("parere.poliziaLocale.nota", "");
		valueMap.put("parere.nota", "");
		valueMap.put("determina.richiestaParere.poliziaLocale.codiceProtocollo", "");
		valueMap.put("determina.richiestaParere.poliziaLocale.dataProtocollo", "");
		valueMap.put("determina.parere.poliziaLocale.codiceProtocollo", "");
		valueMap.put("determina.parere.poliziaLocale.dataProtocollo", "");
		valueMap.put("determina.parere.poliziaLocale.nota", "");
		valueMap.put("utente.concessionario.ragioneSociale", "");
		valueMap.put("utente.concessionario.indirizzo", "");
		valueMap.put("pratica.datiRichiesta.manufatto.descrizione", "");
		valueMap.put("pratica.praticaOriginaria.codiceDetermina", "");
		valueMap.put("pratica.praticaOriginaria.dataEmissioneDetermina","");

		// gestione campi destinatario
		String cognomeDenominazioneDestinatario = "";
		if (pratica.getDestinatario() != null) {
			if (pratica.getDestinatario().getDenominazione() != null) {
				cognomeDenominazioneDestinatario = "di " + pratica.getDestinatario().getDenominazione();
			} else {
				cognomeDenominazioneDestinatario = "di " + pratica.getDestinatario().getCognome();
			}
		}
		valueMap.put("pratica.destinatario.cognome.denominazione", cognomeDenominazioneDestinatario);

		// gestione note parere polizia locale relazione di servizio e note parere
		// istruttoria tecnica
		if (notaParere != null) {
			valueMap.put("parere.poliziaLocale.nota", notaParere);
			valueMap.put("parere.nota", notaParere);
		}

		// gestione richiesta parere polizia locale
		if (!pratica.getRichiestePareri().isEmpty()) {
			Optional<RichiestaParere> richiestaParereTrovata = pratica.getRichiestePareri().stream()
					.filter(r -> r.getGruppoDestinatarioParere().getId().equals(Constants.ID_GRUPPO_POLIZIA_LOCALE))
					.findFirst();

			if (richiestaParereTrovata.isPresent()) {
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormatterPattern);
				RichiestaParere richiestaParere = richiestaParereTrovata.get();

				valueMap.put("determina.richiestaParere.poliziaLocale.codiceProtocollo",
						richiestaParere.getCodiceProtocollo());
				valueMap.put("determina.richiestaParere.poliziaLocale.dataProtocollo",
						dateTimeFormatter.format(richiestaParere.getDataProtocollo()));

				// controllo di sicurezza, in teoria essendo presente il flag inserita risposta,
				// il parere dovrebbe essere sempre presente
				if (richiestaParere.getParere() != null) {
					valueMap.put("determina.parere.poliziaLocale.codiceProtocollo",
							richiestaParere.getParere().getCodiceProtocollo());
					valueMap.put("determina.parere.poliziaLocale.dataProtocollo",
							dateTimeFormatter.format(richiestaParere.getParere().getDataProtocollo()));

					String notaParerePoliziaLocale = "";

					if (!StringUtils.isBlank(richiestaParere.getParere().getNota())) {
						notaParerePoliziaLocale = richiestaParere.getParere().getNota();
					}
					valueMap.put("determina.parere.poliziaLocale.nota", notaParerePoliziaLocale);
				}
			}
		}

		// gestione orari null
		if (valueMap.get("pratica.datiRichiesta.oraInizioOccupazione") == null) {
			valueMap.put("pratica.datiRichiesta.oraInizioOccupazione", "");
		}
		if (valueMap.get("pratica.datiRichiesta.oraScadenzaOccupazione") == null) {
			valueMap.put("pratica.datiRichiesta.oraScadenzaOccupazione", "");
		}

		// gestione utente concessionario
		Gruppo gruppoConcessionario = gruppoRepository.findById(Constants.ID_GRUPPO_CONCESSIONARIO)
				.orElseThrow(() -> new RuntimeException("Errore: gruppo CONCESSIONARIO non trovato"));
		List<Utente> listaUtentiConcessionario = utenteRepository
				.findDistinctByGruppoInAndEnabledTrueAndFlagEliminatoFalse(List.of(gruppoConcessionario));

		if (!listaUtentiConcessionario.isEmpty()) {
			// puo' esistere un solo utente concessionario
			Utente utenteConcessionario = listaUtentiConcessionario.get(0);
			valueMap.put("utente.concessionario.ragioneSociale", utenteConcessionario.getRagioneSociale());
			valueMap.put("utente.concessionario.indirizzo", utenteConcessionario.getIndirizzo());
		}

		// gestione nome cognome destinatario
		valueMap.put("pratica.firmatario.cognomeNome",
				pratica.getFirmatario().getCognome() + " " + pratica.getFirmatario().getNome());

		// gestione tipo manufatto altro
		if (pratica.getDatiRichiesta().getManufatto() != null
				&& pratica.getDatiRichiesta().getManufatto().isFlagTestoLibero()) {
			valueMap.put("pratica.datiRichiesta.manufatto.descrizione",
					pratica.getDatiRichiesta().getDescrizioneManufatto());
		}
		
		// gestione codice determina e data emissione pratica originaria
		Long idPraticaOriginaria = null;

		if (pratica.getIdProrogaPrecedente() != null) {
			idPraticaOriginaria = pratica.getIdProrogaPrecedente();
		} else if (pratica.getIdPraticaOriginaria() != null) {
			idPraticaOriginaria = pratica.getIdPraticaOriginaria();
		}

		if (idPraticaOriginaria != null) {
			Pratica praticaOriginaria = praticaRepository.findById(idPraticaOriginaria)
					.orElseThrow(() -> new BusinessException(ErrorCode.E1,
							"Errore: ID pratica precedente non presente nel sistema"));

			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormatterPattern);

			valueMap.put("pratica.praticaOriginaria.codiceDetermina", praticaOriginaria.getCodiceDetermina());
			valueMap.put("pratica.praticaOriginaria.dataEmissioneDetermina",
					dateFormatter.format(praticaOriginaria.getDataEmissioneDetermina()));
		}

		if(valueMap!=null&&valueMap.size()>0){
			valueMap.replaceAll((k,v)-> v == null ? "":v);
		}

		// processo il file
		byte[] fileTemplateProcessato = this.processTemplate(template.getFileTemplate(), valueMap);

		TemplateDTO result = templateMapper.entityToDto(template);

		// setto il file elaborato
		result.setFileTemplate(new String(Base64.getEncoder().encode(fileTemplateProcessato)));

		return result;
	}

	@Override
	@LogEntryExit
	public List<TemplateSimplifiedDTO> getAllTemplates() {
		return templateRepository.findAll(Sort.by(Sort.Direction.ASC, "tipoTemplate.id")).stream()
				.map(t -> templateMapper.entityToSimplifiedDto(t)).collect(Collectors.toList());
	}

	/**
	 * Elabora il file template, effettuando il replace delle parole contrassegnate
	 * con i placeholder con i corrispondenti attributi contenuti nella mappa
	 * 
	 * @param fileTemplate
	 * @param valueMap
	 * @return il file elaborato
	 */
	@Deprecated
	private byte[] processTemplate(byte[] fileTemplate, Map<String, String> valueMap) {
		XWPFDocument doc;
		try {
			doc = new XWPFDocument(OPCPackage.open(new ByteArrayInputStream(fileTemplate)));

			for (XWPFParagraph p : doc.getParagraphs()) {
				List<XWPFRun> runs = p.getRuns();
				if (runs != null) {

					boolean startField = false;
					StringBuilder field = new StringBuilder(1000);

					for (XWPFRun r : runs) {
						String text = r.getText(0);

						if (text != null) {
							if (text.contains(templatePlaceholderStart)) {

								// caso in cui il run contenga esattamente l'intero placeholder o piu' placeholder
								if (text.trim().length() > 4) {

									if (StringUtils.countMatches(text, templatePlaceholderStart) > 1) {
										// caso in cui ci sono più placeholder nello stesso run

										List<Integer> listStartIndex = new ArrayList<>();

										int index = text.indexOf("+++=");
										
										String initialPart = "";
										if (index != 0 ) {
											initialPart = text.substring(0, index);
										}
										
										while (index >= 0) {
											listStartIndex.add(index);
											index = text.indexOf("+++=", index + 1);
										}

										List<String> partialKey = new ArrayList<>();

										int i = 0;
										for (Integer currentIndex : listStartIndex) {
											if (i == listStartIndex.size() - 1) {
												String word = text.substring(currentIndex + 4, text.length());
												partialKey.add(word);
											} else {
												String word = text.substring(currentIndex + 4,
														listStartIndex.get(i + 1));
												partialKey.add(word);
												i++;
											}
										}

										StringBuilder resultForReplace = new StringBuilder();
										resultForReplace.append(initialPart);
										for (String currentPartialKey : partialKey) {

											String fieldToCheck = "";
											if (currentPartialKey.contains(templatePlaceholderEnd)) {
												fieldToCheck = currentPartialKey.substring(0,
														currentPartialKey.indexOf(templatePlaceholderEnd));
											} else {
												fieldToCheck = currentPartialKey;
											}

											fieldToCheck = fieldToCheck.trim();

											if (valueMap.containsKey(fieldToCheck)) {
												String finalSpaceToAdd = "";

												if (currentPartialKey.contains(templatePlaceholderEnd)
														&& !currentPartialKey.endsWith(templatePlaceholderEnd)) {
													finalSpaceToAdd = currentPartialKey.substring(
															currentPartialKey.indexOf(templatePlaceholderEnd) + 3,
															currentPartialKey.length());
												}

												resultForReplace.append(valueMap.get(fieldToCheck) + finalSpaceToAdd);
											} else {
												// lascio il placeholder
												resultForReplace.append(currentPartialKey);
											}
										}

										r.setText(resultForReplace.toString(), 0);
									} else {
										if (text.trim().endsWith(templatePlaceholderEnd)) {
											String fieldToCheck = text.trim().substring(4, text.trim().length() - 3)
													.trim();

											if (valueMap.containsKey(fieldToCheck)) {
												String initialSpaceToAdd = "";
												String finalSpaceToAdd = "";

												if (!text.startsWith(templatePlaceholderStart)) {
													initialSpaceToAdd = text.substring(0,
															text.indexOf(templatePlaceholderStart));
												}

												if (!text.endsWith(templatePlaceholderEnd)) {
													finalSpaceToAdd = text.substring(
															text.indexOf(templatePlaceholderEnd,
																	text.indexOf(templatePlaceholderEnd) + 1) + 3,
															text.length());
												}

												r.setText(initialSpaceToAdd + valueMap.get(fieldToCheck)
														+ finalSpaceToAdd, 0);
											} else {
												// lascio il placeholder
												r.setText(text, 0);
											}
										} else if (text.endsWith(templatePlaceholderStart)) {
											if (!field.toString().contains(templatePlaceholderStart)) {
												startField = true;
												field = field.append(templatePlaceholderStart);
												r.setText(text.substring(0, text.indexOf(templatePlaceholderStart)), 0);
											} else {
												// caso in cui nel run ho una cosa del genere: +++ +++=
												if (startField && text.trim().startsWith(templatePlaceholderEnd) && StringUtils.countMatches(text, templatePlaceholderEnd) > 1) {
													field = field.append(templatePlaceholderEnd + " ");
													
													String fieldToCheck = field.toString().trim()
															.substring(4, field.toString().trim().length() - 3).trim();

													if (valueMap.containsKey(fieldToCheck)) {
														String initialSpaceToAdd = "";
														String finalSpaceToAdd = "";

														if (!field.toString().startsWith(templatePlaceholderStart)) {
															initialSpaceToAdd = field.toString().substring(0,
																	field.indexOf(templatePlaceholderStart));
														}

														if (!text.endsWith(templatePlaceholderEnd)) {
															if (text.trim().endsWith(templatePlaceholderStart)) {
																finalSpaceToAdd = text.substring(text.indexOf(templatePlaceholderEnd) + 3,
																		text.indexOf(templatePlaceholderStart));
															} else {
																finalSpaceToAdd = text.substring(text.indexOf(templatePlaceholderEnd) + 3,
																		text.length());
															}
														}

														r.setText(initialSpaceToAdd + valueMap.get(fieldToCheck) + finalSpaceToAdd,
																0);
													} else {
														// lascio il placeholder
														r.setText(field.toString(), 0);
													}

													field.setLength(0);
													startField = true;
													field = field.append(templatePlaceholderStart);
												}
											}
										} else {
											startField = true;
											field = field.append(text);
											r.setText("", 0);
										}
									}
								} else {
									startField = true;
									field = field.append(text);
									r.setText("", 0);
								}
							} else if (startField) {
								
								if (text.contains(templatePlaceholderEnd)) {
									
									// se sentro qui vuol dire che dopo il +++ c'è qualche carattere
									if (!text.endsWith(templatePlaceholderEnd)) {
										field = field.append(text.substring(0, text.indexOf(templatePlaceholderEnd) + 3));
									} else {
										field = field.append(text);
									}

									String fieldToCheck = field.toString().trim()
											.substring(4, field.toString().trim().length() - 3).trim();

									if (valueMap.containsKey(fieldToCheck)) {
										String initialSpaceToAdd = "";
										String finalSpaceToAdd = "";

										if (!field.toString().startsWith(templatePlaceholderStart)) {
											initialSpaceToAdd = field.toString().substring(0,
													field.indexOf(templatePlaceholderStart));
										}

										if (!text.endsWith(templatePlaceholderEnd)) {
											finalSpaceToAdd = text.substring(text.indexOf(templatePlaceholderEnd) + 3,
													text.length());
										}

										r.setText(initialSpaceToAdd + valueMap.get(fieldToCheck) + finalSpaceToAdd,
												0);
									} else {
										// lascio il placeholder
										r.setText(field.toString(), 0);
									}

									field.setLength(0);
									startField = false;
								} else {
									field = field.append(text);
									r.setText("", 0);
								}
							}
						}
					}
				}
			}

			// DEBUG
//			OutputStream outFileSystem = new FileOutputStream("C:\\Users\\alfonso.coppola\\Desktop\\test.docx");
//			doc.write(outFileSystem);
//			outFileSystem.close();

			// trasformo il file elaborato in un byte array
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			doc.write(out);
			out.close();
			doc.close();

			return out.toByteArray();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException(ErrorCode.E12, "Errore nell'elaborazione del template");
		}

	}

}
