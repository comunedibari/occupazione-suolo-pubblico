package it.fincons.ospscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;

@SpringBootApplication(exclude = ElasticsearchDataAutoConfiguration.class)
public class OspSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OspSchedulerApplication.class, args);
	}

}
