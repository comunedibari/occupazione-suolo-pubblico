package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.fincons.osp.dto.UserLoginDTO;
import it.fincons.osp.model.Municipio;
import it.fincons.osp.model.Utente;

@Mapper(componentModel = "spring")
public interface UserLoginMapper {

	@Mapping(target = "idsMunicipi", source = "entity.municipi")
	public UserLoginDTO entityToDto(Utente entity);

	default Integer fromMunicipio(Municipio municipio) {
		return municipio.getId();
	}

}
