package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.model.TipoProcesso;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoProcessoMapper {

	public TypologicalDTO entityToDto(TipoProcesso entity);

	public TipoProcesso dtoToEntity(TypologicalDTO dto);

}
