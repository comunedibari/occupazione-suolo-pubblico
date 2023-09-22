package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.TipoRuoloRichiedente;

public interface TipoRuoloRichiedenteRepository extends JpaRepository<TipoRuoloRichiedente, Integer> {

	Optional<TipoRuoloRichiedente> findByDescrizione(String descrizione);

}
