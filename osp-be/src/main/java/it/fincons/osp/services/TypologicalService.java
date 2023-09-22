package it.fincons.osp.services;

import java.util.List;

import it.fincons.osp.dto.GruppoDTO;
import it.fincons.osp.dto.TipoAllegatoDTO;
import it.fincons.osp.dto.TipoAllegatoGruppoStatoProcessoDTO;
import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.dto.TypologicalFlagTestoLiberoDTO;

public interface TypologicalService {

	/**
	 * Ritorna tutti i Gruppi
	 * 
	 * @return la lista dei gruppi
	 */
	public List<GruppoDTO> getGruppi();

	/**
	 * Ritorna tutti i Municipi
	 * 
	 * @return la lista dei municipi
	 */
	public List<TypologicalDTO> getMunicipi();

	/**
	 * Ritorna tutti gli Stati Pratiche
	 * 
	 * @return la lista degli stai pratiche
	 */
	public List<TypologicalDTO> getStatiPratiche();

	/**
	 * Ritorna tutti i Tipi Processi
	 * 
	 * @return la lista dei tipi processi
	 */
	public List<TypologicalDTO> getTipiProcessi();

	/**
	 * Ritorna tutti i Tipi Allegati filtrandoli per Gruppo, Stato Pratica e Tipo
	 * Processo della pratica
	 * 
	 * @param idUtente
	 * @param idPratica
	 * @return la lista dei tipi allegati
	 */
	public List<TipoAllegatoGruppoStatoProcessoDTO> getTipiAllegati(Long idUtente, Long idPratica);

	/**
	 * Ritorna tutti i Tipi Allegati filtrandoli per Gruppo, Stato Pratica e Tipo
	 * Processo
	 * 
	 * @param idUtente
	 * @param idStatoPratica
	 * @param idTipoProcesso
	 * @return la lista dei tipi allegati
	 */
	public List<TipoAllegatoGruppoStatoProcessoDTO> getTipiAllegatiByStatoAndProcesso(Long idUtente,
			Integer idStatoPratica, Integer idTipoProcesso);

	/**
	 * Ritorna tutti i Tipi Allegati
	 * 
	 * @return la lista dei tipi allegati
	 */
	public List<TipoAllegatoDTO> getAllTipiAllegati();

	/**
	 * Ritorna tutti i Tipi Ruoli Richiedenti
	 * 
	 * @return la lista dei tipi ruoli richiedenti
	 */
	public List<TypologicalDTO> getTipiRuoliRichiedenti();

	/**
	 * Ritorna tutti i Tipi Attività da Svolgere
	 * 
	 * @return la lista dei tipi attività da svolgere
	 */
	public List<TypologicalFlagTestoLiberoDTO> getTipiAttivitaDaSvolgere();

	/**
	 * Ritorna tutti i Tipi Documenti Allegati
	 * 
	 * @return la lista dei tipi documenti allegati
	 */
	public List<TypologicalDTO> getTipiDocumentiAllegati();

	/**
	 * Ritorna tutti le Tipologie Titoli Edilizi
	 * 
	 * @return la lista delle tipologie titoli edilizi
	 */
	public List<TypologicalFlagTestoLiberoDTO> getTipologieTitoliEdilizi();

	/**
	 * Ritorna tutti i Tipi Manufatti
	 * 
	 * @return la lista dei tipi manufatti
	 */
	public List<TypologicalFlagTestoLiberoDTO> getTipiManufatti();

	/**
	 * Ritorna tutti i Tipi Template
	 * 
	 * @return la lista dei tipi template
	 */
	public List<TypologicalDTO> getTipiTemplate();

	/**
	 * Ritorna tutti i Tipi Notifiche Scadenzario
	 * 
	 * @return la lista dei tipi notifiche scadenzario
	 */
	public List<TypologicalDTO> getTipiNotificheScadenzario();
}
