package it.fincons.osp.mapper;

import org.locationtech.jts.geom.Geometry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import it.fincons.osp.dto.DatiRichiestaDTO;
import it.fincons.osp.dto.GeoMultiPointDTO;
import it.fincons.osp.dto.IntegrazioneDTO;
import it.fincons.osp.dto.ParereDTO;
import it.fincons.osp.dto.PraticaDTO;
import it.fincons.osp.dto.ProtocolloDTO;
import it.fincons.osp.dto.RichiedenteDTO;
import it.fincons.osp.dto.RichiestaIntegrazioneDTO;
import it.fincons.osp.dto.RichiestaParereDTO;
import it.fincons.osp.dto.TypologicalDTO;
import it.fincons.osp.dto.UtenteDTO;
import it.fincons.osp.model.DatiRichiesta;
import it.fincons.osp.model.Integrazione;
import it.fincons.osp.model.Municipio;
import it.fincons.osp.model.Parere;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.Protocollo;
import it.fincons.osp.model.Richiedente;
import it.fincons.osp.model.RichiestaIntegrazione;
import it.fincons.osp.model.RichiestaParere;
import it.fincons.osp.model.StatoPratica;
import it.fincons.osp.model.TipoProcesso;
import it.fincons.osp.model.Utente;
import it.fincons.osp.utils.GeometryUtil;

@Mapper(componentModel = "spring")
public interface PraticaMapper {

	public PraticaDTO entityToDto(Pratica entity);

	@Mapping(target = "idAttivitaDaSvolgere", source = "entity.attivitaDaSvolgere.id")
	@Mapping(target = "idTipologiaTitoloEdilizio", source = "entity.tipologiaTitoloEdilizio.id")
	@Mapping(target = "idManufatto", source = "entity.manufatto.id")
	@Mapping(target = "coordUbicazioneTemporanea", source = "entity.coordUbicazioneTemporanea", qualifiedByName = "coordUbicazioneTemporaneaToDto")
	@Mapping(target = "coordUbicazioneDefinitiva", source = "entity.coordUbicazioneDefinitiva", qualifiedByName = "coordUbicazioneDefinitivaToDto")
	public DatiRichiestaDTO datiRichiestaToDto(DatiRichiesta entity);
	
	@Named("coordUbicazioneTemporaneaToDto")
	default GeoMultiPointDTO coordUbicazioneTemporaneaEntityToDto(Geometry multiPoint) {
		return GeometryUtil.parseGeometryToDto(multiPoint);
	}
	
	@Named("coordUbicazioneDefinitivaToDto")
	default GeoMultiPointDTO coordUbicazioneDefinitivaEntityToDto(Geometry multiPoint) {
		return GeometryUtil.parseGeometryToDto(multiPoint);
	}

	@Mapping(target = "idTipoRuoloRichiedente", source = "entity.tipoRuoloRichiedente.id")
	@Mapping(target = "idTipoDocumentoAllegato", source = "entity.tipoDocumentoAllegato.id")
	public RichiedenteDTO richiedenteToDto(Richiedente entity);

	public TypologicalDTO municipioToDto(Municipio entity);

	@Mapping(target = "idGruppo", source = "entity.gruppo.id")
	@Mapping(target = "idsMunicipi", source = "entity.municipi")
	public UtenteDTO utenteToDto(Utente entity);

	public TypologicalDTO tipoProcessoToDto(TipoProcesso entity);

	public TypologicalDTO statoPraticaToDto(StatoPratica entity);

	@Mapping(target = "idPratica", source = "entity.pratica.id")
	@Mapping(target = "idStatoPratica", source = "entity.statoPratica.id")
	public ProtocolloDTO protocolloToDto(Protocollo entity);

	default Integer fromMunicipio(Municipio municipio) {
		return municipio.getId();
	}

	@Mapping(target = "idPratica", source = "entity.pratica.id")
	@Mapping(target = "idUtenteRichiedente", source = "entity.utenteRichiedente.id")
	@Mapping(target = "idStatoPratica", source = "entity.statoPratica.id")
	@Mapping(target = "idGruppoDestinatarioParere", source = "entity.gruppoDestinatarioParere.id")
	public RichiestaParereDTO richiestaParereToDto(RichiestaParere entity);

	@Mapping(target = "idRichiestaParere", source = "entity.richiestaParere.id")
	@Mapping(target = "idUtenteParere", source = "entity.utenteParere.id")
	public ParereDTO parereToDto(Parere entity);

	@Mapping(target = "idPratica", source = "entity.pratica.id")
	@Mapping(target = "idUtenteRichiedente", source = "entity.utenteRichiedente.id")
	@Mapping(target = "idStatoPratica", source = "entity.statoPratica.id")
	public RichiestaIntegrazioneDTO richiestaIntegrazioneToDto(RichiestaIntegrazione entity);
	
	@Mapping(target = "idRichiestaIntegrazione", source = "entity.richiestaIntegrazione.id")
	@Mapping(target = "idUtenteIntegrazione", source = "entity.utenteIntegrazione.id")
	public IntegrazioneDTO integrazioneToDto(Integrazione entity);
}
