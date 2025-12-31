package com.example.resilient_api;

import com.example.resilient_api.domain.api.CapacityServicePort;
import com.example.resilient_api.domain.model.Capacity;
import com.example.resilient_api.domain.model.CapacityTechnology;
import com.example.resilient_api.domain.model.PageResponse;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import com.example.resilient_api.infrastructure.entrypoints.dto.TechnologyDTO;
import com.example.resilient_api.infrastructure.entrypoints.handler.CapacityHandlerImpl;
import com.example.resilient_api.infrastructure.entrypoints.mapper.CapacityListMapper;
import com.example.resilient_api.infrastructure.entrypoints.mapper.CapacityMapper;
import com.example.resilient_api.infrastructure.validation.ObjectValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.List;
import static com.example.resilient_api.infrastructure.adapters.technologyapiadapter.util.Constants.X_MESSAGE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ResilientApiApplicationTests {

    @Mock
    private CapacityServicePort capacityServicePort;
    @Mock
    private CapacityMapper capacityMapper;
    @Mock
    private CapacityListMapper capacityListMapper;
    @Mock
    private ObjectValidator objectValidator;

    @InjectMocks
    private CapacityHandlerImpl capacityHandler;

    private CapacityDTO capacityDTO;
    private final String MESSAGE_ID = "test-uuid";

    @BeforeEach
    void setUp() {
        List<TechnologyDTO> technologies = List.of(
                new TechnologyDTO(1L),
                new TechnologyDTO(2L),
                new TechnologyDTO(3L)
        );
        capacityDTO = new CapacityDTO();
        capacityDTO.setName("Backend Java");
        capacityDTO.setId(1L);
        capacityDTO.setName("Backend Specialist");
        capacityDTO.setDescription("Capacidad enfocada en microservicios");
        capacityDTO.setCapacityTechnologyList(technologies);
    }

    @Test
    void createCapacitySuccess() {
        // GIVEN
        MockServerRequest request = MockServerRequest.builder()
                .header(X_MESSAGE_ID, MESSAGE_ID)
                .body(Mono.just(capacityDTO));

        List<CapacityTechnology> capacityTechnologies = List.of(
                new CapacityTechnology(1L, 1L, 100L),
                new CapacityTechnology(2L, 1L, 101L),
                new CapacityTechnology(3L, 1L, 102L)
        );

        //doNothing().when(objectValidator).validate(any());
        when(capacityMapper.capacityDTOToCapacity(any())).thenReturn(new Capacity(1L, "java", "description", capacityTechnologies));
        when(capacityServicePort.registerCapacity(any(), anyString()))
                .thenReturn(Mono.just(new Capacity(1L, "java", "description", capacityTechnologies)));

        // WHEN
        Mono<ServerResponse> responseMono = capacityHandler.createCapacity(request);

        // THEN
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CREATED, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void listCapacitySuccess() {
        // GIVEN
        MockServerRequest request = MockServerRequest.builder()
                .header(X_MESSAGE_ID, MESSAGE_ID)
                .queryParam("page", "0")
                .queryParam("size", "10")
                .build();

        PageResponse<CapacityTechnologyReportDto> pageResponse = createMockPageResponse();
        when(capacityServicePort.listCapacitiesPage(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(pageResponse));

        // WHEN
        Mono<ServerResponse> responseMono = capacityHandler.listCapacity(request);

        // THEN
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.statusCode());
                })
                .verifyComplete();
    }

    private PageResponse<CapacityTechnologyReportDto> createMockPageResponse() {
        // Creamos una lista de datos de prueba
        List<CapacityTechnologyReportDto> mockContent = List.of(
                new CapacityTechnologyReportDto("Java Backend", 5L),
                new CapacityTechnologyReportDto("Frontend React", 3L)
        );

        // Instanciamos el record con datos de paginación
        return new PageResponse<>(
                mockContent,
                2L,
                0,
                10
        );
    }

}
