package com.example.resilient_api.domain.spi;

import com.example.resilient_api.domain.model.BootcampCapacity;
import com.example.resilient_api.domain.model.Capacity;
import com.example.resilient_api.domain.model.CapacityList;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CapacityPersistencePort {
    Mono<Capacity> save(Capacity capacity);
    Mono<Boolean> existByName(String name);
    Flux<CapacityTechnologyReportDto> listCapacitiesPage(int page, int size, String sortBy, String sortDir, String messageId);
    Mono<Long> countGroupedCapacities();
    Flux<CapacityList> findCapabilitiesOrderedByName(int page, int size, String sortBy, String sortDir, String messageId);
    Mono<Boolean> getCapacitiesInOtherBootcamps(Long idBootcamp, String messageId);
    Flux<BootcampCapacity> getCapacitiesByBootcamp(Long idBootcamp, String messageId);
    Mono<Void> deleteCapacitiesByBootcamp(Long idBootcamp, String messageId);
    Mono<Void> deleteAllCapacitiesyBootcamp(Long idBootcamp, String messageId);
}
