package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.TipoProcesso;

public interface TipoProcessoRepository extends JpaRepository<TipoProcesso, Integer> {

	Optional<TipoProcesso> findByDescrizione(String descrizione);

}
