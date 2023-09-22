package it.fincons.osp.services.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.Protocollo;
import it.fincons.osp.model.StatoPratica;
import it.fincons.osp.model.TipoOperazioneProtocollo;
import it.fincons.osp.payload.protocollazione.response.ProtocolloResponse;
import it.fincons.osp.repository.ProtocolloRepository;
import it.fincons.osp.repository.StatoPraticaRepository;
import it.fincons.osp.services.ProtocolloService;
import it.fincons.osp.utils.Constants;

@Service
public class ProtocolloServiceImpl implements ProtocolloService {

	@Autowired
	ProtocolloRepository protocolloRepository;

	@Autowired
	StatoPraticaRepository statoPraticaRepository;

	@Override
	@LogEntryExit
	public Protocollo insertProtocollo(Pratica pratica, StatoPratica statoPratica,
		ProtocolloResponse protocolloResponse, TipoOperazioneProtocollo tipoOperazione) {

		Protocollo protocollo = new Protocollo();
		protocollo.setPratica(pratica);
		protocollo.setStatoPratica(statoPratica);
		protocollo.setCodiceProtocollo(protocolloResponse.getNumeroProtocollo() + "|" + protocolloResponse.getAnno());
		protocollo.setNumeroProtocollo(protocolloResponse.getNumeroProtocollo());
		protocollo.setAnno(protocolloResponse.getAnno());
		protocollo.setDataProtocollo(protocolloResponse.getDataProtocollo());
		protocollo.setTipoOperazione(tipoOperazione);
		protocolloRepository.save(protocollo);

		return protocollo;
	}

	@Override
	@LogEntryExit
	public Protocollo getProtocollo(Pratica pratica, StatoPratica statoPratica) {
		Optional<Protocollo> protocolloTrovato = protocolloRepository.findByPraticaAndStatoPratica(pratica,
				statoPratica);
		return protocolloTrovato.isPresent() ? protocolloTrovato.get() : null;
	}

	@Override
	@LogEntryExit
	public Protocollo getProtocolloInserimento(Pratica pratica) {
		StatoPratica statoPraticaInserimento = statoPraticaRepository.findById(Constants.ID_STATO_PRATICA_INSERITA)
				.orElseThrow(() -> new RuntimeException("Errore: stato pratica INSERITA non trovato"));
		Optional<Protocollo> protocolloTrovato = protocolloRepository.findByPraticaAndStatoPratica(pratica,
				statoPraticaInserimento);
		return protocolloTrovato.isPresent() ? protocolloTrovato.get() : null;
	}

	@Override
	@LogEntryExit
	public Protocollo getProtocolloByCodice(String codiceProtocollo) {
		Optional<Protocollo> protocolloTrovato = protocolloRepository.findByCodiceProtocollo(codiceProtocollo);
		return protocolloTrovato.isPresent() ? protocolloTrovato.get() : null;
	}

	@Override
	public Protocollo getProtocolloByNumeroProtcollo(String numeroProtocollo) {
		Optional<Protocollo> protocolloTrovato = protocolloRepository.findByNumeroProtocollo(numeroProtocollo);
		return protocolloTrovato.isPresent() ? protocolloTrovato.get() : null;
	}

	@Override
	public List<Protocollo> getProtocolliByNumeroProtocollo(String numeroProtocollo) {
		return protocolloRepository.findProtocolliByNumeroProtocollo(
			numeroProtocollo != null &&  !"".equals(numeroProtocollo) ?
				"%"+numeroProtocollo+"%" :
				null
		);
	}

	@Override
	@LogEntryExit
	public void insertDeterminaRettifica(Protocollo protocollo, String codiceDeterminaRettifica,
			LocalDate dataEmissioneDeterminaRettifica) {

		protocollo.setCodiceDeterminaRettifica(codiceDeterminaRettifica);
		protocollo.setDataEmissioneDeterminaRettifica(dataEmissioneDeterminaRettifica);
		protocolloRepository.save(protocollo);
	}

}
