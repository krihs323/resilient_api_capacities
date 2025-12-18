package com.example.resilient_api.infrastructure.entrypoints.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class BootcampCapacitiesDTO {

    @Digits(integer = 3, fraction = 0, message = "Id del bootcamp invalido")
    @PositiveOrZero
    private Long idBootcamp;

    @Size.List({
            @Size(min = 1, message = "Minimo 1 capacidad"),
            @Size(max = 4, message = "maximo 4 capacidades")
    })
    private List<BootcampCapacityDTO> bootcampCapacityList;


}
