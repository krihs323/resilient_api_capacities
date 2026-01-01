package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.*;
import com.example.resilient_api.domain.spi.CapacityPersistencePort;
import com.example.resilient_api.domain.api.CapacityServicePort;
import com.example.resilient_api.domain.spi.TechnologyGateway;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CapacityUseCase implements CapacityServicePort {

    private final CapacityPersistencePort capacityPersistencePort;
    private final TechnologyGateway technologyGateway;

    public CapacityUseCase(CapacityPersistencePort capacityPersistencePort, TechnologyGateway technologyGateway) {
        this.capacityPersistencePort = capacityPersistencePort;
        this.technologyGateway = technologyGateway;
    }

    @Override
    public Mono<Capacity> registerCapacity(Capacity capacity, String messageId) {
        return capacityPersistencePort.existByName(capacity.name())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.CAPACITY_ALREADY_EXISTS)))
                .flatMap(exists -> validateDuplicate(capacity.capacityTechnologyList()))
                .flatMap(x-> capacityPersistencePort.save(capacity));
    }



    @Override
    public Mono<PageResponse<CapacityTechnologyReportDto>> listCapacitiesPage(int page, int size, String sortBy, String sortDir, String messageId) {
        var data = capacityPersistencePort.listCapacitiesPage(page, size, sortBy, sortDir, messageId).collectList();
        var total = capacityPersistencePort.countGroupedCapacities();

        return Mono.zip(data, total)
                .map(tuple -> new PageResponse<>(
                        tuple.getT1(),
                        tuple.getT2(),
                        page,
                        size
                ));
    }

    @Override
    public Flux<CapacityList> listCapacities(int page, int size, String sortBy, String sortDir, String messageId) {
        return capacityPersistencePort.findCapabilitiesOrderedByName(page, size, sortBy, sortDir, messageId);
    }

    @Override
    public Mono<Void> deleteCapacityByBootcamp(int id, String messageId) {
        //llamar a borrar tecnologias
        //si se borra entonces borrar capacidades
        technologyGateway.deleteTechnologyByCapacity(id, messageId);
        //si no se borra la capacidad por asociacion, generar error y no borrar
        return null;
    }


    private Mono<Boolean> validateDuplicate(List<CapacityTechnology> capacityTechnologies) {
        Set<CapacityTechnology> uniqueCapacities = new HashSet<>(capacityTechnologies);
        if (capacityTechnologies.size() != uniqueCapacities.size()) {
            return Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_DUPLICATE_IN_LIST));
        } else {
            return Mono.just(Boolean.FALSE);
        }
    }


}
