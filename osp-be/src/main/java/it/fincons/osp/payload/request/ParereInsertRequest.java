package it.fincons.osp.payload.request;

import java.util.List;

import javax.validation.constraints.NotNull;

import it.fincons.osp.dto.ParereDTO;
import lombok.Data;

@Data
public class ParereInsertRequest {

	@NotNull
	private ParereDTO parere;

	private List<Long> listaIdUtentiEmail;

	private boolean flagPec;

}
