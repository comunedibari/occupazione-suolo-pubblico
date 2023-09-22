package it.fincons.osp.payload.request;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UlterioriPareriInsertRequest {

	@NotNull
	Long idPratica;

	@NotNull
	private List<Integer> idGruppiDestinatariPareri;

}
