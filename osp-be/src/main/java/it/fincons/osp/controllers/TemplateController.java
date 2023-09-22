package it.fincons.osp.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import it.fincons.osp.dto.TemplateDTO;
import it.fincons.osp.dto.TemplateSimplifiedDTO;
import it.fincons.osp.services.TemplateService;

@RestController
@RequestMapping("/template-management")
@SecurityRequirement(name = "jwt")
public class TemplateController {

	@Autowired
	TemplateService templateService;

	@PostMapping("/template")
	@LogEntryExit
	@PreAuthorize("hasAnyAuthority('Admin')")
	public ResponseEntity<TemplateDTO> insertTemplate(@Valid @RequestBody TemplateDTO templateRequest) {

		TemplateDTO result = templateService.insertTemplate(templateRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@PutMapping("/template")
	@LogEntryExit
	@PreAuthorize("hasAnyAuthority('Admin')")
	public ResponseEntity<Void> updateTemplate(@Valid @RequestBody TemplateDTO templateRequest) {

		templateService.updateTemplate(templateRequest);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/template/tipo")
	@LogEntryExit
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<TemplateDTO> getTemplateByTipo(@RequestParam Integer idTipoTemplate) {

		TemplateDTO result = templateService.getTemplateByTipo(idTipoTemplate);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/template/{id}")
	@PreAuthorize("isAuthenticated()")
	@LogEntryExit
	public ResponseEntity<TemplateDTO> getTemplate(@PathVariable("id") Integer id) {

		TemplateDTO result = templateService.getTemplate(id);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/template/elaborato")
	@LogEntryExit
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<TemplateDTO> getTemplateElaborato(@RequestParam Long idPratica,
			@RequestParam Integer idTipoTemplate, @RequestParam(required = false) String notaParere) {

		TemplateDTO result = templateService.getTemplateElaborato(idPratica, idTipoTemplate, notaParere);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/template")
	@PreAuthorize("isAuthenticated()")
	@LogEntryExit
	public ResponseEntity<List<TemplateSimplifiedDTO>> getTemplate() {

		List<TemplateSimplifiedDTO> result = templateService.getAllTemplates();
		return ResponseEntity.ok(result);
	}

}
