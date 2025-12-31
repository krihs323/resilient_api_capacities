package com.example.resilient_api.infrastructure.adapters.technologyapiadapter.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("technology-api")
public class TechnologyApiProperties {
    private String baseUrl;
    private String apiKey;
    private String timeout;
}
