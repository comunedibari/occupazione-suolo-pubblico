package it.fincons.osp.payload.protocollazione.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PersonaFisicaContainerDTO extends MittenteDTO {

	PersonaFisicaDTO personaFisica;
}
