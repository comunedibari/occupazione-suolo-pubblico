package it.fincons.osp.repository.specification;

import java.util.List;
import java.util.Optional;

import it.fincons.osp.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;

import javax.persistence.criteria.CriteriaBuilder;

@Component
@NoArgsConstructor
public class PraticaSpecification implements GenericSpecification<Pratica> {

	public Optional<Specification<Pratica>> inMunicipio(List<Municipio> municipi) {
		return municipi == null || municipi.isEmpty() ? Optional.empty()
				: Optional.of((root, query, builder) -> root.get("municipio").in(municipi));
	}

	public Optional<Specification<Pratica>> inStatoPratica(List<StatoPratica> statiPratica) {
		return statiPratica == null || statiPratica.isEmpty() ? Optional.empty()
				: Optional.of((root, query, builder) -> root.get("statoPratica").in(statiPratica));
	}

	public Optional<Specification<Pratica>> inFirmatario(List<Richiedente> richiedenti) {
		return richiedenti == null || richiedenti.isEmpty() ? Optional.empty()
				: Optional.of((root, query, builder) -> root.get("firmatario").in(richiedenti));
	}

	public Optional<Specification<Pratica>> inDestinatario(List<Richiedente> richiedenti) {
		return richiedenti == null || richiedenti.isEmpty() ? Optional.empty()
				: Optional.of((root, query, builder) -> root.get("destinatario").in(richiedenti));
	}

	public Optional<Specification<Pratica>> inRichiestaPareri(List<RichiestaParere> richiestePareri) {
		return richiestePareri == null || richiestePareri.isEmpty() ? Optional.empty()
				: Optional.of((root, query, builder) -> root.join("richiestePareri").in(richiestePareri));
	}
	
	public Optional<Specification<Pratica>> inProtocollo(List<Protocollo> protocolli) {
		return protocolli == null || protocolli.isEmpty() ? Optional.empty()
				: Optional.of((root, query, builder) -> root.join("protocolli").in(protocolli));
	}

	public Optional<Specification<Pratica>> inTipoProcesso(List<TipoProcesso> tipoProcesso) {
		return tipoProcesso == null || tipoProcesso.isEmpty() ? Optional.empty()
				: Optional.of((root, query, builder) -> root.get("tipoProcesso").in(tipoProcesso));
	}

	public Optional<Specification<Pratica>> inDatiRichiesta(List<DatiRichiesta> datiRichiesta) {
		return datiRichiesta == null || datiRichiesta.isEmpty() ? Optional.empty()
				: Optional.of((root, query, builder) -> root.get("datiRichiesta").in(datiRichiesta));
	}

	public Optional<Specification<Pratica>> praticaById(Long idPratica) {
		return Optional.of((root, query, builder) -> builder.equal(root.get("id"), idPratica));
	}
	public Optional<Specification<Pratica>> praticaByIds(List<Long> idPratiche) {
		return Optional.of((root, query, builder) -> root.get("id").in(idPratiche));
	}
}