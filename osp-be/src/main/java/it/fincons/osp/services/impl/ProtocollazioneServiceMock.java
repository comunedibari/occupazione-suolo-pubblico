package it.fincons.osp.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.fincons.osp.dto.AutomiProtocolloDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.model.*;
import it.fincons.osp.payload.protocollazione.request.*;
import it.fincons.osp.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import it.fincons.osp.payload.protocollazione.response.ProtocolloResponse;
import it.fincons.osp.services.ProtocollazioneService;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "osp.app.protocollo.api", name = "mocked", havingValue = "true")
public class ProtocollazioneServiceMock implements ProtocollazioneService {

	@Override
	public ProtocolloResponse getNumeroProtocolloEntrata(Pratica pratica, Utente utente, String evento) {
		var result = "";
		var characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		var charactersLength = characters.length();
		for (var i = 0; i < 16; i++) {
			result += characters.charAt((int) Math.floor(Math.random() * charactersLength));
		}

		ProtocolloResponse protocolloResponse = new ProtocolloResponse();
		protocolloResponse.setNumeroProtocollo(result);
		protocolloResponse.setAnno(LocalDate.now().getYear() + "");
		protocolloResponse.setDataProtocollo(LocalDateTime.now());

		return protocolloResponse;
	}

	@Override
	public ProtocolloResponse getNumeroProtocolloUscita(Pratica pratica, Utente utente, String evento,
			List<Utente> destinatari, boolean comunicazioneCittadino) {
		var result = "";
		var characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		var charactersLength = characters.length();
		for (var i = 0; i < 16; i++) {
			result += characters.charAt((int) Math.floor(Math.random() * charactersLength));
		}

		ProtocolloResponse protocolloResponse = new ProtocolloResponse();
		protocolloResponse.setNumeroProtocollo(result);
		protocolloResponse.setAnno(LocalDate.now().getYear() + "");
		protocolloResponse.setDataProtocollo(LocalDateTime.now());

		return protocolloResponse;
	}

	@Override
	public ProtocolloResponse getNumeroProtocolloUscita(Pratica pratica, Utente utente, String evento, List<Utente> destinatari, boolean comunicazioneCittadino, String templateName, List<Allegato> allegati, PassaggioStato passaggioStato, String infoPassaggioStato) {
		var result = "";
		var characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		var charactersLength = characters.length();
		for (var i = 0; i < 16; i++) {
			result += characters.charAt((int) Math.floor(Math.random() * charactersLength));
		}

		ProtocolloResponse protocolloResponse = new ProtocolloResponse();
		protocolloResponse.setNumeroProtocollo(result);
		protocolloResponse.setAnno(LocalDate.now().getYear() + "");
		protocolloResponse.setDataProtocollo(LocalDateTime.now());

		return protocolloResponse;
	}

	@Override
	public ProtocolloResponse getNumeroProtocolloUscitaV2(Pratica pratica, Utente utente, String evento, List<Utente> destinatari, boolean comunicazioneCittadino, byte[] documentoPrincipale, String extensionDocPrincipale, List<Allegato> allegati) {
		var result = "";
		var characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		var charactersLength = characters.length();
		for (var i = 0; i < 16; i++) {
			result += characters.charAt((int) Math.floor(Math.random() * charactersLength));
		}
		List<DestinatarioDTO> dest = this.buildDestinatariUscitaRequest(pratica.getFirmatario(), destinatari, comunicazioneCittadino, utente);

		for(DestinatarioDTO d: dest) {
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
		ProtocolloResponse protocolloResponse = new ProtocolloResponse();
		protocolloResponse.setNumeroProtocollo(result);
		protocolloResponse.setAnno(LocalDate.now().getYear() + "");
		protocolloResponse.setDataProtocollo(LocalDateTime.now());

		return protocolloResponse;
	}

	@Override
	public ProtocolloResponse getNumeroProtocolloEntrata(Pratica pratica, Utente utente, String evento, String nomeTemplate, List<Allegato> allegati, PassaggioStato passaggioStato) {
		var result = "";
		var characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		var charactersLength = characters.length();
		for (var i = 0; i < 16; i++) {
			result += characters.charAt((int) Math.floor(Math.random() * charactersLength));
		}

		ProtocolloResponse protocolloResponse = new ProtocolloResponse();
		protocolloResponse.setNumeroProtocollo(result);
		protocolloResponse.setAnno(LocalDate.now().getYear() + "");
		protocolloResponse.setDataProtocollo(LocalDateTime.now());

		return protocolloResponse;
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
			destinatari = destinatari.stream().filter(u -> u.getUoId()!=null && utente.getUoId()!=null && !u.getUoId().equals(utente.getUoId()))
					.collect(Collectors.toList());
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
							/*AutomiProtocolloDTO automiProtocolloDTO=automiProtocolloService.getAutomaProtocolloDTOByUoid(destinatario.getUoId());

							if(automiProtocolloDTO!=null){
								personaGiuridica.setRagioneSociale(automiProtocolloDTO.getDenominazione());
							}*/
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
}
