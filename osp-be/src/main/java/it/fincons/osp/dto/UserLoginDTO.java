package it.fincons.osp.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UserLoginDTO {
	
	private Long id;
	private String codiceFiscale;
	private String nome;
	private String cognome;
	private String ragioneSociale;

	@JsonProperty("municipio_ids")
	private Set<Integer> idsMunicipi;

	private LocalDateTime lastLogin;
	private String email;
	
	private String uoId;
	private String indirizzo;
	
}
