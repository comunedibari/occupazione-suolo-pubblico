package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.fincons.osp.dto.UtenteDTO;
import it.fincons.osp.model.Municipio;
import it.fincons.osp.model.Utente;

@Mapper(componentModel = "spring")
public interface UtenteMapper {

	@Mapping(target = "idGruppo", source = "entity.gruppo.id")
	@Mapping(target = "idsMunicipi", source = "entity.municipi")
	public UtenteDTO entityToDto(Utente entity);

	default Integer fromMunicipio(Municipio municipio) {
		return municipio.getId();
	}

}
