package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.api.BootcampCapacityServicePort;
import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.*;
import com.example.resilient_api.domain.spi.BootcampCapacityPersistencePort;
import com.example.resilient_api.domain.spi.TechnologyGateway;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class BootcampCapacityUseCase implements BootcampCapacityServicePort {

    private final BootcampCapacityPersistencePort bootcampCapacityPersistencePort;
    private final TechnologyGateway technologyGateway;

    public BootcampCapacityUseCase(BootcampCapacityPersistencePort bootcampCapacityPersistencePort, TechnologyGateway technologyGateway) {
        this.bootcampCapacityPersistencePort = bootcampCapacityPersistencePort;
        this.technologyGateway = technologyGateway;
    }

    @Override
    public Mono<BootcampCapacities> registerBootcampCapacity(BootcampCapacities bootcampCapacities, String messageId) {
        return bootcampCapacityPersistencePort.existByIdBootcamp(bootcampCapacities.idBootcamp())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.CAPACITY_ALREADY_EXISTS)))
                .flatMap(exists -> validateDuplicate(bootcampCapacities.bootcampCapacityList()))
                .flatMap(x-> bootcampCapacityPersistencePort.save(bootcampCapacities));
    }

    @Override
    public Flux<BootcampCapacity> listCapacitiesBootcamp(int page, int size, String sortBy, String sortDir, String messageId) {
        return bootcampCapacityPersistencePort.getAll(page, size, sortBy, sortDir, messageId);
    }

    @Override
    public Flux<CapacityTechnologies> listCapacitiesByBootcamp(Long idBootcamp, String messageId) {
        //TODO AGREGARLE LAS TECNOLOGIAS POR CADA CAPACIDAD ENCONTRADA EN LA CONSULTA

        return bootcampCapacityPersistencePort.getCapacitiesByBootcamp(idBootcamp, messageId)
                .flatMap(capacity ->
                    technologyGateway.getTechnologiesByCapacity(String.valueOf(capacity.id()), messageId)
                            .collectList()
                            .map(technologies -> new CapacityTechnologies(
                                    capacity.id(),
                                    capacity.name(),
                                    capacity.description(),
                                    technologies
                            )).doOnNext(x->log.info("tecnologias: "+x.capacityTechnologiesList().toString()))
                    );
    }

    private Mono<Boolean> validateDuplicate(List<BootcampCapacity> bootcampCapacityList) {
        Set<BootcampCapacity> uniqueCapacities = new HashSet<>(bootcampCapacityList);
        if (bootcampCapacityList.size() != uniqueCapacities.size()) {
            return Mono.error(new BusinessException(TechnicalMessage.CAPACITY_DUPLICATE_IN_LIST));
        } else {
            return Mono.just(Boolean.FALSE);
        }
    }


}
