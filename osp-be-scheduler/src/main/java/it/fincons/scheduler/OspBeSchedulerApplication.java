package it.fincons.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class OspBeSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OspBeSchedulerApplication.class, args);
	}

}
