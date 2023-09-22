package it.fincons.osp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import it.fincons.osp.dto.TipoAllegatoDTO;
import it.fincons.osp.dto.TipoAllegatoGruppoStatoProcessoDTO;
import it.fincons.osp.model.TipoAllegato;
import it.fincons.osp.model.TipoAllegatoGruppoStatoProcesso;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoAllegatoGruppoStatoProcessoMapper {

	@Mapping(target = "idStatoPratica", source = "entity.statoPratica.id")
	@Mapping(target = "idTipoProcesso", source = "entity.tipoProcesso.id")
	@Mapping(target = "idGruppo", source = "entity.gruppo.id")
	public TipoAllegatoGruppoStatoProcessoDTO entityToDto(TipoAllegatoGruppoStatoProcesso entity);

	public TipoAllegatoDTO tipoAllegatoToDto(TipoAllegato entity);
}
