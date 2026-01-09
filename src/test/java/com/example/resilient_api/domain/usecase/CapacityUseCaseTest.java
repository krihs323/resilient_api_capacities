package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.BootcampCapacity;
import com.example.resilient_api.domain.model.Capacity;
import com.example.resilient_api.domain.model.CapacityTechnology;
import com.example.resilient_api.domain.model.PageResponse;
import com.example.resilient_api.domain.spi.CapacityPersistencePort;
import com.example.resilient_api.domain.spi.TechnologyGateway;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapacityUseCaseTest {

    @Mock
    private CapacityPersistencePort capacityPersistencePort;

    @Mock
    private TechnologyGateway technologyGateway;

    @InjectMocks
    private CapacityUseCase capacityUseCase;

    private Capacity sampleCapacity;
    private Capacity sampleCapacity2;
    private BootcampCapacity sambleBootcampCapacity;
    private final String messageId = "test-trace-123";

    @BeforeEach
    void setUp() {
        // Objeto de prueba: Capacidad con 2 tecnologías
        CapacityTechnology rel1 = new CapacityTechnology(1L, 1L, 100L);
        CapacityTechnology rel2 = new CapacityTechnology(1L, 2L, 101L);

        sampleCapacity = new Capacity(1L, "Backend Java", "Ruta de microservicios", List.of(rel1, rel2));
        sampleCapacity2 = new Capacity(2L, "Python", "Desc", List.of(rel1, rel2));
        sambleBootcampCapacity = new BootcampCapacity(1L, 1L, 1L);
    }

    @Test
    @DisplayName("Should register capacity correctly when validations pass")
    void registerCapacitySuccess() {
        when(capacityPersistencePort.existByName(anyString())).thenReturn(Mono.just(false));
        when(capacityPersistencePort.save(any(Capacity.class))).thenReturn(Mono.just(sampleCapacity));
        when(technologyGateway.saveAll(anyList(), anyString())).thenReturn(Mono.empty());

        Mono<Capacity> result = capacityUseCase.registerCapacity(sampleCapacity, messageId);

        StepVerifier.create(result)
                .expectNextMatches(saved -> saved.name().equals("Backend Java"))
                .verifyComplete();

        verify(capacityPersistencePort).save(sampleCapacity);
        verify(technologyGateway).saveAll(anyList(), eq(messageId));
    }

    @Test
    @DisplayName("Should throw BusinessException when name already exists")
    void registerCapacityNameExists() {
        when(capacityPersistencePort.existByName(anyString())).thenReturn(Mono.just(true));

        Mono<Capacity> result = capacityUseCase.registerCapacity(sampleCapacity, messageId);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        ((BusinessException) e).getTechnicalMessage() == TechnicalMessage.CAPACITY_ALREADY_EXISTS)
                .verify();
    }

    @Test
    @DisplayName("Should throw BusinessException when duplicate technologies are in the request list")
    void registerCapacityDuplicateTechnologies() {
        // Creamos una capacidad con tecnologías repetidas (mismo ID)
        CapacityTechnology rel = new CapacityTechnology(1L, 1L, 100L);
        Capacity duplicateCapacity = new Capacity(1L, "Fail", "Desc", List.of(rel, rel));

        when(capacityPersistencePort.existByName(anyString())).thenReturn(Mono.just(false));

        Mono<Capacity> result = capacityUseCase.registerCapacity(duplicateCapacity, messageId);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        ((BusinessException) e).getTechnicalMessage() == TechnicalMessage.TECHNOLOGY_DUPLICATE_IN_LIST)
                .verify();
    }

    @Test
    @DisplayName("Should generate paged report with technology counts")
    void listCapacitiesPageSuccess() {
        // Datos de prueba
        when(capacityPersistencePort.countCapacities()).thenReturn(Mono.just(1L));
        when(capacityPersistencePort.listCapacitiesPage(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(Flux.just(sampleCapacity, sampleCapacity2));

        // El Gateway devuelve las relaciones para contar
        when(technologyGateway.getAllTecnologies(anyString()))
                .thenReturn(Flux.fromIterable(sampleCapacity.capacityTechnologyList()));

        Mono<PageResponse<CapacityTechnologyReportDto>> result =
                capacityUseCase.listCapacitiesPage(0, 10, "CANT", "ASC", messageId);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assert response.content().get(0).getName().equals("Backend Java");
                    assert response.content().get(1).getName().equals("Python");
                })
                .verifyComplete();

        Mono<PageResponse<CapacityTechnologyReportDto>> resultDesc =
                capacityUseCase.listCapacitiesPage(0, 10, "CANT", "DESC", messageId);

        StepVerifier.create(resultDesc)
                .assertNext(response -> {
                    assert response.content().get(0).getName().equals("Backend Java");
                    assert response.content().get(1).getName().equals("Python");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete capacity only when not linked to other bootcamps")
    void deleteCapacitySuccess() {
        Long idBootcamp = 5L;
        when(capacityPersistencePort.getCapacitiesInOtherBootcamps(anyLong(), anyString())).thenReturn(Mono.just(false));
        when(capacityPersistencePort.getCapacitiesByBootcamp(anyLong(), anyString())).thenReturn(Flux.just(sambleBootcampCapacity));
        when(technologyGateway.deleteTechnologyByCapacity(anyLong(), anyList(), anyString())).thenReturn(Mono.just(true));
        when(capacityPersistencePort.deleteCapacitiesByBootcamp(anyLong(), anyString())).thenReturn(Mono.empty());
        when(capacityPersistencePort.deleteAllCapacitiesyBootcamp(anyLong(), anyString())).thenReturn(Mono.empty());

        Mono<Void> result = capacityUseCase.deleteCapacityByBootcamp(idBootcamp, messageId);

        StepVerifier.create(result).verifyComplete();

        verify(capacityPersistencePort).deleteCapacitiesByBootcamp(idBootcamp, messageId);
    }
}