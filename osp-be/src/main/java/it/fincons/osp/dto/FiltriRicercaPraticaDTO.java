package it.fincons.osp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FiltriRicercaPraticaDTO {

	private String nome;
	private String cognome;
	private String denominazioneRagSoc;
	private String codFiscalePIva;
	private String numProtocollo;
	private String numProvvedimento;

	private Integer tipoProcesso;
	private String indirizzo;
}
