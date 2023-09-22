package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.model.TipoRuoloRichiedente;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoRuoloRichiedenteMapper {

	public TypologicalDTO entityToDto(TipoRuoloRichiedente entity);

	public TipoRuoloRichiedente dtoToEntity(TypologicalDTO dto);

}
