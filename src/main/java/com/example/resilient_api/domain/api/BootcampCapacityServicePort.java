package com.example.resilient_api.domain.api;

import com.example.resilient_api.domain.model.*;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampCapacityServicePort {
    Mono<BootcampCapacities> registerBootcampCapacity(BootcampCapacities bootcampCapacities, String messageId);
    Flux<BootcampCapacity> listCapacitiesBootcamp(int page, int size, String sortBy, String sortDir, String messageId);
    Flux<CapacityTechnologies> listCapacitiesByBootcamp(Long idBootcamp, String messageId);
}
