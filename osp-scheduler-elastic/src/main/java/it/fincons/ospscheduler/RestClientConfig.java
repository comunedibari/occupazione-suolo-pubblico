package it.fincons.ospscheduler;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;


@EnableElasticsearchRepositories(basePackages = "it.fincons.ospscheduler.elasticsearch.repository")
@ComponentScan(basePackages = { "it.fincons.ospscheduler.elasticsearch" })
@Configuration
public class RestClientConfig extends ElasticsearchConfiguration{

    @Value("${elasticsearch.hostAndPort}")
    private String hostAndPort;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder() //
                .connectedTo(hostAndPort) //
                .build();
    }

}
