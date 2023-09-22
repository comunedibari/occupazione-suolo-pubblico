package it.fincons.osp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import it.fincons.osp.dto.PraticaDTO;
import it.fincons.osp.model.StatoPratica;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.TipoProcesso;
import it.fincons.osp.model.Utente;

public interface PraticaRepository
		extends PagingAndSortingRepository<Pratica, Long>, JpaSpecificationExecutor<Pratica> {

	List<Pratica> findByUtenteCreazioneOrUtenteModificaOrUtentePresaInCarico(Utente utenteCreazione,
			Utente utenteModifica, Utente utentePresaInCarico);
	
	List<Pratica> findByIdPraticaOriginariaOrderByIdDesc(Long idPratica);
	List<Pratica> findByIdPraticaOriginariaAndStatoPraticaNotOrderByIdDesc(Long idPratica, StatoPratica statoPratica);
	List<Pratica> findByIdPraticaOriginariaAndStatoPraticaNotInOrderByIdDesc(Long idPratica, List<StatoPratica> statiPratica);
	
	List<Pratica> findByIdPraticaOriginariaAndTipoProcessoOrderByIdDesc(Long idPratica, TipoProcesso tipoProcesso);

	Optional<Pratica> findByIdProrogaPrecedente(Long idProrogaPrecedente);


	@Query(
		" SELECT p " +
		" FROM Pratica p " +
		" WHERE (p.id = :idPratica or p.idPraticaOriginaria = :idPratica or p.idProrogaPrecedente = :idPratica) and p.statoPratica.id not in (:statiPratica) and p.tipoProcesso.id = :idTipoProcesso "
	)
	List<Pratica> findPraticheByIdOrIdPraticaOriginariaAndStatoPraticaNotInAndTipoProcesso(Long idPratica, List<Integer> statiPratica, Integer idTipoProcesso);

	@Query("SELECT p FROM Pratica p WHERE p.statoPratica.id in (:idStatiPratica) AND p.codiceDetermina IS NULL")
	List<Pratica> findByStatoPraticaIdInAndCodiceDeterminaIsNull(@Param("idStatiPratica") List<Integer> idStatiPratica);

	@Query("SELECT p FROM Pratica p WHERE p.statoPratica.id in (:idStatiPratica)")
	List<Pratica> findByStatoPraticaIdIn(@Param("idStatiPratica") List<Integer> idStatiPratica);

	@Query("SELECT p1 FROM Pratica p1 WHERE p1.statoPratica.id in (:idStatiPratica) AND (SELECT COUNT(p2) FROM Pratica p2 WHERE p2.tipoProcesso.id = 2 AND p2.idPraticaOriginaria = p1.id) = 0")
	List<Pratica> findByStatoPraticaIdInAndNotExistProroga(@Param("idStatiPratica") List<Integer> idStatiPratica);

	@Query("SELECT p1 FROM Pratica p1 WHERE p1.statoPratica.id in (:idStatiPratica) and " +
			"(" +
			"	exists (select 'a' from Richiedente destinatario where destinatario.id=p1.destinatario.id and upper(destinatario.codiceFiscalePartitaIva)=upper(:codiceFiscalePiva)) " +
			"or exists (select 'a' from Richiedente firmatario where firmatario.id=p1.firmatario.id and upper(firmatario.codiceFiscalePartitaIva)=upper(:codiceFiscalePiva))" +
			")")
	List<Pratica> findPraticheByStatoPraticaAndCodiceFiscalePiva(@Param("idStatiPratica") List<Integer> idStatiPratica, @Param("codiceFiscalePiva") String codiceFiscalePiva);

	@Query("SELECT p1 FROM Pratica p1 WHERE p1.statoPratica.id in (:idStatiPratica) and p1.tipoProcesso.id = :idTipoProcesso and " +
			"(" +
			"	exists (select 'a' from Richiedente destinatario where destinatario.id=p1.destinatario.id and upper(destinatario.codiceFiscalePartitaIva)=upper(:codiceFiscalePiva)) " +
			"or exists (select 'a' from Richiedente firmatario where firmatario.id=p1.firmatario.id and upper(firmatario.codiceFiscalePartitaIva)=upper(:codiceFiscalePiva))" +
			")")
	List<Pratica> findPraticheByStatoPraticaAndCodiceFiscalePivaAndTipoProcesso(
		@Param("idStatiPratica") List<Integer> idStatiPratica,
		@Param("codiceFiscalePiva") String codiceFiscalePiva,
		Integer idTipoProcesso
	);
	@Query("SELECT count(p) FROM Pratica p WHERE p.statoPratica.id in (:idsStatiPratica) and " +
			"(:gruppoDestinatarioParere is null or exists (" +
			"											select" +
			"												'a'" +
			"											from" +
			"												RichiestaParere trp" +
			"											where" +
			"												trp.gruppoDestinatarioParere.id =:gruppoDestinatarioParere" +
			"												and trp.flagInseritaRisposta = false" +
			"												and p.id =trp.pratica.id " +
			"										)" +
			") and" +
			"((:idsMunicipi) is null or p.municipio.id in :idsMunicipi)")
	long getCountPratiche(@Param("idsStatiPratica") List<Integer> idsStatiPratica, @Param("gruppoDestinatarioParere") Integer gruppoDestinatarioParere, @Param("idsMunicipi") List<Integer> idsMunicipi);
}
