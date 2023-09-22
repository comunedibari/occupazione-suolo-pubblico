package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.ParereDTO;
import it.fincons.osp.dto.RichiestaParereDTO;
import it.fincons.osp.model.Parere;
import it.fincons.osp.model.RichiestaParere;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RichiestaParereMapper {

	@Mapping(target = "idPratica", source = "entity.pratica.id")
	@Mapping(target = "idUtenteRichiedente", source = "entity.utenteRichiedente.id")
	@Mapping(target = "idStatoPratica", source = "entity.statoPratica.id")
	@Mapping(target = "idGruppoDestinatarioParere", source = "entity.gruppoDestinatarioParere.id")
	public RichiestaParereDTO entityToDto(RichiestaParere entity);
	
	@Mapping(target = "idRichiestaParere", source = "entity.richiestaParere.id")
	@Mapping(target = "idUtenteParere", source = "entity.utenteParere.id")
	public ParereDTO parereToDto(Parere entity);
}
