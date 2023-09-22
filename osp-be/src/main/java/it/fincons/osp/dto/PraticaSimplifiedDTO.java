package it.fincons.osp.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PraticaSimplifiedDTO {

	private Long id;
	
	private Long idPraticaOriginaria;
	
	private Long idProrogaPrecedente;
	
	private String motivazioneRichiesta;

	private DatiRichiestaDTO datiRichiesta;

	private RichiedenteDTO firmatario;

	private RichiedenteDTO destinatario;

	private TypologicalDTO municipio;

	private UtenteDTO utentePresaInCarico;

	private UtenteDTO utenteCreazione;

	private UtenteDTO utenteModifica;
	
	private UtenteDTO utenteAssegnatario;

	private LocalDateTime dataCreazione;
	
	private LocalDateTime dataInserimento;

	private LocalDateTime dataModifica;

	private TypologicalDTO tipoProcesso;

	private TypologicalDTO statoPratica;

	private String infoPassaggioStato;

	private Boolean flagVerificaFormale;

	private Boolean flagProceduraDiniego;
	
	private List<ProtocolloDTO> protocolli;

	private String codiceDetermina;
	
	private LocalDate dataEmissioneDetermina;
	
	private String codiceDeterminaRinuncia;
	
	private LocalDate dataEmissioneDeterminaRinuncia;
	
	private String notaAlCittadinoRda;
	
	private String codiceDeterminaRda;
	
	private LocalDate dataEmissioneDeterminaRda;

	private LocalDateTime dataScadenzaPratica;

	private LocalDateTime dataScadenzaRigetto;

	private LocalDateTime dataScadenzaPreavvisoDiniego;
	
	private LocalDateTime dataScadenzaPagamento;

	private Integer contatoreRichiesteIntegrazioni;
	
	private String nomeCittadinoEgov;

	private String cognomeCittadinoEgov;
	
	private String cfCittadinoEgov;

	private MarcaBolloPraticaDTO marcaBolloPratica;

	private boolean flagEsenzionePagamentoCUP;

	private String motivazioneEsenzionePagamentoCup;
}
