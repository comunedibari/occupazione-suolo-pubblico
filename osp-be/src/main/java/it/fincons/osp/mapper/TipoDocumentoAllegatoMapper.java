package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.model.TipoDocumentoAllegato;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoDocumentoAllegatoMapper {

	public TypologicalDTO entityToDto(TipoDocumentoAllegato entity);

	public TipoDocumentoAllegato dtoToEntity(TypologicalDTO dto);

}
