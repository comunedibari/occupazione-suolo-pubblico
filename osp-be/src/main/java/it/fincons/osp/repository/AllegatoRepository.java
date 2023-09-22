package it.fincons.osp.repository;

import java.util.List;

import it.fincons.osp.dto.AllegatoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.fincons.osp.model.Allegato;
import it.fincons.osp.model.Integrazione;
import it.fincons.osp.model.Parere;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.TipoAllegato;

public interface AllegatoRepository extends JpaRepository<Allegato, Long> {

	List<Allegato> findByPraticaAndTipoAllegatoAndNota(Pratica pratica, TipoAllegato tipoAllegato, String nota);

	List<Allegato> findByParereAndTipoAllegatoAndNota(Parere parere, TipoAllegato tipoAllegato, String nota);
	
	List<Allegato> findByParereInAndTipoAllegatoAndNota(List<Parere> listaPareri, TipoAllegato tipoAllegato, String nota);

	List<Allegato> findByIdRichiestaParereAndTipoAllegatoAndNota(Long idRichiestaParere, TipoAllegato tipoAllegato,
			String nota);

	List<Allegato> findByIntegrazioneAndTipoAllegatoAndNota(Integrazione integrazione, TipoAllegato tipoAllegato,
			String nota);

	List<Allegato> findByPraticaAndTipoAllegatoInOrderByDataInserimentoDescTipoAllegatoAscRevisioneDesc(Pratica pratica,
			List<TipoAllegato> tipoAllegato);

	@Query("SELECT a1 FROM Allegato a1 WHERE a1.pratica = :pratica AND a1.tipoAllegato IN (:tipiAllegati) AND a1.revisione = (SELECT MAX(a2.revisione) FROM Allegato a2 WHERE a2.pratica = a1.pratica AND a2.tipoAllegato = a1.tipoAllegato AND ((a2.nota IS NULL AND a1.nota IS NULL) OR (a2.nota = a1.nota)))")
	List<Allegato> findByPraticaAndTipoAllegatoWithMaxRevisione(@Param("pratica") Pratica pratica,
			@Param("tipiAllegati") List<TipoAllegato> tipiAllegati);

	List<Allegato> findByIdRichiestaIntegrazione(Long idRichiestaIntegrazione);

	List<Allegato> findByIdRichiestaParere(Long idRichiestaParere);

	List<Allegato> findByParere(Parere parere);

	List<Allegato> findByParereAndTipoAllegatoOrderByRevisioneDesc(Parere parere, TipoAllegato tipoAllegato);
	
	List<Allegato> findByPraticaAndTipoAllegatoOrderByRevisioneDesc(Pratica pratica, TipoAllegato tipoAllegato);

	List<Allegato> findByPraticaOrParereInOrderByDataInserimentoDescTipoAllegatoAscRevisioneDesc(Pratica pratica,
			List<Parere> pareri);

	@Query("SELECT a1 FROM Allegato a1, Pratica p1 WHERE a1.pratica.id=p1.id and p1.statoPratica.id=:idStatoPratica and a1.pratica.id = :idPratica AND a1.tipoAllegato.id IN (select t1.id from TipoAllegato t1 where t1.id in (:idTipiAllegati))")
	List<Allegato> findDeterminaByPraticaTipoAllegatoAndStato(@Param("idPratica")Long idPratica, @Param("idTipiAllegati") List<Integer> idTipiAllegati, @Param("idStatoPratica")Integer idStatoPratica);

	@Query("SELECT a1 FROM Allegato a1, Pratica p1 WHERE a1.pratica.id=p1.id and a1.pratica.id = :idPratica AND a1.tipoAllegato.id IN (select t1.id from TipoAllegato t1 where t1.id in (:idTipiAllegati))")
	List<Allegato> findDeterminaByPraticaAndTipoAllegato(@Param("idPratica")Long idPratica, @Param("idTipiAllegati") List<Integer> idTipiAllegati);

	@Query("SELECT a1 FROM Allegato a1, Pratica p1 WHERE a1.pratica.id=p1.id and p1.id = :idPratica AND p1.statoPratica.id = :idStatoPratica AND a1.revisione = (SELECT MAX(a2.revisione) FROM Allegato a2 WHERE a2.pratica.id=a1.pratica.id AND a2.tipoAllegato.id = a1.tipoAllegato.id AND ((a2.nota IS NULL AND a1.nota IS NULL) OR (a2.nota = a1.nota)))")
	List<Allegato> findByPraticaAndStatoPratica(@Param("idPratica")Long idPratica, @Param("idStatoPratica")Integer idStatoPratica);

	List<Allegato> findAllByPratica_Id(Long id);

}
