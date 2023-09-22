package it.fincons.osp.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.IntegrazioneDTO;
import it.fincons.osp.services.IntegrazioneService;

@RestController
@RequestMapping("/integrazione-management")
@SecurityRequirement(name = "jwt")
public class IntegrazioneController {

	@Autowired
	IntegrazioneService integrazioneService;

	@PostMapping("/integrazione")
	@PreAuthorize("hasAnyAuthority('Operatore Sportello', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<IntegrazioneDTO> insertIntegrazione(@Valid @RequestBody IntegrazioneDTO integrazioneRequest) {

		IntegrazioneDTO result = integrazioneService.insertIntegrazione(integrazioneRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@GetMapping("/integrazione/{id}")
	@PreAuthorize("isAuthenticated()")
	@LogEntryExit
	public ResponseEntity<IntegrazioneDTO> getIntegrazione(@PathVariable("id") Long id) {

		IntegrazioneDTO result = integrazioneService.getIntegrazione(id);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/rettifica-date")
	@PreAuthorize("hasAnyAuthority('Operatore Sportello', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<IntegrazioneDTO> insertRettificaDate(
			@Valid @RequestBody IntegrazioneDTO integrazioneRequest) {

		IntegrazioneDTO result = integrazioneService.insertRettificaDate(integrazioneRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

}
