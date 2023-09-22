package it.fincons.osp.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ProtocollazioneRestTemplateConfig {

    @Bean
    public RestTemplate restTemplateProtocollazione(RestTemplateBuilder builder) {

        return builder
                .setConnectTimeout(Duration.ofMillis(60000))
                .setReadTimeout(Duration.ofMillis(60000))
                .build();
    }
	
	
}