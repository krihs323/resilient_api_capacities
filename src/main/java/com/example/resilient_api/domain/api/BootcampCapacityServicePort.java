package com.example.resilient_api.domain.api;

import com.example.resilient_api.domain.model.BootcampCapacities;
import com.example.resilient_api.domain.model.BootcampCapacity;
import com.example.resilient_api.domain.model.Capacity;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampCapacityServicePort {
    Mono<BootcampCapacities> registerBootcampCapacity(BootcampCapacities bootcampCapacities, String messageId);
    Flux<BootcampCapacity> listCapacitiesBootcamp(int page, int size, String sortBy, String sortDir, String messageId);
    Flux<Capacity> listCapacitiesByBootcamp(Long idBootcamp, String messageId);
}
