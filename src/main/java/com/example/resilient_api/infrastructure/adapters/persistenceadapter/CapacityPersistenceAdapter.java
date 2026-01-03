package com.example.resilient_api.infrastructure.adapters.persistenceadapter;

import com.example.resilient_api.domain.model.*;
import com.example.resilient_api.domain.spi.CapacityPersistencePort;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.CapacityTechnologyEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.CapacityEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.CapacityTechnologyEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.CapacityRepository;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.CapacityTechnologyRepository;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import com.example.resilient_api.infrastructure.entrypoints.mapper.CapacityListMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
            """.formatted(sortBy, sortDir);
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

    @Override
    public Mono<Boolean> getCapacitiesInOtherBootcamps(Long idBootcamp, String messageId) {
        String sql = """
                select id from tecnologias.capacities_x_bootcamps where id_bootcamp <> %s
                    and id_capacity in (select id_capacity  from tecnologias.capacities_x_bootcamps where id_bootcamp = %s) limit 1
            """.formatted(idBootcamp, idBootcamp);
        return databaseClient.sql(sql)
                .map((row, meta) -> true)
                .first()
                .defaultIfEmpty(false);
    }

    @Override
    public Flux<BootcampCapacity> getCapacitiesByBootcamp(Long idBootcamp, String messageId) {
        String sql = """
            select id, id_bootcamp, id_capacity from tecnologias.capacities_x_bootcamps where id_bootcamp = %s
            """.formatted(idBootcamp);
        return databaseClient.sql(sql)
                .map((row, meta) -> new BootcampCapacity(
                        row.get("id", Long.class),
                        row.get("id_capacity", Long.class),
                        row.get("id_bootcamp", Long.class)
                ))
                .all();
    }

    @Override
    public Mono<Void> deleteCapacitiesByBootcamp(Long idBootcamp, String messageId) {
        // Preparar el SQL
        String sql = """
                DELETE FROM tecnologias.capacities WHERE id in (
                    select id_capacity from tecnologias.capacities_x_bootcamps where id_bootcamp = %s group by id_capacity)
                """
                .formatted(idBootcamp);
        log.info("Executing delete for capacity idBootcamp: {} for messageId: {}", idBootcamp, messageId);
        // Ejecutar y retornar Mono<Void>
        return databaseClient.sql(sql)
                .fetch()
                .rowsUpdated() // Retorna la cantidad de filas afectadas (Mono<Long>)
                .doOnNext(rows -> log.info("Successfully deleted {} rows for messageId: {}", rows, messageId))
                .then(); // Transformamos el Mono<Long> en Mono<Void>
    }

    @Override
    public Mono<Void> deleteAllCapacitiesyBootcamp(Long idBootcamp, String messageId) {
        // Preparar el SQL
        String sql = """
                DELETE FROM tecnologias.capacities_x_bootcamps WHERE id_bootcamp = %s
                """
                .formatted(idBootcamp);
        log.info("Executing delete for capacities_x_bootcamps idBootcamp: {} for messageId: {}", idBootcamp, messageId);
        // Ejecutar y retornar Mono<Void>
        return databaseClient.sql(sql)
                .fetch()
                .rowsUpdated() // Retorna la cantidad de filas afectadas (Mono<Long>)
                .doOnNext(rows -> log.info("Successfully deleted {} rows for messageId: {}", rows, messageId))
                .then(); // Transformamos el Mono<Long> en Mono<Void>
    }
}
