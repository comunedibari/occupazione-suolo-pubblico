package it.fincons.osp.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.GruppoDTO;
import it.fincons.osp.dto.TipoAllegatoDTO;
import it.fincons.osp.dto.TipoAllegatoGruppoStatoProcessoDTO;
import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.dto.TypologicalFlagTestoLiberoDTO;
import it.fincons.osp.services.TypologicalService;

@RestController
@RequestMapping("/tipologiche")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "jwt")
public class TypologicalController {

	@Autowired
	TypologicalService typologicalService;

	@GetMapping("/gruppi")
	@LogEntryExit
	public ResponseEntity<List<GruppoDTO>> getGruppi() {

		List<GruppoDTO> result = typologicalService.getGruppi();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/municipi")
	@LogEntryExit
	public ResponseEntity<List<TypologicalDTO>> getMunicipi() {

		List<TypologicalDTO> result = typologicalService.getMunicipi();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/stati-pratiche")
	@LogEntryExit
	public ResponseEntity<List<TypologicalDTO>> getStatiPratiche() {

		List<TypologicalDTO> result = typologicalService.getStatiPratiche();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/tipi-processi")
	@LogEntryExit
	public ResponseEntity<List<TypologicalDTO>> getTipiProcessi() {

		List<TypologicalDTO> result = typologicalService.getTipiProcessi();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/tipi-allegati")
	@LogEntryExit
	public ResponseEntity<List<TipoAllegatoGruppoStatoProcessoDTO>> getTipiAllegati(@RequestParam Long idUtente,
			@RequestParam Long idPratica) {

		List<TipoAllegatoGruppoStatoProcessoDTO> result = typologicalService.getTipiAllegati(idUtente, idPratica);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/tipi-allegati-stato-processo")
	@LogEntryExit
	public ResponseEntity<List<TipoAllegatoGruppoStatoProcessoDTO>> getTipiAllegatiByStatoAndProcesso(
			@RequestParam Long idUtente, @RequestParam Integer idStatoPratica, @RequestParam Integer idTipoProcesso) {

		List<TipoAllegatoGruppoStatoProcessoDTO> result = typologicalService.getTipiAllegatiByStatoAndProcesso(idUtente,
				idStatoPratica, idTipoProcesso);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/tipi-allegati/all")
	@LogEntryExit
	public ResponseEntity<List<TipoAllegatoDTO>> getAllTipiAllegati() {

		List<TipoAllegatoDTO> result = typologicalService.getAllTipiAllegati();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/tipi-ruoli-richiedenti")
	@LogEntryExit
	public ResponseEntity<List<TypologicalDTO>> getTipiRuoliRichiedenti() {

		List<TypologicalDTO> result = typologicalService.getTipiRuoliRichiedenti();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/tipi-attivita-da-svolgere")
	@LogEntryExit
	public ResponseEntity<List<TypologicalFlagTestoLiberoDTO>> getTipiAttivitaDaSvolgere() {

		List<TypologicalFlagTestoLiberoDTO> result = typologicalService.getTipiAttivitaDaSvolgere();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/tipi-documenti-allegati")
	@LogEntryExit
	public ResponseEntity<List<TypologicalDTO>> getTipiDocumentiAllegati() {

		List<TypologicalDTO> result = typologicalService.getTipiDocumentiAllegati();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/tipologie-titoli-edilizi")
	@LogEntryExit
	public ResponseEntity<List<TypologicalFlagTestoLiberoDTO>> getTipologieTitoliEdilizi() {

		List<TypologicalFlagTestoLiberoDTO> result = typologicalService.getTipologieTitoliEdilizi();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/tipi-manufatti")
	@LogEntryExit
	public ResponseEntity<List<TypologicalFlagTestoLiberoDTO>> getTipiManufatti() {

		List<TypologicalFlagTestoLiberoDTO> result = typologicalService.getTipiManufatti();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/tipi-template")
	@LogEntryExit
	public ResponseEntity<List<TypologicalDTO>> getTipiTemplate() {

		List<TypologicalDTO> result = typologicalService.getTipiTemplate();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/tipi-notifiche-scadenzario")
	@LogEntryExit
	public ResponseEntity<List<TypologicalDTO>> getTipiNotificheScadenzario() {

		List<TypologicalDTO> result = typologicalService.getTipiNotificheScadenzario();
		return ResponseEntity.ok(result);
	}
}
