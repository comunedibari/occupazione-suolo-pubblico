package it.fincons.osp.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class NotificaScadenzarioDTO {
	
	@NotNull
	private Long id;
	
	@NotNull
	private PraticaSimplifiedDTO pratica;
	
	@NotNull
	private UtenteDTO utente;
	
	@NotNull
	private TypologicalDTO tipoNotificaScadenzario;

	private LocalDateTime dataNotifica;

}
