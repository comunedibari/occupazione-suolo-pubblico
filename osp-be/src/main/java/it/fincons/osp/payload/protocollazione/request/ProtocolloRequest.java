package it.fincons.osp.payload.protocollazione.request;

import it.fincons.osp.model.Allegato;
import lombok.Data;

import java.util.List;

@Data
public class ProtocolloRequest {
	private MittenteDTO mittente;
	protected List<DestinatarioDTO> destinatari;
	private DocumentoDTO documento;
	private List<AllegatoDTO> allegati;
	private String areaOrganizzativaOmogenea;
	private String amministrazione;
	private String oggetto;
	private String idUtente;

}
