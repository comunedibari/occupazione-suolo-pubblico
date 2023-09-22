package it.fincons.osp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.exceptions.CiviliarioServiceException;
import it.fincons.osp.payload.civiliario.response.DataSingoloMunicipioResponse;
import it.fincons.osp.services.CiviliarioService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/civiliario")
@SecurityRequirement(name = "jwt")
@PreAuthorize("isAuthenticated()")
@Slf4j
public class CiviliarioController {

	@Autowired
	CiviliarioService civiliarioService;

	@GetMapping("/data-singolo-municipio")
	@LogEntryExit
	public ResponseEntity<DataSingoloMunicipioResponse> dataSingoloMunicipio(@RequestParam String indirizzo,
			@RequestParam(required = false) String numero, @RequestParam(required = false) String idMunicipio) {

		try {
			DataSingoloMunicipioResponse result = civiliarioService.getDataSingoloMunicipio(indirizzo, numero,
					idMunicipio);
			return ResponseEntity.ok(result);
		} catch (CiviliarioServiceException e) {
			log.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
