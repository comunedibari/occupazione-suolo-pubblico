package it.fincons.osp.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.RichiestaIntegrazioneDTO;
import it.fincons.osp.services.RichiestaIntegrazioneService;

@RestController
@RequestMapping("/richiesta-integrazione-management")
@SecurityRequirement(name = "jwt")
public class RichiestaIntegrazioneController {

	@Autowired
	RichiestaIntegrazioneService richiestaIntegrazioneService;

	@PostMapping("/richiesta-integrazione")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<RichiestaIntegrazioneDTO> insertRichiestaIntegrazione(
			@Valid @RequestBody RichiestaIntegrazioneDTO richiestaIntegrazioneRequest) {

		RichiestaIntegrazioneDTO result = richiestaIntegrazioneService.insertRichiestaIntegrazione(richiestaIntegrazioneRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PutMapping("/richiesta-rettifica-date")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<RichiestaIntegrazioneDTO> richiestaRettificaDate(@RequestParam Long idPratica,
			@RequestParam Long idUtente, @RequestParam(required = false) String notaAlCittadino) {

		RichiestaIntegrazioneDTO result = richiestaIntegrazioneService.insertRichiestaRettificaDate(idPratica,
				idUtente, notaAlCittadino);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
