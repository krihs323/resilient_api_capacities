package com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository;

import com.example.resilient_api.domain.model.Capacity;
import com.example.resilient_api.domain.model.CapacityList;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.CapacityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface CapacityRepository extends ReactiveCrudRepository<CapacityEntity, Long> {
    Mono<CapacityEntity> findByName(String name);


}
