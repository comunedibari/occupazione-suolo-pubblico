package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.dto.TypologicalFlagTestoLiberoDTO;
import it.fincons.osp.model.TipologiaTitoloEdilizio;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipologiaTitoloEdilizioMapper {

	public TypologicalFlagTestoLiberoDTO entityToDto(TipologiaTitoloEdilizio entity);

	public TipologiaTitoloEdilizio dtoToEntity(TypologicalDTO dto);

}
