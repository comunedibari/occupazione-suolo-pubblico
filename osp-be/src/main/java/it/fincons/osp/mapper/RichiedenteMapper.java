package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.RichiedenteDTO;
import it.fincons.osp.model.Richiedente;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RichiedenteMapper {

	@Mapping(target = "idTipoRuoloRichiedente", source = "entity.tipoRuoloRichiedente.id")
	@Mapping(target = "idTipoDocumentoAllegato", source = "entity.tipoDocumentoAllegato.id")
	public RichiedenteDTO entityToDto(Richiedente entity);

	@Mapping(target = "tipoRuoloRichiedente", ignore = true)
	@Mapping(target = "tipoDocumentoAllegato", ignore = true)
	public Richiedente dtoToEntity(RichiedenteDTO dto);

}
