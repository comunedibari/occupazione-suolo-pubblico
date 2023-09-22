package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.model.TipoTemplate;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoTemplateMapper {

	public TypologicalDTO entityToDto(TipoTemplate entity);

	public TipoTemplate dtoToEntity(TypologicalDTO dto);

}
