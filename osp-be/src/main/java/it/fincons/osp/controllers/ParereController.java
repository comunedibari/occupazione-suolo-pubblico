package it.fincons.osp.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.ParereDTO;
import it.fincons.osp.payload.request.ParereInsertRequest;
import it.fincons.osp.services.ParereService;

@RestController
@RequestMapping("/parere-management")
@SecurityRequirement(name = "jwt")
public class ParereController {

	@Autowired
	ParereService parereService;

	@PostMapping("/parere")
	@PreAuthorize("hasAnyAuthority('Polizia Locale', 'IVOOPP - Settore Urbanizzazioni Primarie', 'IVOOPP - Settore Giardini', 'IVOOPP - Settore Interventi sul Territorio', 'IVOOPP - Settore Infrastrutture a Rete', 'Ripartizione Patrimonio', 'Ripartizione Urbanistica')")
	@LogEntryExit
	public ResponseEntity<ParereDTO> insertParere(
			@Valid @RequestBody ParereInsertRequest parereInsertRequest,
			@RequestParam() boolean isRipristinoLuoghi
	) {

		ParereDTO result = parereService.insertParere(parereInsertRequest, isRipristinoLuoghi);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@GetMapping("/parere/{id}")
	@PreAuthorize("isAuthenticated()")
	@LogEntryExit
	public ResponseEntity<ParereDTO> getParere(@PathVariable("id") Long id) {

		ParereDTO result = parereService.getParere(id);
		return ResponseEntity.ok(result);
	}
}
