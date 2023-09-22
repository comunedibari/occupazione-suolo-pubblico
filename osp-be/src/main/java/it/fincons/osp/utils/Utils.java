package it.fincons.osp.utils;

import java.util.ArrayList;
import java.util.List;

public class Utils {

	private Utils() {
		throw new IllegalStateException("Utility class");
	}

	public static List<Integer> getIdStatiPraticaConclusivi() {
		List<Integer> result = new ArrayList<>();

		result.add(Constants.ID_STATO_PRATICA_RIGETTATA);
		result.add(Constants.ID_STATO_PRATICA_ARCHIVIATA);
		result.add(Constants.ID_STATO_PRATICA_REVOCATA);
		result.add(Constants.ID_STATO_PRATICA_DECADUTA);
		result.add(Constants.ID_STATO_PRATICA_ANNULLATA);
		result.add(Constants.ID_STATO_PRATICA_TERMINATA);
		result.add(Constants.ID_STATO_PRATICA_RINUNCIATA);

		return result;
	}

	public static List<Integer> getIdGruppiDestinatariPareri() {
		List<Integer> result = new ArrayList<>();

		result.add(Constants.ID_GRUPPO_POLIZIA_LOCALE);
		result.add(Constants.ID_GRUPPO_IVOOPP_SET_GIARDINI);
		result.add(Constants.ID_GRUPPO_IVOOPP_SET_INTERVENTI_SUL_TERRITORIO);
		result.add(Constants.ID_GRUPPO_IVOOPP_SET_URB_PRIMARIE);
		result.add(Constants.ID_GRUPPO_IVOOPP_SET_INFRASTRUTTURE_A_RETE);
		result.add(Constants.ID_GRUPPO_RIP_URBANISTICA);
		result.add(Constants.ID_GRUPPO_RIP_PATRIMONIO);

		return result;
	}

	public static List<Integer> getIdStatiPraticaControlloTempiProcedimentali() {
		List<Integer> result = new ArrayList<>();

		result.add(Constants.ID_STATO_PRATICA_INSERITA);
		result.add(Constants.ID_STATO_PRATICA_VERIFICA_FORMALE);
		result.add(Constants.ID_STATO_PRATICA_RICHIESTA_PARERI);
		result.add(Constants.ID_STATO_PRATICA_APPROVATA);

		return result;
	}

	public static List<Integer> getIdStatiPraticaVerificaOccupazione() {
		List<Integer> result = new ArrayList<>();

		result.add(Constants.ID_STATO_PRATICA_INSERITA);
		result.add(Constants.ID_STATO_PRATICA_VERIFICA_FORMALE);
		result.add(Constants.ID_STATO_PRATICA_RICHIESTA_PARERI);
		result.add(Constants.ID_STATO_PRATICA_NECESSARIA_INTEGRAZIONE);

		return result;
	}
	
	public static List<Integer> getIdStatiPraticaVerificaRipristinoDeiLuoghi() {
		List<Integer> result = new ArrayList<>();

		result.add(Constants.ID_STATO_PRATICA_REVOCATA);
		result.add(Constants.ID_STATO_PRATICA_DECADUTA);
		result.add(Constants.ID_STATO_PRATICA_ANNULLATA);
		result.add(Constants.ID_STATO_PRATICA_RINUNCIATA);
		result.add(Constants.ID_STATO_PRATICA_TERMINATA);

		return result;
	}

	/**
	 * Controlla se l'oggetto in input e' un oggetto interno dell'applicazione
	 * 
	 * @param objectToCheck
	 * @return true se e' un oggetto dell'applicazione, false altrimenti
	 */
	public static boolean isProjectObject(Object objectToCheck) {
		return objectToCheck.getClass().getName().startsWith("it.fincons");
	}

	/**
	 * Controlla che il mimeType ssia fra quelli validi
	 * 
	 * @param mimeType
	 * @return true se il mime type è valido, false altrimenti
	 */
	public static boolean isMimeTypeValid(String mimeType) {
		return Constants.MIME_TYPE_LIST.contains(mimeType);
	}

	public static String getFileExtension(String fileName) {
		String[] splitted = fileName.split("\\.");
		return splitted[splitted.length - 1];
	}
}
