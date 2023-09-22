package it.fincons.osp.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.NotificaScadenzarioDTO;
import it.fincons.osp.services.NotificaScadenzarioService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/notifica-scadenzario-management")
public class NotificaScadenzarioController {

	@Autowired
	NotificaScadenzarioService notifiScadenzarioService;

	@Value("${osp.app.scadenzario.scheduler.restrict}")
	private boolean restrictScheduler;
	@Value("${osp.app.scadenzario.scheduler.host}")
	private String restrictHost;

	@PostMapping("/inserimento")
	@LogEntryExit
	public ResponseEntity<Void> inserimentoNotifiche(HttpServletRequest request) {
		log.info("RemoteAddr: <" + request.getRemoteAddr() + ">");
		log.info("restictScheduler: " + restrictScheduler + " restrictHost <" + restrictHost + ">");
		if (restrictScheduler && !request.getRemoteAddr().equals(restrictHost)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); 
		}

		notifiScadenzarioService.inserimentoNotifiche();
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "jwt")
	@GetMapping("/notifica-scadenzario")
	@LogEntryExit
	public ResponseEntity<List<NotificaScadenzarioDTO>> getNotificheScadenzario(@RequestParam Long idUtente) {

		List<NotificaScadenzarioDTO> result = notifiScadenzarioService.getNotificheScadenzario(idUtente);
		return ResponseEntity.ok(result);
	}
	
	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "jwt")
	@GetMapping("/notifica-scadenzario/count")
	@LogEntryExit
	public ResponseEntity<Long> countNotificheScadenzario(@RequestParam Long idUtente) {

		long result = notifiScadenzarioService.countNotificheScadenzario(idUtente);
		return ResponseEntity.ok(result);
	}
}
