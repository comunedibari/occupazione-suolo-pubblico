package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.ParereDTO;
import it.fincons.osp.model.Parere;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ParereMapper {

	@Mapping(target = "idRichiestaParere", source = "entity.richiestaParere.id")
	@Mapping(target = "idUtenteParere", source = "entity.utenteParere.id")
	public ParereDTO entityToDto(Parere entity);
}
