package com.example.resilient_api.domain.spi;

import com.example.resilient_api.domain.model.Capacity;
import reactor.core.publisher.Mono;

public interface CapacityPersistencePort {
    Mono<Capacity> save(Capacity capacity);
    Mono<Boolean> existByName(String name);
}
