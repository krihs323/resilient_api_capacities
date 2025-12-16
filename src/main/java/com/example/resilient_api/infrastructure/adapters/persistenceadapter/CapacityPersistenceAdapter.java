package com.example.resilient_api.infrastructure.adapters.persistenceadapter;

import com.example.resilient_api.domain.model.Capacity;
import com.example.resilient_api.domain.model.CapacityTechnology;
import com.example.resilient_api.domain.spi.CapacityPersistencePort;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.CapacityEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.CapacityTechnologyEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.CapacityEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.CapacityTechnologyEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.CapacityRepository;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.CapacityTechnologyRepository;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class CapacityPersistenceAdapter implements CapacityPersistencePort {
    private final CapacityRepository capacityRepository;
    private final CapacityEntityMapper capacityEntityMapper;

    private final CapacityTechnologyRepository capacityTechnologyRepository;
    private final CapacityTechnologyEntityMapper capacityTechnologyEntityMapper;


//    @Override
//    public Mono<Capacity> save(Capacity capacity) {
//        //TODO GUARDAR EL DETALLE
//        Mono<CapacityEntity> capacityEntitySaved = capacityRepository.save(capacityEntityMapper.toEntity(capacity));
//
//        List<CapacityTechnologyEntity> details = new java.util.ArrayList<>();
//        for (CapacityTechnology req : capacity.capacityTechnologyList()) {
//            CapacityTechnologyEntity capacityTechnologyEntity = new CapacityTechnologyEntity();
//            capacityTechnologyEntity.setId_capacity(capacityEntitySaved.flatMap(x-> x.getId());
//        }
//        //return capacityRepository.save(capacityEntityMapper.toEntity(capacity))
//        //        .map(capacityEntityMapper::toModel);
//    }

    @Override
    public Mono<Capacity> save(Capacity capacity) {
        return capacityRepository
                .save(capacityEntityMapper.toEntity(capacity))
                .flatMap(savedCapacityEntity -> {
                    List<CapacityTechnologyEntity> details = new ArrayList<>();
                    for (CapacityTechnology req : capacity.capacityTechnologyList()) {
                        CapacityTechnologyEntity detail = new CapacityTechnologyEntity();
                        detail.setId_capacity(savedCapacityEntity.getId());
                        detail.setId_tecnology(req.id_tecnology());
                        details.add(detail);
                    }
                    return capacityTechnologyRepository
                            .saveAll(details)
                            .then(Mono.just(savedCapacityEntity));
                })
                .map(capacityEntityMapper::toModel);
    }

    @Override
    public Mono<Boolean> existByName(String name) {
        return capacityRepository.findByName(name)
                .map(capacityEntityMapper::toModel)
                .map(capacity -> true)  // Si encuentra el usuario, devuelve true
                .defaultIfEmpty(false);  // Si no encuentra, devuelve false
    }

}
