package it.fincons.osp.payload.protocollazione.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PersonaGiuridicaDTO extends DestinatarioDTO {

	String ragioneSociale;
	String piva;
}
