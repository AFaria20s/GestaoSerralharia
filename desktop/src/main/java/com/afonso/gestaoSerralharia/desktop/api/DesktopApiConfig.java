package com.afonso.gestaoSerralharia.desktop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DesktopApiConfig {

    @Bean
    RestClient desktopRestClient(RestClient.Builder builder,
                                 @Value("${desktop.api.base-url:http://localhost:8080}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    DesktopApiSupport desktopApiSupport(RestClient desktopRestClient, ObjectMapper objectMapper) {
        return new DesktopApiSupport(desktopRestClient, objectMapper);
    }
}
