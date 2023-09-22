package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.IntegrazioneDTO;
import it.fincons.osp.model.Integrazione;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IntegrazioneMapper {

	@Mapping(target = "idRichiestaIntegrazione", source = "entity.richiestaIntegrazione.id")
	@Mapping(target = "idUtenteIntegrazione", source = "entity.utenteIntegrazione.id")
	public IntegrazioneDTO entityToDto(Integrazione entity);
}
