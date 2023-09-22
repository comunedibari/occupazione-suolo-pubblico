package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.model.Municipio;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MunicipioMapper {

	public TypologicalDTO entityToDto(Municipio entity);

	public Municipio dtoToEntity(TypologicalDTO dto);

}
