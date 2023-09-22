package it.fincons.osp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.TipoNotificaScadenzario;

public interface TipoNotificaScadenzarioRepository extends JpaRepository<TipoNotificaScadenzario, Integer> {

	Optional<TipoNotificaScadenzario> findByDescrizione(String descrizione);

}
