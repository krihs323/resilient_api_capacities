package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.*;
import com.example.resilient_api.domain.spi.BootcampCapacityPersistencePort;
import com.example.resilient_api.domain.spi.TechnologyGateway;
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
class BootcampCapacityUseCaseTest {

    @Mock
    private BootcampCapacityPersistencePort bootcampCapacityPersistencePort;

    @Mock
    private TechnologyGateway technologyGateway;

    @InjectMocks
    private BootcampCapacityUseCase bootcampCapacityUseCase;

    private final String messageId = "trace-123";
    private BootcampCapacities bootcampCapacities;

    @BeforeEach
    void setUp() {
        // Objeto de prueba: Bootcamp con 2 capacidades asociadas
        List<BootcampCapacity> list = List.of(
                new BootcampCapacity(1L, 10L, 1L),
                new BootcampCapacity(2L, 11L, 1L)
        );
        bootcampCapacities = new BootcampCapacities(100L, list);
    }

    @Test
    @DisplayName("Should register bootcamp capacity successfully")
    void registerSuccess() {
        when(bootcampCapacityPersistencePort.existByIdBootcamp(anyLong())).thenReturn(Mono.just(false));
        when(bootcampCapacityPersistencePort.save(any())).thenReturn(Mono.just(bootcampCapacities));

        Mono<BootcampCapacities> result = bootcampCapacityUseCase.registerBootcampCapacity(bootcampCapacities, messageId);

        StepVerifier.create(result)
                .expectNext(bootcampCapacities)
                .verifyComplete();

        verify(bootcampCapacityPersistencePort).save(bootcampCapacities);
    }

    @Test
    @DisplayName("Should throw error if bootcamp ID already exists")
    void registerFailExists() {
        when(bootcampCapacityPersistencePort.existByIdBootcamp(anyLong())).thenReturn(Mono.just(true));

        Mono<BootcampCapacities> result = bootcampCapacityUseCase.registerBootcampCapacity(bootcampCapacities, messageId);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        ((BusinessException) e).getTechnicalMessage() == TechnicalMessage.CAPACITY_ALREADY_EXISTS)
                .verify();
    }

    @Test
    @DisplayName("Should throw error if list contains duplicate capacities")
    void registerFailDuplicates() {
        BootcampCapacity duplicate = new BootcampCapacity(1L, 10L, 1L);
        BootcampCapacities badRequest = new BootcampCapacities(100L, List.of(duplicate, duplicate));

        when(bootcampCapacityPersistencePort.existByIdBootcamp(anyLong())).thenReturn(Mono.just(false));

        Mono<BootcampCapacities> result = bootcampCapacityUseCase.registerBootcampCapacity(badRequest, messageId);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        ((BusinessException) e).getTechnicalMessage() == TechnicalMessage.CAPACITY_DUPLICATE_IN_LIST)
                .verify();
    }

    @Test
    @DisplayName("Should list capacities enriched with technologies")
    void listCapacitiesByBootcampEnriched() {
        // Datos de las capacidades
        Capacity cap1 = new Capacity(10L, "Java", "Backend", List.of());
        Technology tech1 = new Technology(50L, "Spring Boot", "Framework");

        when(bootcampCapacityPersistencePort.getCapacitiesByBootcamp(anyLong(), anyString()))
                .thenReturn(Flux.just(cap1));

        // Simular que el gateway devuelve una lista de tecnologías para esa capacidad
        when(technologyGateway.getTechnologiesByCapacity(eq("10"), anyString()))
                .thenReturn(Flux.just(tech1));

        Flux<CapacityTechnologies> result = bootcampCapacityUseCase.listCapacitiesByBootcamp(100L, messageId);

        StepVerifier.create(result)
                .assertNext(res -> {
                    assert res.id().equals(10L);
                    assert res.name().equals("Java");
                    assert res.capacityTechnologiesList().size() == 1;
                    assert res.capacityTechnologiesList().get(0).name().equals("Spring Boot");
                })
                .verifyComplete();
    }
}