package it.fincons.osp.mapper;

import java.util.Base64;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import it.fincons.osp.dto.TemplateDTO;
import it.fincons.osp.dto.TemplateSimplifiedDTO;
import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.dto.UtenteDTO;
import it.fincons.osp.model.Municipio;
import it.fincons.osp.model.Template;
import it.fincons.osp.model.Utente;

@Mapper(componentModel = "spring")
public interface TemplateMapper {

	@Mapping(target = "fileTemplate", source = "entity.fileTemplate", qualifiedByName = "fileTemplate")
	public TemplateDTO entityToDto(Template entity);

	@Named("fileTemplate")
	default String encodeFileTemplate(byte[] fileTemplate) {
		return new String(Base64.getEncoder().encode(fileTemplate));
	}

	public TemplateSimplifiedDTO entityToSimplifiedDto(Template entity);

	public TypologicalDTO tipoTemplateToDto(Template entity);

	@Mapping(target = "idGruppo", source = "entity.gruppo.id")
	@Mapping(target = "idsMunicipi", source = "entity.municipi")
	public UtenteDTO utenteToDto(Utente entity);
	
	default Integer fromMunicipio(Municipio municipio) {
		return municipio.getId();
	}
}
