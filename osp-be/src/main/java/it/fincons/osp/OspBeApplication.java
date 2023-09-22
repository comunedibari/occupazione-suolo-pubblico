package it.fincons.osp;

import java.util.Locale;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SpringBootApplication
@EnableAutoConfiguration
@OpenAPIDefinition(info = @Info(title = "OSP API", version = "1.0", description = "Gestionale Occupazione Suolo Pubblico Bari"))
@SecurityScheme(name = "jwt", scheme = "Bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@EnableAsync
public class OspBeApplication {

	public static void main(String[] args) {
		init();
		SpringApplication.run(OspBeApplication.class, args);
	}
	
    public static void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Rome"));
        Locale.setDefault(Locale.ITALY);
    }

}
