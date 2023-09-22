package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.dto.TypologicalFlagTestoLiberoDTO;
import it.fincons.osp.model.TipoManufatto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoManufattoMapper {

	public TypologicalFlagTestoLiberoDTO entityToDto(TipoManufatto entity);

	public TipoManufatto dtoToEntity(TypologicalDTO dto);

}
