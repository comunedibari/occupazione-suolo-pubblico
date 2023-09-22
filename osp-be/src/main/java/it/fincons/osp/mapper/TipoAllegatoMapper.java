package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.TipoAllegatoDTO;
import it.fincons.osp.model.TipoAllegato;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoAllegatoMapper {

	public TipoAllegatoDTO entityToDto(TipoAllegato entity);

}
