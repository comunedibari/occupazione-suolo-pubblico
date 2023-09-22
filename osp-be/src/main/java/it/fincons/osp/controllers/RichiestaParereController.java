package it.fincons.osp.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.RichiestaParereDTO;
import it.fincons.osp.payload.request.UlterioriPareriInsertRequest;
import it.fincons.osp.services.RichiestaParereService;

@RestController
@RequestMapping("/richiesta-parere-management")
@SecurityRequirement(name = "jwt")
public class RichiestaParereController {

	@Autowired
	RichiestaParereService richiestaParereService;

	@PostMapping("/richiesta-parere/verifica-formale")
	@PreAuthorize("hasAnyAuthority('Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<RichiestaParereDTO> insertRichiestaParereFromVerificaFormale(
			@Valid @RequestBody RichiestaParereDTO richiestaParereRequest) {

		RichiestaParereDTO result = richiestaParereService
				.insertRichiestaParereFromVerificaFormale(richiestaParereRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PostMapping("/richiesta-parere/ulteriori-pareri")
	@PreAuthorize("hasAnyAuthority('Polizia Locale', 'Istruttore Municipio')")
	@LogEntryExit
	public ResponseEntity<List<RichiestaParereDTO>> insertUlterioriRichiestePareri(
			@Valid @RequestBody UlterioriPareriInsertRequest ulterioriPareriRequest) {

		// recupero username loggato
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();

		List<RichiestaParereDTO> result = richiestaParereService.insertUlterioriRichiestePareri(ulterioriPareriRequest,
				username);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}
}
