package com.example.resilient_api.infrastructure.adapters.persistenceadapter;

import com.example.resilient_api.domain.model.BootcampCapacities;
import com.example.resilient_api.domain.model.BootcampCapacity;
import com.example.resilient_api.domain.spi.BootcampCapacityPersistencePort;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampCapacityEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.BootcampCapacityEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampCapacityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class BootcampCapacityPersistenceAdapter implements BootcampCapacityPersistencePort {
    private final BootcampCapacityRepository bootcampCapacityRepository;
    private final BootcampCapacityEntityMapper bootcampCapacityEntityMapper;

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

}
