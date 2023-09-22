package it.fincons.osp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import it.fincons.osp.model.TipoOperazioneProtocollo;
import lombok.Data;

import javax.persistence.Column;

@Data
public class ProtocolloDTO {

	private Long id;

	private Long idPratica;

	private Integer idStatoPratica;

	private String codiceProtocollo;
	
	private LocalDateTime dataProtocollo;

	private TipoOperazioneProtocollo tipoOperazione;
	
	private String codiceDeterminaRettifica;
	
	private LocalDate dataEmissioneDeterminaRettifica;

	private String numeroProtocollo;

	private String anno;
}
