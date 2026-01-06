package com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository;

import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampCapacityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface BootcampCapacityRepository extends ReactiveCrudRepository<BootcampCapacityEntity, Long> {
    Mono<BootcampCapacityEntity> findByIdBootcamp(Long idBootcamp);
}
