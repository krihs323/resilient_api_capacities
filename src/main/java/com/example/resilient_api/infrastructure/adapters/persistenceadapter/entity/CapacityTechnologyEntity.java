package com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@RequiredArgsConstructor
public class CapacityTechnologyEntity {
    @Id
    private Long id;
    private Long id_tecnology;
    private Long id_capacity;
}
