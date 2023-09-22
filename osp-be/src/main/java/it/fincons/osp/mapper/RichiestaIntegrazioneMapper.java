package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.IntegrazioneDTO;
import it.fincons.osp.dto.RichiestaIntegrazioneDTO;
import it.fincons.osp.model.Integrazione;
import it.fincons.osp.model.RichiestaIntegrazione;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RichiestaIntegrazioneMapper {

	@Mapping(target = "idPratica", source = "entity.pratica.id")
	@Mapping(target = "idUtenteRichiedente", source = "entity.utenteRichiedente.id")
	@Mapping(target = "idStatoPratica", source = "entity.statoPratica.id")
	public RichiestaIntegrazioneDTO entityToDto(RichiestaIntegrazione entity);
	
	@Mapping(target = "idRichiestaIntegrazione", source = "entity.richiestaIntegrazione.id")
	@Mapping(target = "idUtenteIntegrazione", source = "entity.utenteIntegrazione.id")
	public IntegrazioneDTO integrazioneToDto(Integrazione entity);
}
