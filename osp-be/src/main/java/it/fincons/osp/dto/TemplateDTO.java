package it.fincons.osp.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TemplateDTO extends TemplateSimplifiedDTO {

	@NotNull
	public String fileTemplate;

}
