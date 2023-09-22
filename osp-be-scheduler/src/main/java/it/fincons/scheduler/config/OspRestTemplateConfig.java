package it.fincons.scheduler.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OspRestTemplateConfig {

    @Bean
    public RestTemplate restTemplateOsp(RestTemplateBuilder builder) {

        return builder
                .setConnectTimeout(Duration.ofMillis(120000))
                .setReadTimeout(Duration.ofMillis(120000))
                .build();
    }
	
	
}