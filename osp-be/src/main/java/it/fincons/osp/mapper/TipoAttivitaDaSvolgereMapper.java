package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.dto.TypologicalFlagTestoLiberoDTO;
import it.fincons.osp.model.TipoAttivitaDaSvolgere;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoAttivitaDaSvolgereMapper {

	public TypologicalFlagTestoLiberoDTO entityToDto(TipoAttivitaDaSvolgere entity);

	public TipoAttivitaDaSvolgere dtoToEntity(TypologicalDTO dto);

}
