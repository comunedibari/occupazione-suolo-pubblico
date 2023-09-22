package it.fincons.ospscheduler.service;

import it.fincons.ospscheduler.elasticsearch.model.ElPratica;
import it.fincons.ospscheduler.elasticsearch.repository.ElPraticaRepository;
import it.fincons.ospscheduler.model.PraticaSearch;
import it.fincons.ospscheduler.model.Utente;
import it.fincons.ospscheduler.repository.PraticaRepository;
import it.fincons.ospscheduler.util.GeometryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoadDataService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${elasticsearch.indexName}")
    private String indexName = "osp";

    private final PraticaRepository praticaRepository;
    private final ElPraticaRepository elPraticaRepository;

    private final ElasticsearchTemplate elasticsearchTemplate;

    public LoadDataService(PraticaRepository praticaRepository, ElPraticaRepository elPraticaRepository, ElasticsearchTemplate elasticsearchTemplate) {
        this.praticaRepository = praticaRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.elPraticaRepository = elPraticaRepository;
    }

    @Transactional
    public void executeSampleJob() throws IOException {

        logger.info("Inizio caricamento dati");

        List<PraticaSearch> listaPratiche = praticaRepository.findPratiche();

        if (listaPratiche != null && listaPratiche.size() > 0) {

            if(!elasticsearchTemplate.indexOps(IndexCoordinates.of(indexName)).exists()){
                logger.info("creo indice :: "+indexName);

                Map<String, Object> settings = new HashMap<>();
                settings.put("number_of_shards", 1);
                settings.put("number_of_replicas", 1);

                Document mapping = Document.create().fromJson(
                        "{\n" +
                                "    \"properties\": {\n" +
                                "        \"coord_ubicazione_temporanea\": {\n" +
                                "            \"type\": \"geo_shape\"\n" +
                                "        },\n" +
                                "        \"coord_ubicazione_definitiva\": {\n" +
                                "            \"type\": \"geo_shape\"\n" +
                                "        }\n" +
                                "    }\n" +
                                "}"
                );

                elasticsearchTemplate.indexOps(IndexCoordinates.of("osp")).create(settings, mapping);
            }

            logger.info("Numero pratiche da elaborare::"+listaPratiche.size());

            listaPratiche.forEach(pratica -> {
                ElPratica elPratica=ElPratica.builder()
                        .id(pratica.getId())
                        .idMunicipio(pratica.getIdMunicipio())
                        .idTipoRuoloRichiedente(pratica.getIdTipoRuoloRichiedente())
                        .dataInserimento(pratica.getDataInserimento())
                        .dataInizioOccupazione(pratica.getDataInizioOccupazione())
                        .dataScadenzaOccupazione(pratica.getDataScadenzaOccupazione())
                        .oraInizioOccupazione(pratica.getOraInizioOccupazione())
                        .oraScadenzaOccupazione(pratica.getOraScadenzaOccupazione())
                        .idTipoAttivitaDaSvolgere(pratica.getIdTipoAttivitaDaSvolgere())
                        .idStatoPratica(pratica.getIdStatoPratica())
                        .idTipoProcesso(pratica.getIdTipoProcesso())
                        .coordUbicazioneTemporanea(GeometryUtil.geometryToWKTFormat(pratica.getCoordUbicazioneTemporanea()))
                        .coordUbicazioneDefinitiva(GeometryUtil.geometryToWKTFormat(pratica.getCoordUbicazioneDefinitiva()))
                        .istruttoreResponsabile(pratica.getUtentePresaInCarico()!=null?pratica.getUtentePresaInCarico().getNome()+" "+pratica.getUtentePresaInCarico().getCognome():"")
                        .build();
                elPraticaRepository.save(elPratica);
            });
        }

        logger.info("Fine caricamento dati");

    }

}
