package it.fincons.osp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class ActuatorInfoContributor implements InfoContributor {
    @Autowired
    BuildProperties buildProperties;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("spring-boot.version", SpringBootVersion.getVersion());
    }
}