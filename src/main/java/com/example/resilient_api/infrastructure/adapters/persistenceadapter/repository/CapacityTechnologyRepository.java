package com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository;

import com.example.resilient_api.domain.model.CapacityList;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.CapacityTechnologyEntity;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CapacityTechnologyRepository extends ReactiveCrudRepository<CapacityTechnologyEntity, Long> {

     @Query("SELECT c.name AS name, \n" +
            "               COUNT(ct.id_tecnology) AS cantTechnologies \n" +
            "        FROM capacities c\n" +
            "        INNER JOIN capacities_x_tecnologies ct\n" +
            "            ON c.id = ct.id_capacity  \n" +
            "        GROUP BY c.id ")
    Flux<CapacityTechnologyReportDto> findCapacityByTechnology(
            int size,
            long offset
    );

    @Query("""
            SELECT COUNT(DISTINCT c.id)
                    FROM capacities c
                    INNER JOIN capacities_x_tecnologies ct
                        ON c.id = ct.id_capacity
        """)
    Mono<Long> countGroupedCapacities();

    @Query("SELECT c.name AS name, \n" +
            "               COUNT(ct.id_tecnology) AS cantTechnologies \n" +
            "        FROM capacities c\n" +
            "        INNER JOIN capacities_x_tecnologies ct\n" +
            "            ON c.id = ct.id_capacity  \n" +
            "        GROUP BY c.id ")
    Flux<CapacityList> getAll();
}
