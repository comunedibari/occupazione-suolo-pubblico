package it.fincons.osp.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.AutomiProtocolloDTO;
import it.fincons.osp.dto.PraticaDTO;
import it.fincons.osp.dto.UtenteDTO;
import it.fincons.osp.payload.request.ModificaPasswordRequest;
import it.fincons.osp.payload.request.PraticaEgovEditRequest;
import it.fincons.osp.payload.request.UtenteEditRequest;
import it.fincons.osp.payload.request.UtenteInsertRequest;
import it.fincons.osp.services.AutomiProtocolloService;
import it.fincons.osp.services.UtenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/automi-protocollo-management")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "jwt")
public class AutomiProtocolloController {

    @Autowired
    private AutomiProtocolloService automiProtocolloService;

	@GetMapping("/automi")
    @LogEntryExit
    public ResponseEntity<List<AutomiProtocolloDTO>> getAllAutomiProtocolloDTO() {

        List<AutomiProtocolloDTO> result = automiProtocolloService.getAllAutomiProtocolloDTO();

        if (result == null || result.size() == 0) {
            return new ResponseEntity("Nessun elemento trovato.", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/automi/{automaId}")
    @LogEntryExit
    public ResponseEntity<AutomiProtocolloDTO> getAutomaProtocolloDTOByUoid(@PathVariable(name = "automaId", required = true) Long automaId) {

        AutomiProtocolloDTO result = automiProtocolloService.getAutomaProtocolloDTOByid(automaId);

        if (result == null) {
            return new ResponseEntity("Nessun elemento trovato.", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/automi")
    @LogEntryExit
    public ResponseEntity<AutomiProtocolloDTO> updateAutoma(@Valid @RequestBody AutomiProtocolloDTO automiProtocolloDTO) {

        AutomiProtocolloDTO result = automiProtocolloService.update(automiProtocolloDTO);

        if (result == null) {
            return new ResponseEntity("Nessun elemento trovato.", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(result);
    }

    /*
    @GetMapping("/automi")
    @LogEntryExit
    public ResponseEntity<AutomiProtocolloDTO> getAutomaProtocolloDTOByUoid(@RequestParam(name = "uoid", required = true) String uoid) {

        AutomiProtocolloDTO result = automiProtocolloService.getAutomaProtocolloDTOByUoid(uoid);

        if (result == null) {
            return new ResponseEntity("Nessun elemento trovato.", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(result);
    }*/

	@GetMapping("/automi/municipio/{municipioId}")
	@LogEntryExit
	public ResponseEntity<AutomiProtocolloDTO> getAutomaProtocolloDTOByIdMunicipio(@PathVariable(name = "municipioId", required = true) Integer municipioId) {

		AutomiProtocolloDTO result = automiProtocolloService.getAutomaProtocolloDTOByMunicipioId(municipioId);

		if (result == null) {
			return new ResponseEntity("Nessun elemento trovato.", HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(result);
	}


}
