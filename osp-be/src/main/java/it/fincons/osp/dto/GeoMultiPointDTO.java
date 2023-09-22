package it.fincons.osp.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class GeoMultiPointDTO {
	
	@NotNull
	private List<GeoPointDTO> points;
	
}
