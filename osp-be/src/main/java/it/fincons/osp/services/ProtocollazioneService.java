package it.fincons.osp.services;

import java.util.List;

import it.fincons.osp.model.Allegato;
import it.fincons.osp.model.PassaggioStato;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.Utente;
import it.fincons.osp.payload.protocollazione.response.ProtocolloResponse;

public interface ProtocollazioneService {

	/**
	 * Effettua una chiamata ai servizi di protocollazione in entrata per
	 * protocollare l'inserimento della pratica e le iterazioni del cittadino ed
	 * ottenere il numero di protocollo
	 * 
	 * @param pratica
	 * @param utente
	 * @param evento
	 * @return le informazioni del protocollo
	 */
	@Deprecated
	public ProtocolloResponse getNumeroProtocolloEntrata(Pratica pratica, Utente utente, String evento);

	/**
	 * Effettua una chiamata ai servizi di protocollazione in uscita per
	 * protocollare le operazioni sulla pratica ed ottenere il numero di protocollo
	 * 
	 * @param pratica
	 * @param utente
	 * @param evento
	 * @param destinatari
	 * @param comunicazioneCittadino
	 * @return le informazioni del protocollo
	 */
	@Deprecated
	public ProtocolloResponse getNumeroProtocolloUscita(Pratica pratica, Utente utente, String evento, List<Utente> destinatari, boolean comunicazioneCittadino);

	/**
	 * Effettua una chiamata ai servizi di protocollazione in uscita per
	 * protocollare le operazioni sulla pratica ed ottenere il numero di protocollo
	 *
	 * @param pratica
	 * @param utente
	 * @param evento
	 * @param destinatari
	 * @param comunicazioneCittadino
	 * @return le informazioni del protocollo
	 */
	public ProtocolloResponse getNumeroProtocolloUscita(Pratica pratica, Utente utente, String evento, List<Utente> destinatari, boolean comunicazioneCittadino, String templateName, List<Allegato> allegati, PassaggioStato passaggioStato, String infoPassaggioStato);


	/**
	 * Effettua una chiamata ai servizi di protocollazione in uscita per
	 * protocollare le operazioni sulla pratica ed ottenere il numero di protocollo
	 *
	 * @param pratica
	 * @param utente
	 * @param evento
	 * @param destinatari
	 * @param comunicazioneCittadino
	 * @return le informazioni del protocollo
	 */
	public ProtocolloResponse getNumeroProtocolloUscitaV2(Pratica pratica, Utente utente, String evento, List<Utente> destinatari, boolean comunicazioneCittadino, byte[] documentoPrincipale, String docPrincipaleExtension, List<Allegato> allegati);



	/**
	 * Effettua una chiamata ai servizi di protocollazione in entrata per
	 * protocollare l'inserimento della pratica e le iterazioni del cittadino ed
	 * ottenere il numero di protocollo
	 *
	 * @param pratica
	 * @param utente
	 * @param evento
	 * @return le informazioni del protocollo
	 */
	public ProtocolloResponse getNumeroProtocolloEntrata(Pratica pratica, Utente utente, String evento, String templateName, List<Allegato> allegati, PassaggioStato passaggioStato);
}
