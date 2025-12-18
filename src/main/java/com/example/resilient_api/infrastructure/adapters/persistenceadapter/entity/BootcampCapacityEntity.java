package com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "capacities_x_bootcamps")
@Getter
@Setter
@RequiredArgsConstructor
public class BootcampCapacityEntity {
    @Id
    private Long id;
    @Column("id_bootcamp")
    private Long idBootcamp;
    @Column("id_capacity")
    private Long idCapacity;
}
