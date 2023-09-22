package it.fincons.osp.mapper;

import java.util.Base64;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import it.fincons.osp.dto.AllegatoDTO;
import it.fincons.osp.dto.AllegatoSimplifiedDTO;
import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.model.Allegato;
import it.fincons.osp.model.TipoAllegato;

@Mapper(componentModel = "spring")
public interface AllegatoMapper {

	@Mapping(target = "idPratica", source = "entity.pratica.id")
	@Mapping(target = "idParere", source = "entity.parere.id")
	@Mapping(target = "idIntegrazione", source = "entity.integrazione.id")
	@Mapping(target = "fileAllegato", source = "entity.fileAllegato", qualifiedByName = "fileAllegato")
	public AllegatoDTO entityToDto(Allegato entity);

	@Named("fileAllegato")
	default String encodeFileAllegato(byte[] fileAllegato) {
		return new String(Base64.getEncoder().encode(fileAllegato));
	}

	@Mapping(target = "idPratica", source = "entity.pratica.id")
	@Mapping(target = "idParere", source = "entity.parere.id")
	@Mapping(target = "idIntegrazione", source = "entity.integrazione.id")
	public AllegatoSimplifiedDTO entityToSimplifiedDto(Allegato entity);
	
	public TypologicalDTO tipoAllegatoToDto(TipoAllegato entity);
}
