package it.fincons.osp.controllers;

import io.swagger.v3.oas.annotations.security.*;
import it.fincons.osp.annotation.*;
import it.fincons.osp.config.*;
import it.fincons.osp.dto.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/stats")
@SecurityRequirement(name = "jwt")
public class StatisticheController {

	@Autowired
	private OspConfiguration ospConfiguration;

	@GetMapping(value = {"/caricaDashboardKibana", "/caricaDashboardKibana/{municipio}"})
	@PreAuthorize("isAuthenticated()")
	@LogEntryExit
	public ResponseEntity<DashboardDTO> getTemplate(@PathVariable(value="municipio", required = false) String municipio) {
		Map<String, String> kibanaDashboardConfig = ospConfiguration.getDashboard();

		String dashboardLink = kibanaDashboardConfig.get("m" + municipio);
		if(dashboardLink == null)
			dashboardLink = kibanaDashboardConfig.get("default");
		return ResponseEntity.ok(new DashboardDTO(dashboardLink));
	}

}
