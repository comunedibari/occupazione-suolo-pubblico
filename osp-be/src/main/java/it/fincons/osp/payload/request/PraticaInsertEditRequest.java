package it.fincons.osp.payload.request;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import it.fincons.osp.dto.DatiRichiestaDTO;
import it.fincons.osp.dto.MarcaBolloPraticaDTO;
import it.fincons.osp.dto.RichiedenteDTO;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PraticaInsertEditRequest {

	private Long id;

	private Long idPraticaOriginaria;

	private Long idUtente;

	@NotNull
	private Integer idMunicipio;

	private String motivazioneRichiesta;

	@NotNull
	private RichiedenteDTO firmatario;

	private RichiedenteDTO destinatario;

	@NotNull
	private DatiRichiestaDTO datiRichiesta;

	@NotNull
	private Integer idTipoProcesso;

	private String nomeCittadinoEgov;

	private String cognomeCittadinoEgov;

	private String cfCittadinoEgov;

	private boolean originEgov;

	private MarcaBolloPraticaDTO marcaBolloPratica;

	private MarcaBolloPraticaDTO marcaBolloDetermina;

}
