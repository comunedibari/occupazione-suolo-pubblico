package it.fincons.osp.dto;

import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.RichiestaIntegrazione;
import it.fincons.osp.model.RichiestaParere;
import lombok.Data;

@Data
public class ComunicazioneMailInsertDTO {

	private Pratica pratica;

	private RichiestaIntegrazione richiestaIntegrazione;

	private RichiestaParere richiestaParere;

	private String destinatari;

	private String destinatariCc;

	private String oggetto;

	private String testo;
	
	private boolean flagPec;
	
	private String nomeFileAllegato;
	private String mimeTypeFileAllegato;
	private byte[] fileAllegato;

	public ComunicazioneMailInsertDTO() {
		this.flagPec = false;
	}

	public ComunicazioneMailInsertDTO(ComunicazioneMailInsertDTO source) {
		this.pratica = source.pratica;
		this.richiestaIntegrazione = source.richiestaIntegrazione;
		this.richiestaParere = source.richiestaParere;
		this.oggetto = source.oggetto;
		this.testo = source.testo;
		this.flagPec = source.flagPec;
	}

}
