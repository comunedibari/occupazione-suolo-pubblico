package it.fincons.osp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.fincons.osp.model.Gruppo;
import it.fincons.osp.model.StatoPratica;
import it.fincons.osp.model.TipoAllegatoGruppoStatoProcesso;
import it.fincons.osp.model.TipoProcesso;

public interface TipoAllegatoGruppoStatoProcessoRepository
		extends JpaRepository<TipoAllegatoGruppoStatoProcesso, Integer> {

	List<TipoAllegatoGruppoStatoProcesso> findByStatoPraticaAndTipoProcessoAndGruppo(StatoPratica statoPratica, TipoProcesso tipoProcesso,
			Gruppo gruppo);
	
	List<TipoAllegatoGruppoStatoProcesso> findByStatoPraticaAndTipoProcessoAndGruppoAndFlagObbligatorioTrue(StatoPratica statoPratica, TipoProcesso tipoProcesso,
			Gruppo gruppo);

	List<TipoAllegatoGruppoStatoProcesso> findByStatoPraticaAndTipoProcesso(StatoPratica statoPratica, TipoProcesso tipoProcesso);

	List<TipoAllegatoGruppoStatoProcesso> findByStatoPratica(StatoPratica statoPratica);

	List<TipoAllegatoGruppoStatoProcesso> findByTipoProcesso(TipoProcesso tipoProcesso);
}
