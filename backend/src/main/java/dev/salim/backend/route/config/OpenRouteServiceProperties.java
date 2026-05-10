package dev.salim.backend.route.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "openrouteservice")
public class OpenRouteServiceProperties {

    private boolean enabled;
    private String apiKey = "";
    private String baseUrl = "https://api.openrouteservice.org";
}
