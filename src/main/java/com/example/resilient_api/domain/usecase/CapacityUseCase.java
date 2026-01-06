package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.*;
import com.example.resilient_api.domain.spi.CapacityPersistencePort;
import com.example.resilient_api.domain.api.CapacityServicePort;
import com.example.resilient_api.domain.spi.TechnologyGateway;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.CapacityTechnologyEntity;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
                .flatMap(x-> capacityPersistencePort.save(capacity))
                .flatMap(savedCapacityEntity -> {
                    List<CapacityTechnologyEntity> details = new ArrayList<>();
                    for (CapacityTechnology req : capacity.capacityTechnologyList()) {
                        CapacityTechnologyEntity detail = new CapacityTechnologyEntity();
                        detail.setId_capacity(savedCapacityEntity.id());
                        detail.setId_tecnology(req.id_tecnology());
                        details.add(detail);
                    }
                    return technologyGateway
                            .saveAll(details, messageId)
                            .then(Mono.just(savedCapacityEntity));
                });
    }

    @Override
    public Mono<PageResponse<CapacityTechnologyReportDto>> listCapacitiesPage(int page, int size, String sortBy, String sortDir, String messageId) {
        Mono<Long> total = capacityPersistencePort.countCapacities();

        Mono<List<Capacity>> data = capacityPersistencePort.listCapacitiesPage(page, size, sortBy, sortDir, messageId)
                .collectList()
                .doOnNext(list -> {
                    log.info("DEBUG - Lista de capacidades recuperadas:");
                    list.forEach(capacity -> log.info(" > " + capacity));
                    log.info("Total elementos en página: " + list.size());
                });

        //Obtenemos todas las capacidades y las agrupamos por idCapacity en un Map
        // Esto optimiza la búsqueda: K = idCapacity, V = cantidad de ocurrencias
        Mono<Map<Long, Long>> technologiesCountMapMono = technologyGateway.getAllTecnologies(messageId)
                .filter(cap -> cap.id_capacity() != null)
                .collect(Collectors.groupingBy(
                        CapacityTechnology::id_capacity,
                        Collectors.counting()
                )).doOnNext(list -> {
                    log.info("DEBUG - Lista de tecnologias recuperada:");
                    log.info(" > " + list);
                    log.info("Total elementos en página: " + list.size());
                });

        //Combinamos ambos Monos y transformamos
        return Mono.zip(data, technologiesCountMapMono, total)
                .map(tuple -> {
                    List<Capacity> capacities = tuple.getT1();
                    Map<Long, Long> countsMap = tuple.getT2();
                    Long totalCapacities = tuple.getT3();

                    // Transformamos la lista de Capacities a CapacityTechnologyReportDto
                    List<CapacityTechnologyReportDto> content = capacities.stream()
                            .map(b -> new CapacityTechnologyReportDto(
                                    b.name(),
                                    countsMap.getOrDefault(b.id(), 0L)
                            ))
                            .sorted((o1, o2) -> {
                                // Comparador dinámico
                                if ("DESC".equalsIgnoreCase(sortBy)) {
                                    return Long.compare(o2.getCantTechnologies(), o1.getCantTechnologies()); // Mayor a menor
                                } else {
                                    return Long.compare(o1.getCantTechnologies(), o2.getCantTechnologies()); // Menor a mayor
                                }
                            })
                            .toList();
                    // Retornamos el objeto de paginación
                    return new PageResponse<>(
                            content,
                            totalCapacities,
                            page,
                            size
                    );
                });
    }

    @Override
    //@Transactional
    public Mono<Void> deleteCapacityByBootcamp(Long idBootcamp, String messageId) {
        //Buscar capacidades en otros bootcamps
        return capacityPersistencePort.getCapacitiesInOtherBootcamps(idBootcamp, messageId)
                // si existe, retorna un error
                .flatMap(existe -> {
                    if (Boolean.TRUE.equals(existe)) {
                        return Mono.error(new BusinessException(TechnicalMessage.CAPACITY_WITH_OTHER_BOOTCAMPS));
                    }
                    //no existe, entonces llame al borrado de tecnologias
                    //busque la lista de capacidades a borrar
                    return capacityPersistencePort.getCapacitiesByBootcamp(idBootcamp, messageId)
                            .collectList()
                            .flatMap(capacities -> {
                                if(capacities.isEmpty()){
                                    log.warn("No se encontraron capacidades para el bootcamp: {}", idBootcamp);
                                    return Mono.empty();
                                }
                                //llama al metodo de borrado y le pasa la lista de capacidades a borrar en las tecnologias
                                return technologyGateway.deleteTechnologyByCapacity(idBootcamp, capacities, messageId)
                                        .flatMap(isDeleted -> {
                                            //si retorna 204 es porque borro
                                            //llame a mi metodo de borrar
                                                return capacityPersistencePort.deleteCapacitiesByBootcamp(idBootcamp, messageId)
                                                        .then(capacityPersistencePort.deleteAllCapacitiesyBootcamp(idBootcamp, messageId));
                                        });
                            });
                });
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
