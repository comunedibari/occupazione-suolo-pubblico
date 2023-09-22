package it.fincons.ospscheduler.elasticsearch.repository;


import it.fincons.ospscheduler.elasticsearch.model.ElPratica;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElPraticaRepository extends ElasticsearchRepository<ElPratica, String> {
}
