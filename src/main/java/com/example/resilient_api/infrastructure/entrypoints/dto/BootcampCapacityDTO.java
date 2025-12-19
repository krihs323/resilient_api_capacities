package com.example.resilient_api.infrastructure.entrypoints.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class BootcampCapacityDTO {

    @Digits(integer = 3, fraction = 0, message = "Id de la capacidad invalido")
    @PositiveOrZero
    private Long idCapacity;


    @Digits(integer = 3, fraction = 0, message = "Id del bootcamp invalido")
    @PositiveOrZero
    private Long idBootcamp;

    //@JsonIgnore
    private Long id;
}
