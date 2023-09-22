package it.fincons.osp.controllers;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import it.fincons.osp.payload.request.FlagEsenzioneCupEditRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.FiltriRicercaPraticaDTO;
import it.fincons.osp.dto.PraticaDTO;
import it.fincons.osp.dto.RichiestaIntegrazioneDTO;
import it.fincons.osp.dto.VerificaOccupazioneDTO;
import it.fincons.osp.model.TipoDetermina;
import it.fincons.osp.payload.request.DateOccupazioneEditRequest;
import it.fincons.osp.payload.request.PraticaInsertEditRequest;
import it.fincons.osp.payload.request.PraticaRettificaRequest;
import it.fincons.osp.services.PraticaService;

@RestController
@RequestMapping("/pratica-management")
@SecurityRequirement(name = "jwt")
public class PraticaController {

	@Autowired
	PraticaService praticaService;

	@PostMapping("/pratica")
	@PreAuthorize("hasAnyAuthority('Operatore Sportello', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> insertPratica(@Valid @RequestBody PraticaInsertEditRequest praticaInsertRequest) {

		PraticaDTO result = praticaService.insertPratica(praticaInsertRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PutMapping("/pratica")
	@PreAuthorize("hasAnyAuthority('Operatore Sportello', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<Void> editPratica(@Valid @RequestBody PraticaInsertEditRequest praticaEditRequest) {

		praticaService.editPratica(praticaEditRequest);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/pratica/{id}")
	@PreAuthorize("hasAnyAuthority('Operatore Sportello', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<Void> deletePratica(@PathVariable("id") Long id) {

		praticaService.deletePratica(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/pratica")
	@PreAuthorize("isAuthenticated()")
	@LogEntryExit
	public ResponseEntity<Page<PraticaDTO>> getPratiche(
			@RequestParam(required = false) List<Integer> idsMunicipi,
			@RequestParam(required = false) List<Integer> idsStatiPratica,
			@RequestParam(required = false) String nome,
			@RequestParam(required = false) String cognome,
			@RequestParam(required = false) String denominazioneRagSoc,
			@RequestParam(required = false) String codFiscalePIva,
			@RequestParam(required = false) String numProtocollo,
			@RequestParam(required = false) String numProvvedimento,
			@RequestParam(required = false) Boolean richiestaVerificaRipristinoLuoghi,
			@RequestParam(required = false,name = "tipologiaProcesso") Integer tipoProcesso,
			@RequestParam(required = false) String indirizzo,
			Pageable pageable) {

		// recupero username loggato
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();

		FiltriRicercaPraticaDTO filtriRicerca = new FiltriRicercaPraticaDTO(nome, cognome, denominazioneRagSoc,
				codFiscalePIva, numProtocollo, numProvvedimento, tipoProcesso, indirizzo);

		Page<PraticaDTO> result = praticaService.getPratiche(username, idsMunicipi, idsStatiPratica, filtriRicerca,
				richiestaVerificaRipristinoLuoghi, pageable);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/pratica/count")
	@PreAuthorize("isAuthenticated()")
	@LogEntryExit
	public ResponseEntity<Long> getCountPratiche(@RequestParam(required = true) List<Integer> idsStatiPratica, @RequestParam(required = false) List<Integer> idsMunicipi) {
		// recupero username loggato
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();

		Long result = praticaService.getCountPratiche(username, idsStatiPratica,idsMunicipi);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/pratica/{id}")
	@PreAuthorize("isAuthenticated()")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> getPratica(@PathVariable("id") Long id) {

		PraticaDTO result = praticaService.getPratica(id);
		return ResponseEntity.ok(result);
	}

	@PutMapping("/date-occupazione")
	@PreAuthorize("hasAnyAuthority('Operatore Sportello', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<Void> editDateOccupazione(
			@Valid @RequestBody DateOccupazioneEditRequest dateOccupazioneEditRequest) {

		// recupero username loggato
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();

		praticaService.editDateOccupazione(username, dateOccupazioneEditRequest);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/flag-esenzione-cup")
	@PreAuthorize("hasAnyAuthority('Operatore Sportello', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<Void> editFlagEsenzioneCup(
			@Valid @RequestBody FlagEsenzioneCupEditRequest flagEsenzioneCupEditRequest) {

		// recupero username loggato
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();

		praticaService.editFlagEsenzioneCup(username, flagEsenzioneCupEditRequest);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/pratica/stato-inserita")
	@PreAuthorize("hasAnyAuthority('Operatore Sportello', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> statoInserita(@RequestParam Long idPratica, @RequestParam Long idUtente) {

		PraticaDTO result = praticaService.switchToStatoInserita(idPratica, idUtente, null, null, null);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/pratica/presa-in-carico")
	@PreAuthorize("hasAnyAuthority('Direttore Municipio', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> presaInCarico(@RequestParam Long idPratica, @RequestParam Long idUtenteIstruttore,
			@RequestParam(required = false) Long idUtenteDirettore) {

		PraticaDTO result = praticaService.presaInCarico(idPratica, idUtenteIstruttore, idUtenteDirettore);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/pratica/approvazione")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> approvazione(@RequestParam Long idPratica, @RequestParam Long idUtente) {

		PraticaDTO result = praticaService.approvazione(idPratica, idUtente);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/pratica/rigetto-richiesta")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> rigettoRichiesta(
			@Valid @RequestBody RichiestaIntegrazioneDTO richiestaIntegrazioneRequest) {

		PraticaDTO result = praticaService.rigettoRichiesta(richiestaIntegrazioneRequest);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/pratica/inserimento-determina")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> inserimentoDetermina(@RequestParam Long idPratica, @RequestParam Long idUtente,
			@RequestParam String codiceDetermina,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataEmissioneDetermina) {

		PraticaDTO result = praticaService.inserimentoDetermina(idPratica, idUtente, codiceDetermina,
				dataEmissioneDetermina);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/pratica/pronto-al-rilascio")
	@PreAuthorize("hasAnyAuthority('Operatore Sportello', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> prontoAlRilascio(@RequestParam Long idPratica, @RequestParam Long idUtente) {

		PraticaDTO result = praticaService.switchToStatoProntoAlRilascio(idPratica, idUtente);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/pratica/concessione-valida")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> concessioneValida(@RequestParam Long idPratica, @RequestParam Long idUtente) {

		PraticaDTO result = praticaService.switchToStatoConcessioneValida(idPratica, idUtente);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/pratica/inserimento-determina/rigetto")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> inserimentoDeterminaRigetto(@RequestParam Long idPratica,
			@RequestParam Long idUtente, @RequestParam String codiceDeterminaRigetto,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataEmissioneDetermina) {

		PraticaDTO result = praticaService.inserimentoDeterminaRigetto(idPratica, idUtente, codiceDeterminaRigetto,
				dataEmissioneDetermina);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/pratica/storico")
	@PreAuthorize("isAuthenticated()")
	@LogEntryExit
	public ResponseEntity<Page<PraticaDTO>> getStoricoPratica(@RequestParam Long id, Pageable pageable) {

		Page<PraticaDTO> result = praticaService.getStoricoPratica(id, pageable);
		return ResponseEntity.ok(result);
	}

	@PutMapping("/pratica/archiviazione")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> archiviazione(@RequestParam Long idPratica, @RequestParam Long idUtente) {

		PraticaDTO result = praticaService.archiviazione(idPratica, idUtente);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/pratica/proroga/verifiche-apertura/{idPratica}")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio', 'Operatore Sportello')")
	@LogEntryExit
	public ResponseEntity<Void> prorogaVerificheApertura(@PathVariable("idPratica") Long idPratica) {
		praticaService.prorogaVerificheApertura(idPratica);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/pratica/proroga/pratica-precompilata/{idPratica}")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio', 'Operatore Sportello')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> prorogaPraticaPrecompilata(@PathVariable("idPratica") Long idPratica) {

		PraticaDTO result = praticaService.getPraticaPrecompilataProroga(idPratica);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/pratica/check-sovrapposizione-occupazione")
	@PreAuthorize("hasAnyAuthority('Operatore Sportello', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<Void> checkSovrapposizioneUbicazione(
			@Valid @RequestBody VerificaOccupazioneDTO verificaOccupazione) {

		praticaService.checkSovrapposizioneUbicazione(verificaOccupazione);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping("/pratica/verifica-occupazione")
	@PreAuthorize("hasAnyAuthority('Operatore Sportello', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> verificaOccupazione(
			@Valid @RequestBody VerificaOccupazioneDTO verificaOccupazione) {

		PraticaDTO result = praticaService.verificaOccupazione(verificaOccupazione);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/pratica/rinuncia/pratica-precompilata/{idPratica}")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio', 'Operatore Sportello')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> rinunciaPraticaPrecompilata(@PathVariable("idPratica") Long idPratica) {

		PraticaDTO result = praticaService.getPraticaPrecompilataRinuncia(idPratica);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/pratica/rettifica")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> rettificaPratica(@Valid @RequestBody PraticaRettificaRequest praticaRettificaRequest) {

		PraticaDTO result = praticaService.rettificaPratica(praticaRettificaRequest);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/pratica/inserimento-determina/revoca")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio', 'Direttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> inserimentoDeterminaRevoca(@RequestParam Long idPratica,
			@RequestParam Long idUtente, @RequestParam String notaAlCittadino, @RequestParam String codiceDetermina,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataEmissioneDetermina) {

		PraticaDTO result = praticaService.inserimentoDeterminaRda(idPratica, idUtente, notaAlCittadino,
				codiceDetermina, dataEmissioneDetermina, TipoDetermina.REVOCA);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/pratica/inserimento-determina/decadenza")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio', 'Direttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> inserimentoDeterminaDecadenza(@RequestParam Long idPratica,
			@RequestParam Long idUtente, @RequestParam String notaAlCittadino, @RequestParam String codiceDetermina,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataEmissioneDetermina) {

		PraticaDTO result = praticaService.inserimentoDeterminaRda(idPratica, idUtente, notaAlCittadino,
				codiceDetermina, dataEmissioneDetermina, TipoDetermina.DECADENZA);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/pratica/inserimento-determina/annullamento")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio', 'Direttore Municipio')")
	@LogEntryExit
	public ResponseEntity<PraticaDTO> inserimentoDeterminaAnnullamento(@RequestParam Long idPratica,
			@RequestParam Long idUtente, @RequestParam String notaAlCittadino, @RequestParam String codiceDetermina,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataEmissioneDetermina) {

		PraticaDTO result = praticaService.inserimentoDeterminaRda(idPratica, idUtente, notaAlCittadino,
				codiceDetermina, dataEmissioneDetermina, TipoDetermina.ANNULLAMENTO);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
