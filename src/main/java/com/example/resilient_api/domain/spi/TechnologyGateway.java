package com.example.resilient_api.domain.spi;

import com.example.resilient_api.domain.model.Technology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TechnologyGateway {
    Flux<Technology> getTechnologiesByCapacity(String idCapacity, String messageId);

    Mono<Boolean> deleteTechnologyByCapacity(int id, String messageId);
}
