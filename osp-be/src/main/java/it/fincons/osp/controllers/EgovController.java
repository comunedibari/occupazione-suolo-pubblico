package it.fincons.osp.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.*;
import it.fincons.osp.model.Protocollo;
import it.fincons.osp.payload.request.*;
import it.fincons.osp.services.AllegatoService;
import it.fincons.osp.services.IntegrazioneService;
import it.fincons.osp.services.PraticaService;
import it.fincons.osp.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Pattern;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/egov-management")
@SecurityRequirement(name = "jwt")
@PreAuthorize("hasAnyAuthority('EGOV')")
public class EgovController {

	@Value("${flagControlloAllegati}")
	private boolean flagControlloAllegati;

	@Value("${maxDimAllegatiProtocolloKb}")
	private double maxDimAllegatiProtocolloKb;

	@Autowired
	PraticaService praticaService;

	@Autowired
	AllegatoService allegatoService;

	@Autowired
	IntegrazioneService integrazioneService;

	@PostMapping("/pratica/inserimento-completo")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> insertPraticaCompleta(
			@Valid @RequestBody PraticaEgovInsertRequest praticaEgovInsertRequest) {

		if(
				StringUtils.isBlank(praticaEgovInsertRequest.getPratica().getNumeroProtocollo())
						|| StringUtils.isBlank(praticaEgovInsertRequest.getPratica().getAnno())
						|| praticaEgovInsertRequest.getPratica().getDataProtocollo()==null
		){
			throw new ValidationException("Errore: è obbligatorio fornire Numero Protocollo, Anno, Data Protocollo");
		}

		PraticaDTO result = praticaService.insertPraticaCompletaEgov(praticaEgovInsertRequest);

		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PutMapping("/pratica/aggiornamento-pagamento")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> aggiornamentoPagamento(@Valid @RequestBody PraticaEgovEditRequest praticaEgovEditRequest) {
		if(
				StringUtils.isBlank(praticaEgovEditRequest.getPratica().getNumeroProtocollo())
						|| StringUtils.isBlank(praticaEgovEditRequest.getPratica().getAnno())
						|| praticaEgovEditRequest.getPratica().getDataProtocollo()==null
		){
			throw new ValidationException("Errore: è obbligatorio fornire Numero Protocollo, Anno, Data Protocollo");
		}

		PraticaDTO result = praticaService.editPraticaEgov(praticaEgovEditRequest);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	@PostMapping("/integrazione/inserimento-completo")
	@LogEntryExit
	public ResponseEntity<IntegrazioneDTO> insertIntegrazioneCompleta(
			@Valid @RequestBody IntegrazioneCompletaEgovInsertRequest integrazioneCompletaEgovInsertRequest) {

		if(
				StringUtils.isBlank(integrazioneCompletaEgovInsertRequest.getNumeroProtocollo())
						|| StringUtils.isBlank(integrazioneCompletaEgovInsertRequest.getAnno())
						|| integrazioneCompletaEgovInsertRequest.getDataProtocollo()==null
		){
			throw new ValidationException("Errore: è obbligatorio fornire Numero Protocollo, Anno, Data Protocollo");
		}

		IntegrazioneDTO result = integrazioneService.insertIntegrazioneCompletaEgov(integrazioneCompletaEgovInsertRequest);

		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PutMapping("/date-occupazione")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> editDateOccupazione(@Valid @RequestBody DateOccupazioneEgovEditRequest dateOccupazioneEgovEditRequest) {

		PraticaDTO result = praticaService.editDateOccupazioneEgov(dateOccupazioneEgovEditRequest);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/pratica")
	@LogEntryExit
	public ResponseEntity<List<PraticaDTO>> getPratiche(
		@RequestParam String codiceFiscale,
		@RequestParam(required = false) String codiceProtocollo,
		@RequestParam(required = false) Long idPratica
	) {
		List<PraticaDTO> result = praticaService.getPraticheEgov(codiceFiscale, codiceProtocollo, idPratica);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/pratica/proroga/inserimento-completo")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> insertProrogaPraticaCompleta(
			@Valid @RequestBody ProrogaEgovInsertRequest prorogaEgovInsertRequest) {

		if(
				StringUtils.isBlank(prorogaEgovInsertRequest.getPratica().getNumeroProtocollo())
						|| StringUtils.isBlank(prorogaEgovInsertRequest.getPratica().getAnno())
						|| prorogaEgovInsertRequest.getPratica().getDataProtocollo()==null
		){
			throw new ValidationException("Errore: è obbligatorio fornire Numero Protocollo, Anno, Data Protocollo");
		}
		PraticaDTO result = praticaService.insertProrogaCompletaEgov(prorogaEgovInsertRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PostMapping("/pratica/rinuncia/inserimento-completo")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> insertRinunciaPraticaCompleta(
			@Valid @RequestBody RinunciaEgovInsertRequest rinunciaEgovInsertRequest) {
		if(
				StringUtils.isBlank(rinunciaEgovInsertRequest.getPratica().getNumeroProtocollo())
						|| StringUtils.isBlank(rinunciaEgovInsertRequest.getPratica().getAnno())
						|| rinunciaEgovInsertRequest.getPratica().getDataProtocollo()==null
		){
			throw new ValidationException("Errore: è obbligatorio fornire Numero Protocollo, Anno, Data Protocollo");
		}

		PraticaDTO result = praticaService.insertRinunciaCompletaEgov(rinunciaEgovInsertRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PostMapping("/rettifica-date")
	@LogEntryExit
	public ResponseEntity<IntegrazioneDTO> insertRettificaDate(
			@Valid @RequestBody RettificaDateEgovInsertRequest rettificaDateEgovInsertRequest) {
		if(
				StringUtils.isBlank(rettificaDateEgovInsertRequest.getNumeroProtocollo())
						|| StringUtils.isBlank(rettificaDateEgovInsertRequest.getAnno())
						|| rettificaDateEgovInsertRequest.getDataProtocollo()==null
		){
			throw new ValidationException("Errore: è obbligatorio fornire Numero Protocollo, Anno, Data Protocollo");
		}

		IntegrazioneDTO result = integrazioneService.insertRettificaDateEgov(rettificaDateEgovInsertRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@GetMapping("/pratica/rettificabili")
	@LogEntryExit
	public ResponseEntity<?> getPraticheRettificabili(@RequestParam(required = true) String codiceFiscalePartitaIva) {
		List<PraticaDTO> result = praticaService.searchPraticheRettificabili(codiceFiscalePartitaIva);

		if(result==null||result.size()==0){
			return new ResponseEntity<>("Nessun elemento trovato.", HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(result);
	}

	/**
	 *
	 * idDetermina
	 *
	 * @param codiceFiscalePartitaIva
	 * @param idTipoProcesso
	 * @return
	 */
	@GetMapping("/pratica/avviabili-post-concessione")
	@LogEntryExit
	public ResponseEntity<?> getPraticheAvviabiliPostConcessione(@RequestParam(required = true) String codiceFiscalePartitaIva, @RequestParam(required = true) Integer idTipoProcesso) {
		List<PraticaDTO> result = praticaService.searchPraticheAvviabiliPostConcessioone(codiceFiscalePartitaIva, idTipoProcesso);

		if(result==null||result.size()==0){
			return new ResponseEntity<>("Nessun elemento trovato.", HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(result);
	}

	/**
	 *
	 * @param praticaEgovValidazioneRequest
	 * @return
	 */
	@PostMapping("/pratica/validazione-pratica")
	@LogEntryExit
	public ResponseEntity<String> validazionePratica(@RequestBody PraticaEgovValidazioneRequest praticaEgovValidazioneRequest) {

		if(		flagControlloAllegati
				&& praticaEgovValidazioneRequest.getDimensioneTotaleAllegatiProtocolloKb()!=null &&
				!"".equals(praticaEgovValidazioneRequest.getDimensioneTotaleAllegatiProtocolloKb())
				&& Double.valueOf(praticaEgovValidazioneRequest.getDimensioneTotaleAllegatiProtocolloKb()) > maxDimAllegatiProtocolloKb){
			return new ResponseEntity<>(String.format("Dimensione allegati maggiore a quella consentita [MAX: %s kb]", maxDimAllegatiProtocolloKb), HttpStatus.PAYLOAD_TOO_LARGE);
		}

		return new ResponseEntity<>("Validazione avvenuta con successo", HttpStatus.OK);
	}

	/**
	 *
	 * @param codiceFiscalePiva
	 * @return
	 */
	@GetMapping("/pratica/attesa-pagamento")
	@LogEntryExit
	public ResponseEntity<?> getPraticheAttesaPagamentoBollo(@RequestParam(required = true) String codiceFiscalePiva) {
		List<PraticaDTO> result = praticaService.getPraticheEgovAttesaPagamentoBollo(codiceFiscalePiva);

		if(result==null||result.size()==0){
			return new ResponseEntity<>("Nessun elemento trovato.", HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(result);
	}

	/**
	 *
	 * @param codiceFiscalePiva
	 * @return
	 */
	@GetMapping("/pratica/rettifica-date")
	@LogEntryExit
	public ResponseEntity<?> getPraticheRettificaDate(@RequestParam(required = true) String codiceFiscalePiva) {
		List<PraticaDTO> result = praticaService.getPraticheEgovRettificaDate(codiceFiscalePiva);

		if(result==null||result.size()==0){
			return new ResponseEntity<>("Nessun elemento trovato.", HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(result);
	}

	/**
	 *
	 * @param idPratica
	 * @return
	 */
	@GetMapping(value = "/pratica/{idPratica}/determina")
	@LogEntryExit
	public ResponseEntity<DeterminaDTO> getDetermina(@PathVariable("idPratica") String idPratica) {

		DeterminaDTO result = praticaService.getDeterminaInAttesaPagamento(Long.valueOf(idPratica));

		if(result==null){
			return new ResponseEntity<DeterminaDTO>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<DeterminaDTO>(result, HttpStatus.OK);
	}

}
