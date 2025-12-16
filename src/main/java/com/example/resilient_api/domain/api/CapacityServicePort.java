package com.example.resilient_api.domain.api;

import com.example.resilient_api.domain.model.Capacity;
import reactor.core.publisher.Mono;

public interface CapacityServicePort {
    Mono<Capacity> registerCapacity(Capacity capacity, String messageId);
}
