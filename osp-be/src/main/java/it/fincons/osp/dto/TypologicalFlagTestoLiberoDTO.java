package it.fincons.osp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TypologicalFlagTestoLiberoDTO extends TypologicalDTO {

	private boolean flagTestoLibero;

}
