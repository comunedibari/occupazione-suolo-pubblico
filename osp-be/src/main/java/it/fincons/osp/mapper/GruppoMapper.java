package it.fincons.osp.mapper;

import org.mapstruct.Mapper;

import it.fincons.osp.dto.GruppoDTO;
import it.fincons.osp.model.Gruppo;

@Mapper(componentModel = "spring")
public interface GruppoMapper {

	public GruppoDTO entityToDto(Gruppo entity);

	public Gruppo dtoToEntity(GruppoDTO dto);

}
