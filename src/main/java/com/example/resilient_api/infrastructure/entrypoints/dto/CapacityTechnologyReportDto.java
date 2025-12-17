package com.example.resilient_api.infrastructure.entrypoints.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class CapacityTechnologyReportDto {

    private String name;
    private Long cantTechnologies;
}
