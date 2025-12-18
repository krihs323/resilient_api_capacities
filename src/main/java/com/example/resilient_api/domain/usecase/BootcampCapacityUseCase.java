package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.api.BootcampCapacityServicePort;
import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.*;
import com.example.resilient_api.domain.spi.BootcampCapacityPersistencePort;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BootcampCapacityUseCase implements BootcampCapacityServicePort {

    private final BootcampCapacityPersistencePort bootcampCapacityPersistencePort;

    public BootcampCapacityUseCase(BootcampCapacityPersistencePort bootcampCapacityPersistencePort) {
        this.bootcampCapacityPersistencePort = bootcampCapacityPersistencePort;
    }

    @Override
    public Mono<BootcampCapacities> registerBootcampCapacity(BootcampCapacities bootcampCapacities, String messageId) {
        return bootcampCapacityPersistencePort.existByIdBootcamp(bootcampCapacities.idBootcamp())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.CAPACITY_ALREADY_EXISTS)))
                .flatMap(exists -> validateDuplicate(bootcampCapacities.bootcampCapacityList()))
                .flatMap(x-> bootcampCapacityPersistencePort.save(bootcampCapacities));
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
