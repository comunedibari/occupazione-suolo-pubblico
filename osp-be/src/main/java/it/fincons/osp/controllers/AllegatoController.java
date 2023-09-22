package it.fincons.osp.controllers;

import java.util.List;

import javax.validation.Valid;

import it.fincons.osp.payload.request.AllegatoRichiestaIntegrazioneRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.AllegatoDTO;
import it.fincons.osp.dto.AllegatoSimplifiedDTO;
import it.fincons.osp.payload.request.AllegatoIntegrazioneInsertRequest;
import it.fincons.osp.payload.request.AllegatoParereInsertRequest;
import it.fincons.osp.payload.request.AllegatoPraticaInsertRequest;
import it.fincons.osp.services.AllegatoService;

@RestController
@RequestMapping("/allegato-management")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "jwt")
public class AllegatoController {

	@Autowired
	AllegatoService allegatoService;

	@PostMapping("/allegato/pratica")
	@LogEntryExit
	public ResponseEntity<AllegatoDTO> insertAllegatoPratica(
			@Valid @RequestBody AllegatoPraticaInsertRequest allegatoPraticaInsertRequest) {

		AllegatoDTO result = allegatoService.insertAllegatoPratica(allegatoPraticaInsertRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PostMapping("/allegato/parere")
	@LogEntryExit
	public ResponseEntity<AllegatoDTO> insertAllegatoParere(
			@Valid @RequestBody AllegatoParereInsertRequest allegatoParereInsertRequest) {
		
		// recupero username utente loggato
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();

		AllegatoDTO result = allegatoService.insertAllegatoParere(allegatoParereInsertRequest, username);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PostMapping("/allegato/richiesta-integrazione")
	@LogEntryExit
	public ResponseEntity<AllegatoDTO> insertAllegatoRichiestaIntegrazione(
			@Valid @RequestBody AllegatoRichiestaIntegrazioneRequest allegatoRichiestaIntegrazioneRequest) {

		AllegatoDTO result = allegatoService.insertAllegatoRichiestaIntegrazione(allegatoRichiestaIntegrazioneRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PostMapping("/allegato/integrazione")
	@LogEntryExit
	public ResponseEntity<AllegatoDTO> insertAllegatoIntegrazione(
			@Valid @RequestBody AllegatoIntegrazioneInsertRequest allegatoIntegrazioneInsertRequest) {

		AllegatoDTO result = allegatoService.insertAllegatoIntegrazione(allegatoIntegrazioneInsertRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@GetMapping("/allegato")
	@LogEntryExit
	public ResponseEntity<List<AllegatoSimplifiedDTO>> getAllegati(@RequestParam Long idPratica,
			@RequestParam(required = false) Integer idStatoPratica,
			@RequestParam(required = false) Integer idTipoProcesso) {

		List<AllegatoSimplifiedDTO> result = allegatoService.getAllegati(idPratica, idStatoPratica, idTipoProcesso);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/allegato/richiesta-parere")
	@LogEntryExit
	public ResponseEntity<List<AllegatoSimplifiedDTO>> getAllegatiRichiestaParere(
			@RequestParam Long idRichiestaParere) {

		List<AllegatoSimplifiedDTO> result = allegatoService.getAllegatiRichiestaParere(idRichiestaParere);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/allegato/parere")
	@LogEntryExit
	public ResponseEntity<List<AllegatoSimplifiedDTO>> getAllegatiParere(@RequestParam Long idParere) {

		List<AllegatoSimplifiedDTO> result = allegatoService.getAllegatiParere(idParere);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/allegato/richiesta-integrazione")
	@LogEntryExit
	public ResponseEntity<List<AllegatoSimplifiedDTO>> getAllegatiRichiestaIntegrazione(
			@RequestParam Long idRichiestaintegrazione) {

		List<AllegatoSimplifiedDTO> result = allegatoService.getAllegatiRichiestaIntegrazione(idRichiestaintegrazione);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/allegato/{id}")
	@LogEntryExit
	public ResponseEntity<AllegatoDTO> getAllegato(@PathVariable("id") Long id) {

		AllegatoDTO result = allegatoService.getAllegato(id);
		return ResponseEntity.ok(result);
	}

	@DeleteMapping("/allegato/{id}")
	@LogEntryExit
	public ResponseEntity<Void> deleteAllegato(@PathVariable("id") Long id) {

		allegatoService.deleteAllegato(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/allegato/documentale-pratica")
	@LogEntryExit
	public ResponseEntity<List<AllegatoSimplifiedDTO>> getAllegatiDocumentalePratica(@RequestParam Long idPratica) {

		List<AllegatoSimplifiedDTO> result = allegatoService.getAllegatiDocumentalePratica(idPratica);
		return ResponseEntity.ok(result);
	}

}
