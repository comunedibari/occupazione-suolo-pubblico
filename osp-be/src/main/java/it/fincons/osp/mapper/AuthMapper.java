package it.fincons.osp.mapper;

import org.mapstruct.Mapper;

import it.fincons.osp.dto.AuthDTO;
import it.fincons.osp.model.Gruppo;

@Mapper(componentModel = "spring")
public interface AuthMapper {

	public AuthDTO entityToDto(Gruppo entity);

	public Gruppo dtoToEntity(AuthDTO dto);

}
