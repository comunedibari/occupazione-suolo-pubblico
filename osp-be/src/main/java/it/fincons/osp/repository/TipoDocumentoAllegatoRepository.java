package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.TipoDocumentoAllegato;

public interface TipoDocumentoAllegatoRepository extends JpaRepository<TipoDocumentoAllegato, Integer> {

	Optional<TipoDocumentoAllegato> findByDescrizione(String descrizione);

}
