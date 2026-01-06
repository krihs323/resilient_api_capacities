package com.example.resilient_api.infrastructure.adapters.persistenceadapter;

import com.example.resilient_api.domain.model.BootcampCapacities;
import com.example.resilient_api.domain.model.BootcampCapacity;
import com.example.resilient_api.domain.model.Capacity;
import com.example.resilient_api.domain.spi.BootcampCapacityPersistencePort;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampCapacityEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.BootcampCapacityEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampCapacityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class BootcampCapacityPersistenceAdapter implements BootcampCapacityPersistencePort {
    private final BootcampCapacityRepository bootcampCapacityRepository;
    private final BootcampCapacityEntityMapper bootcampCapacityEntityMapper;

    private final DatabaseClient databaseClient;

    @Override
    public Mono<BootcampCapacities> save(BootcampCapacities bootcampCapacity) {

        List<BootcampCapacityEntity> details = new ArrayList<>();
        for (BootcampCapacity req : bootcampCapacity.bootcampCapacityList()) {
            BootcampCapacityEntity detail = new BootcampCapacityEntity();
            detail.setIdBootcamp(bootcampCapacity.idBootcamp());
            detail.setIdCapacity(req.idCapacity());
            details.add(detail);
        }

        return bootcampCapacityRepository
                .saveAll(details)
                .then(Mono.just(bootcampCapacity));
    }


    @Override
    public Mono<Boolean> existByIdBootcamp(Long idBootcamp) {
        return bootcampCapacityRepository.findByIdBootcamp(idBootcamp)
                .map(bootcampCapacityEntityMapper::toModel)
                .map(bootcampCapacity -> true)  // Si encuentra el usuario, devuelve true
                .defaultIfEmpty(false);  // Si no encuentra, devuelve false
    }

    @Override
    public Flux<BootcampCapacity> getAll(int page, int size, String sortBy, String sortDir, String messageId) {
        return bootcampCapacityRepository.findAll().map(bootcampCapacityEntityMapper::toModel);
    }

    @Override
    public Flux<Capacity> getCapacitiesByBootcamp(Long idBootcamp, String messageId) {
        String sql = """
            select capacities.id as id_capacity, capacities.name as name, capacities.description as description from capacities_x_bootcamps inner join capacities on
            capacities_x_bootcamps.id_capacity  = capacities.id
            where capacities_x_bootcamps.id_bootcamp = %s
            ORDER BY capacities_x_bootcamps.id_bootcamp, NAME ASC
            """.formatted(idBootcamp);
        return databaseClient.sql(sql)
                .map((row, meta) -> new Capacity(
                        row.get("id_capacity", Long.class),
                        row.get("name", String.class),
                        row.get("description", String.class),
                        new ArrayList<>()
                ))
                .all();
    }

}
