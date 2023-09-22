package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.model.TipoNotificaScadenzario;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoNotificaScadenzarioMapper {

	public TypologicalDTO entityToDto(TipoNotificaScadenzario entity);

	public TipoNotificaScadenzario dtoToEntity(TypologicalDTO dto);

}
