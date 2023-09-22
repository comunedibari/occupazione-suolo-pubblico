package it.fincons.osp.dto;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GeoPointDTO {
	
	@NonNull
	private Double lon;

	@NonNull
	private Double lat;

}
