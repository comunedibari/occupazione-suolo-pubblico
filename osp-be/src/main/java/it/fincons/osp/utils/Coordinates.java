package it.fincons.osp.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class that rapresent geographic coordinates in Decimal Degrees
 * 
 * @author alfonso.coppola
 *
 */
@Data
@AllArgsConstructor
public class Coordinates {

	Double latitude;
	Double longitude;

}
