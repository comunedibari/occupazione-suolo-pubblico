package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.Municipio;

public interface MunicipioRepository extends JpaRepository<Municipio, Integer> {

	Optional<Municipio> findByDescrizione(String descrizione);

}
