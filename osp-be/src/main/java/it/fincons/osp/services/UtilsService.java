package it.fincons.osp.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.fincons.osp.utils.Utils;

@Slf4j
@Service
public class UtilsService {
	@Value("${osp.app.template.placeholder.start}")
	private String templatePlaceholderStart;

	@Value("${osp.app.template.placeholder.end}")
	private String templatePlaceholderEnd;

	@Value("${osp.app.format.date-time}")
	private String dateTimeFormatterPattern;

	@Value("${osp.app.format.date}")
	private String dateFormatterPattern;

	private DateTimeFormatter dateFormatter;
	private DateTimeFormatter dateTimeFormatter;

	@Autowired
	public UtilsService(@Value("${osp.app.format.date}") String dateFormatterPattern,
			@Value("${osp.app.format.date-time}") String dateTimeFormatterPattern) {
		this.dateFormatter = DateTimeFormatter.ofPattern(dateFormatterPattern);
		this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormatterPattern);
	}

	/**
	 * Scansiona ricorsivamente tutti i campi dell'oggetto e li inserisce in una
	 * mappa chiave valore dove la chiave viene costruita concatendando il prefisso
	 * e il nome del campo, mentre il valore e' il valore di quel campo recuperato
	 * attraverso la reflection con i metodi get o is
	 * 
	 * @param prefix - Il prefisso dei campi
	 * @param o      - L'oggetto da parsare
	 * @param result - La mappa con gli oggetti parsati
	 */
	public void objectToMap(String prefix, Object o, Map<String, String> result) {
		Class<? extends Object> co = o.getClass();
		Field[] cfields = co.getDeclaredFields();
		for (Field f : cfields) {
			String attributeName = f.getName();

			if (!attributeName.equalsIgnoreCase("SerialVersionUID") && !attributeName.equalsIgnoreCase("pratica")
					&& !attributeName.equalsIgnoreCase("protocolli")) {
				String getterMethodName = "get" + attributeName.substring(0, 1).toUpperCase()
						+ attributeName.substring(1, attributeName.length());

				String isMethodName = "is" + attributeName.substring(0, 1).toUpperCase()
						+ attributeName.substring(1, attributeName.length());

				// System.out.println(getterMethodName);

				Method m = null;
				try {

					// provo prima il get, nel caso non trovo il metodo potrebbe essere un booleano
					// e provo con is
					try {
						m = co.getMethod(getterMethodName);
					} catch (NoSuchMethodException e) {
						m = co.getMethod(isMethodName);
					}

					Object valObject = m.invoke(o);

					if (valObject != null) {
						if (Utils.isProjectObject(valObject)) {
							objectToMap(prefix + "." + attributeName, valObject, result);
						} else {
							if (valObject instanceof LocalDate) {
								valObject = ((LocalDate) valObject).format(dateFormatter);
							}

							if (valObject instanceof LocalDateTime) {
								valObject = ((LocalDateTime) valObject).format(dateTimeFormatter);
							}

							if (valObject instanceof Boolean) {
								if((Boolean) valObject){
									valObject = "SI";
								}else{
									valObject = "NO";
								}
							}

							result.put(prefix + "." + attributeName, valObject.toString());
						}
					}else{
						result.put(prefix + "." + attributeName, "");
					}
				} catch (Exception e) {
					// ignoro volutamente le eccezioni in quanto potrebbero essere causate
					// dall'esecuzione di metodi costruiti ma inesistenti
				}
			}
		}
	}

	/**
	 * Elabora il file template, effettuando il replace delle parole contrassegnate
	 * con i placeholder con i corrispondenti attributi contenuti nella mappa
	 *
	 * @param fileTemplate
	 * @param valueMap
	 * @return il file elaborato
	 */
	public byte[] processTemplate(byte[] fileTemplate, Map<String, String> valueMap) {
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
