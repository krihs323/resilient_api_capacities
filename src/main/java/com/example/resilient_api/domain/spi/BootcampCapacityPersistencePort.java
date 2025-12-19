package com.example.resilient_api.domain.spi;

import com.example.resilient_api.domain.model.BootcampCapacities;
import com.example.resilient_api.domain.model.BootcampCapacity;
import com.example.resilient_api.domain.model.Capacity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampCapacityPersistencePort {
    Mono<BootcampCapacities> save(BootcampCapacities bootcampCapacities);
    Mono<Boolean> existByIdBootcamp(Long idBootcamp);
    Flux<BootcampCapacity> getAll(int page, int size, String sortBy, String sortDir, String messageId);
}
