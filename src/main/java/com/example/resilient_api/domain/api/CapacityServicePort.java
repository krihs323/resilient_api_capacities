package com.example.resilient_api.domain.api;

import com.example.resilient_api.domain.model.Capacity;
import com.example.resilient_api.domain.model.CapacityList;
import com.example.resilient_api.domain.model.PageResponse;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CapacityServicePort {
    Mono<Capacity> registerCapacity(Capacity capacity, String messageId);

    Mono<PageResponse<CapacityTechnologyReportDto>> listCapacitiesNoPage(int page, int size, String sortBy, String sortDir, String messageId);


}
