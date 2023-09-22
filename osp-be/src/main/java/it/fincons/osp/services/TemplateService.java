package it.fincons.osp.services;

import java.util.List;

import it.fincons.osp.dto.TemplateDTO;
import it.fincons.osp.dto.TemplateSimplifiedDTO;

public interface TemplateService {

	/**
	 * Effettua l'inserimento di un oggetto {@link template}
	 * 
	 * @param templateRequest
	 * @return le informazioni del template inserito
	 */
	public TemplateDTO insertTemplate(TemplateDTO templateRequest);

	/**
	 * Effettua l'update di un file template
	 * 
	 * @param templateDTO
	 * @return le informazioni del template aggiornato
	 */
	public TemplateDTO updateTemplate(TemplateDTO templateRequest);

	/**
	 * Ricerca un template di uno specifico tipo
	 * 
	 * @param idTipoTemplate
	 * @return le informazioni del template
	 */
	public TemplateDTO getTemplateByTipo(Integer idTipoTemplate);

	/**
	 * Ricerca un template per id
	 * 
	 * @param id
	 * @return le informazioni del template
	 */
	public TemplateDTO getTemplate(Integer id);

	/**
	 * Ricerca un template di uno specifico tipo e lo elabora con le informazioni
	 * della pratica
	 * 
	 * @param idPratica
	 * @param idTipoTemplate
	 * @param notaParere
	 * @return le informazione del template
	 */
	public TemplateDTO getTemplateElaborato(Long idPratica, Integer idTipoTemplate, String notaParere);

	/**
	 * Ricerca e ritorna la lista di tutti i template
	 * 
	 * @return la lista dei template
	 */
	public List<TemplateSimplifiedDTO> getAllTemplates();

}
