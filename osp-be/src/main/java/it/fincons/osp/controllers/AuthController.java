package it.fincons.osp.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.dto.AuthDTO;
import it.fincons.osp.dto.ComunicazioneMailInsertDTO;
import it.fincons.osp.dto.GroupsDTO;
import it.fincons.osp.dto.UserLoginDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import it.fincons.osp.mapper.AuthMapper;
import it.fincons.osp.mapper.UserLoginMapper;
import it.fincons.osp.model.Gruppo;
import it.fincons.osp.model.Utente;
import it.fincons.osp.payload.request.LoginRequest;
import it.fincons.osp.payload.request.LogoutRequest;
import it.fincons.osp.payload.request.RecuperaPasswordRequest;
import it.fincons.osp.payload.response.LoginInfoResponse;
import it.fincons.osp.repository.GruppoRepository;
import it.fincons.osp.repository.UtenteRepository;
import it.fincons.osp.security.jwt.JwtUtils;
import it.fincons.osp.security.services.UserDetailsImpl;
import it.fincons.osp.services.ComunicazioneMailService;
import it.fincons.osp.services.UtenteService;
import it.fincons.osp.utils.EmailException;
import it.fincons.osp.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UtenteRepository utenteRepository;

	@Autowired
	GruppoRepository gruppoRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	UserLoginMapper userLoginMapper;

	@Autowired
	AuthMapper authMapper;

	@Autowired
	UtenteService utenteService;

	@Autowired
	ComunicazioneMailService comunicazioneMailService;

	@PostMapping("/login")
	@LogEntryExit
	public ResponseEntity<LoginInfoResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		String gruppoDsc = userDetails.getAuthorities().stream().map(item -> item.getAuthority()).findFirst()
				.orElseThrow(() -> new RuntimeException("Errore: nessun gruppo impostato"));

		Gruppo gruppo = gruppoRepository.findByDescrizione(gruppoDsc)
				.orElseThrow(() -> new BusinessException(ErrorCode.E9,
						"Errore: nessun gruppo corrispondente alle descrizione", gruppoDsc));

		Utente utente = utenteService.updateLastLoginByUsername(userDetails.getUsername());

		UserLoginDTO userLogin = userLoginMapper.entityToDto(utente);
		AuthDTO auth = authMapper.entityToDto(gruppo);

		GroupsDTO groups = new GroupsDTO();
		groups.setId(gruppo.getId());
		groups.setAuth(auth);

		return ResponseEntity.ok(new LoginInfoResponse(true, jwt, utente.getUsername(), groups, userLogin));
	}

	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "jwt")
	@PostMapping("/logout")
	@LogEntryExit
	public ResponseEntity<Void> logoutUser(@Valid @RequestBody LogoutRequest logoutRequest) {
		utenteService.updateLastLoginByUsername(logoutRequest.getUsername());

		log.info("Logout utente registrato");

		return ResponseEntity.noContent().build();
	}

	@PostMapping("/recupero-password")
	@LogEntryExit
	public ResponseEntity<Void> recuperoPassword(@Valid @RequestBody RecuperaPasswordRequest recuperaPasswordRequest)
			throws EmailException {

		Utente utente = utenteRepository.findByUsernameAndFlagEliminatoFalse(recuperaPasswordRequest.getUsername())
				.orElseThrow(() -> new BusinessException(ErrorCode.E3, "Errore: username non presente nel sistema",
						recuperaPasswordRequest.getUsername()));

		String password = utenteService.generateNewPassword(utente);

		ComunicazioneMailInsertDTO comunicazioneMailInsert = new ComunicazioneMailInsertDTO();
		comunicazioneMailInsert
				.setOggetto("Bari Smart City - Gestionale Occupazione Suolo Pubblico - Recupero password");
		comunicazioneMailInsert.setTesto(EmailUtils.getContentEmailResetPassword(utente.getUsername(), password));
		comunicazioneMailInsert.setDestinatari(utente.getEmail());

		comunicazioneMailService.insertComunicazioneMail(comunicazioneMailInsert);

		log.info("Inserita comunicazione mail da mandare all'utente");

		return ResponseEntity.noContent().build();
	}
}
