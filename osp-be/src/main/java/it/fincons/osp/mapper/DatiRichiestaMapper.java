package it.fincons.osp.mapper;

import org.locationtech.jts.geom.Geometry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.DatiRichiestaDTO;
import it.fincons.osp.dto.GeoMultiPointDTO;
import it.fincons.osp.model.DatiRichiesta;
import it.fincons.osp.utils.GeometryUtil;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DatiRichiestaMapper {

	@Mapping(target = "idAttivitaDaSvolgere", source = "entity.attivitaDaSvolgere.id")
	@Mapping(target = "idTipologiaTitoloEdilizio", source = "entity.tipologiaTitoloEdilizio.id")
	@Mapping(target = "idManufatto", source = "entity.manufatto.id")
	@Mapping(target = "coordUbicazioneTemporanea", source = "entity.coordUbicazioneTemporanea", qualifiedByName = "coordUbicazioneTemporaneaToDto")
	@Mapping(target = "coordUbicazioneDefinitiva", source = "entity.coordUbicazioneDefinitiva", qualifiedByName = "coordUbicazioneDefinitivaToDto")
	public DatiRichiestaDTO entityToDto(DatiRichiesta entity);

	@Mapping(target = "attivitaDaSvolgere", ignore = true)
	@Mapping(target = "tipologiaTitoloEdilizio", ignore = true)
	@Mapping(target = "manufatto", ignore = true)
	@Mapping(target = "coordUbicazioneTemporanea", qualifiedByName = "coordUbicazioneTemporaneaToEntity")
	@Mapping(target = "coordUbicazioneDefinitiva", qualifiedByName = "coordUbicazioneDefinitivaToEntity")
	public DatiRichiesta dtoToEntity(DatiRichiestaDTO dto);
	
	@Named("coordUbicazioneTemporaneaToDto")
	default GeoMultiPointDTO coordUbicazioneTemporaneaEntityToDto(Geometry multiPoint) {
		return GeometryUtil.parseGeometryToDto(multiPoint);
	}
	
	@Named("coordUbicazioneDefinitivaToDto")
	default GeoMultiPointDTO coordUbicazioneDefinitivaEntityToDto(Geometry multiPoint) {
		return GeometryUtil.parseGeometryToDto(multiPoint);
	}

	@Named("coordUbicazioneTemporaneaToEntity")
	default Geometry coordUbicazioneTemporaneaDtoToEntity(GeoMultiPointDTO geoMultiPoint) {
		return GeometryUtil.parseGeoGeomerty(geoMultiPoint);
	}

	@Named("coordUbicazioneDefinitivaToEntity")
	default Geometry coordUbicazioneDefinitivaDtoToEntity(GeoMultiPointDTO geoMultiPoint) {
		return GeometryUtil.parseGeoGeomerty(geoMultiPoint);
	}


}
