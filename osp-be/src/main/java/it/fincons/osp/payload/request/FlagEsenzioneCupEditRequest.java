package it.fincons.osp.payload.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class FlagEsenzioneCupEditRequest {
	
	@NotNull
	private Long idPratica;

	private String motivazioneEsenzionePagamentoCup;

	@NotEmpty
	private String flagEsenzionePagamentoCUP;
}
