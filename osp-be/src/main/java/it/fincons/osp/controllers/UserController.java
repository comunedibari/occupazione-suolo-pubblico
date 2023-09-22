package it.fincons.osp.controllers;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import it.fincons.osp.dto.UtenteDTO;
import it.fincons.osp.payload.request.ModificaPasswordRequest;
import it.fincons.osp.payload.request.UtenteEditRequest;
import it.fincons.osp.payload.request.UtenteInsertRequest;
import it.fincons.osp.services.UtenteService;

@RestController
@RequestMapping("/user-management")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "jwt")
public class UserController {

	@Autowired
	UtenteService utenteService;

	@GetMapping("/user")
	@LogEntryExit
	public ResponseEntity<Page<UtenteDTO>> getUsers(@RequestParam(required = false) Long id,
			@Size(min = 3, max = 30) @RequestParam(required = false) String username,
			@Size(max = 50) @RequestParam(required = false) String nome,
			@Size(max = 50) @RequestParam(required = false) String cognome, Pageable pageable) {

		Page<UtenteDTO> result = utenteService.getUtenti(id, username, nome, cognome, pageable);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/user/gruppo-municipio")
	@LogEntryExit
	public ResponseEntity<List<UtenteDTO>> getUsersGruppoMunicipio(@RequestParam Integer idGruppo,
			@RequestParam Integer idMunicipio) {

		List<UtenteDTO> result = utenteService.getUtentiGruppoMunicipio(idGruppo, idMunicipio);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/user/gruppo")
	@LogEntryExit
	public ResponseEntity<List<UtenteDTO>> getUsersGruppo(@RequestParam Integer idGruppo) {

		List<UtenteDTO> result = utenteService.getUtentiGruppo(idGruppo);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/user/{id}")
	@LogEntryExit
	public ResponseEntity<UtenteDTO> getUser(@PathVariable("id") Long id) {

		UtenteDTO result = utenteService.getUtente(id);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/user")
	@LogEntryExit
	public ResponseEntity<UtenteDTO> inserisciUtente(@Valid @RequestBody UtenteInsertRequest utenteRequest) {

		UtenteDTO result = utenteService.insertUtente(utenteRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PutMapping("/user")
	@LogEntryExit
	public ResponseEntity<Void> modificaDatiUtente(@Valid @RequestBody UtenteEditRequest utenteRequest) {

		utenteService.editUtente(utenteRequest);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/user/password")
	@LogEntryExit
	public ResponseEntity<Void> cambiaPasswordUtente(
			@Valid @RequestBody ModificaPasswordRequest modificaPasswordRequest) {

		utenteService.editPassword(modificaPasswordRequest);
		return ResponseEntity.noContent().build();

	}

	@DeleteMapping("/user/{id}")
	@LogEntryExit
	public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {

		utenteService.deleteUtente(id);
		return ResponseEntity.noContent().build();
	}

}
