package com.example.resilient_api.domain.api;

import com.example.resilient_api.domain.model.BootcampCapacities;
import reactor.core.publisher.Mono;

public interface BootcampCapacityServicePort {
    Mono<BootcampCapacities> registerBootcampCapacity(BootcampCapacities bootcampCapacities, String messageId);

}
