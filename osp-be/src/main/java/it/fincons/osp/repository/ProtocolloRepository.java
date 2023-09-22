package it.fincons.osp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.Protocollo;
import it.fincons.osp.model.StatoPratica;
import org.springframework.data.jpa.repository.Query;

public interface ProtocolloRepository extends JpaRepository<Protocollo, Long> {

	Optional<Protocollo> findByPraticaAndStatoPratica(Pratica pratica, StatoPratica statoPratica);

	Optional<Protocollo> findByCodiceProtocollo(String codiceProtocollo);

	Optional<Protocollo> findByNumeroProtocollo(String numeroProtocollo);

	@Query("SELECT p1 FROM Protocollo p1 WHERE :codiceProtocollo is null or p1.codiceProtocollo like :codiceProtocollo " )
	List<Protocollo> findProtocolliByNumeroProtocollo(String codiceProtocollo);
}
