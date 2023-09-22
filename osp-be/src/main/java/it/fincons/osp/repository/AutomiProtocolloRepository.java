package it.fincons.osp.repository;

import it.fincons.osp.dto.AutomiProtocolloDTO;
import it.fincons.osp.model.AutomiProtocollo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutomiProtocolloRepository extends JpaRepository<AutomiProtocollo, Long> {
    Optional<AutomiProtocollo> findAutomaProtocolloByMunicipioId(Integer municipioId);

    Optional<AutomiProtocollo> findAutomaProtocolloByUoId(String uoid);
}
