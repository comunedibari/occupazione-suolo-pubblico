package it.fincons.osp.config;

import lombok.*;
import org.springframework.boot.context.properties.*;
import org.springframework.stereotype.*;

import java.util.*;

@Component
@ConfigurationProperties(prefix = "osp.app")
@Data
public class OspConfiguration {
    private Map<String, String> dashboard;
    private Map<String, String> senderMailMunicipio;
}
