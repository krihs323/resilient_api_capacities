package com.example.resilient_api.infrastructure.entrypoints.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class TechnologyDTO {

    @Digits(integer = 3, fraction = 0, message = "Id de la tecnologia invalida")
    @PositiveOrZero
    private Long idTechnology;

}
