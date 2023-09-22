package it.fincons.osp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.ComunicazioneMail;

public interface ComunicazioneMailRepository extends JpaRepository<ComunicazioneMail, Long> {

	List<ComunicazioneMail> findByFlagInviataFalseOrderByDataInserimentoAsc();

}
