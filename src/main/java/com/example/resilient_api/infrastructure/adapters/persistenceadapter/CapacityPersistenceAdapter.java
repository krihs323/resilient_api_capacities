package com.example.resilient_api.infrastructure.adapters.persistenceadapter;

import com.example.resilient_api.domain.model.Capacity;
import com.example.resilient_api.domain.model.CapacityList;
import com.example.resilient_api.domain.model.CapacityTechnology;
import com.example.resilient_api.domain.model.PageResponse;
import com.example.resilient_api.domain.spi.CapacityPersistencePort;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.CapacityEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.CapacityTechnologyEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.CapacityEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.CapacityTechnologyEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.CapacityRepository;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.CapacityTechnologyRepository;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import com.example.resilient_api.infrastructure.entrypoints.mapper.CapacityListMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class CapacityPersistenceAdapter implements CapacityPersistencePort {
    private final CapacityRepository capacityRepository;
    private final CapacityEntityMapper capacityEntityMapper;

    private final CapacityTechnologyRepository capacityTechnologyRepository;
    private final CapacityTechnologyEntityMapper capacityTechnologyEntityMapper;

    private final CapacityListMapper capacityListMapper;

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Capacity> save(Capacity capacity) {
        return capacityRepository
                .save(capacityEntityMapper.toEntity(capacity))
                .flatMap(savedCapacityEntity -> {
                    List<CapacityTechnologyEntity> details = new ArrayList<>();
                    for (CapacityTechnology req : capacity.capacityTechnologyList()) {
                        CapacityTechnologyEntity detail = new CapacityTechnologyEntity();
                        detail.setId_capacity(savedCapacityEntity.getId());
                        detail.setId_tecnology(req.id_tecnology());
                        details.add(detail);
                    }
                    return capacityTechnologyRepository
                            .saveAll(details)
                            .then(Mono.just(savedCapacityEntity));
                })
                .map(capacityEntityMapper::toModel);
    }

    @Override
    public Mono<Boolean> existByName(String name) {
        return capacityRepository.findByName(name)
                .map(capacityEntityMapper::toModel)
                .map(capacity -> true)  // Si encuentra el usuario, devuelve true
                .defaultIfEmpty(false);  // Si no encuentra, devuelve false
    }


    @Override
    public Flux<CapacityTechnologyReportDto> listCapacitiesPage(int page, int size, String sortBy, String sortDir, String messageId) {

        String sql = """
            select capacities.description as name, count(capacities.id) as cantTechnologies from capacities_x_tecnologies inner join capacities on\s
                            capacities_x_tecnologies.id_capacity  = capacities.id
                            GROUP by capacities.id
                            ORDER BY %s %s
            LIMIT :limit OFFSET :offset
            """.formatted(sortBy, sortDir);;
        return databaseClient.sql(sql)
                .bind("limit", size )
                .bind("offset", page)
                .map((row, meta) -> new CapacityTechnologyReportDto(
                        row.get("name", String.class),
                        row.get("cantTechnologies", Long.class)
                ))
                .all();
    }

    @Override
    public Mono<Long> countGroupedCapacities() {
        return capacityTechnologyRepository.countGroupedCapacities();
    }

    @Override
    public Flux<CapacityList> findCapabilitiesOrderedByName(
            int offset,
            int limit,
            String sortBy, String sortDir, String messageId
    ) {
        String sql = """
            select capacities.description as name, count(capacities.id) as cantTechnologies from capacities_x_tecnologies inner join capacities on\s
                            capacities_x_tecnologies.id_capacity  = capacities.id
                            GROUP by capacities.id
            LIMIT :limit OFFSET :offset
            """;
        return databaseClient.sql(sql)
                .bind("limit", limit)
                .bind("offset", offset)
                .map((row, meta) -> new CapacityList(
                        row.get("name", String.class),
                        row.get("cantTechnologies", Long.class)
                ))
                .all();
    }

}
