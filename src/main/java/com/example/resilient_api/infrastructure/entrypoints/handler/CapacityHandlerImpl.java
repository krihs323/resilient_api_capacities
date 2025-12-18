package com.example.resilient_api.infrastructure.entrypoints.handler;

import com.example.resilient_api.domain.api.BootcampCapacityServicePort;
import com.example.resilient_api.domain.api.CapacityServicePort;
import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.exceptions.CustomException;
import com.example.resilient_api.domain.exceptions.TechnicalException;
import com.example.resilient_api.domain.model.BootcampCapacity;
import com.example.resilient_api.domain.model.PageResponse;
import com.example.resilient_api.infrastructure.entrypoints.dto.BootcampCapacitiesDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import com.example.resilient_api.infrastructure.entrypoints.mapper.BootcampCapacitieMapper;
import com.example.resilient_api.infrastructure.entrypoints.mapper.BootcampCapacitiesMapper;
import com.example.resilient_api.infrastructure.entrypoints.mapper.CapacityListMapper;
import com.example.resilient_api.infrastructure.entrypoints.mapper.CapacityMapper;
import com.example.resilient_api.infrastructure.entrypoints.util.APIResponse;
import com.example.resilient_api.infrastructure.entrypoints.util.ErrorDTO;
import com.example.resilient_api.infrastructure.validation.ObjectValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.time.Instant;
import java.util.List;

import static com.example.resilient_api.infrastructure.entrypoints.util.Constants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CapacityHandlerImpl {

    private final CapacityServicePort capacityServicePort;
    private final CapacityMapper capacityMapper;
    private final CapacityListMapper capacityListMapper;
    private final ObjectValidator objectValidator;
    private final BootcampCapacitiesMapper bootcampCapacitiesMapper;
    private final BootcampCapacityServicePort bootcampCapacityServicePort;


    @Operation(
            summary = "Registrar una nueva capacidad",
            description = "Crea una capacidad con un nombre, descripción y una lista de tecnologías (mínimo 3, máximo 20).",
            requestBody = @RequestBody(
                    description = "Información de la capacidad a registrar",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CapacityDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Capacidad creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
            })
    public Mono<ServerResponse> createCapacity(ServerRequest request) {
        String messageId = getMessageId(request);
        return request.bodyToMono(CapacityDTO.class).doOnNext(objectValidator::validate)
                .flatMap(capacity -> capacityServicePort.registerCapacity(capacityMapper.capacityDTOToCapacity(capacity), messageId)
                        .doOnSuccess(savedCapacity -> log.info("Capacity created successfully with messageId: {}", messageId))
                )
                .flatMap(savedCapacity -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(TechnicalMessage.CAPACITY_CREATED.getMessage()))
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnError(ex -> log.error(CAPACITY_ERROR, ex))
                .onErrorResume(BusinessException.class, ex -> buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        messageId,
                        TechnicalMessage.INVALID_PARAMETERS,
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getTechnicalMessage().getMessage())
                                .param(ex.getTechnicalMessage().getParam())
                                .build())))
                .onErrorResume(TechnicalException.class, ex -> buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        messageId,
                        TechnicalMessage.INTERNAL_ERROR,
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getTechnicalMessage().getMessage())
                                .param(ex.getTechnicalMessage().getParam())
                                .build())))
                .onErrorResume(CustomException.class, ex -> buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        messageId,
                        TechnicalMessage.INVALID_REQUEST,
                        List.of(ErrorDTO.builder()
                                .code(TechnicalMessage.INVALID_REQUEST.getCode())
                                .message(ex.getMessage())
                                .build())))
                .onErrorResume(ex -> {
                    log.error("Unexpected error occurred for messageId: {}", messageId, ex);
                    return buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            messageId,
                            TechnicalMessage.INTERNAL_ERROR,
                            List.of(ErrorDTO.builder()
                                    .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                    .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                                    .build()));
                });
    }

    @Operation(
            summary = "Registrar una nuevaas capacidades por bootcamps",
            description = "Crea una lista de capacidades del bootcamp",
            requestBody = @RequestBody(
                    description = "Información de la capacidad a registrar",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BootcampCapacitiesDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Capacidad creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
            })
    public Mono<ServerResponse> createCapacityBootcamp(ServerRequest request) {
        String messageId = getMessageId(request);
        return request.bodyToMono(BootcampCapacitiesDTO.class).doOnNext(objectValidator::validate)
                .flatMap(capacity -> bootcampCapacityServicePort.registerBootcampCapacity(bootcampCapacitiesMapper.bootcampCapacitiesDTOToBootcampCapacities(capacity), messageId)
                        .doOnSuccess(savedCapacity -> log.info("Capacity created successfully with messageId: {}", messageId))
                )
                .flatMap(savedCapacity -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(TechnicalMessage.CAPACITY_CREATED.getMessage()))
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnError(ex -> log.error(CAPACITY_ERROR, ex))
                .onErrorResume(BusinessException.class, ex -> buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        messageId,
                        TechnicalMessage.INVALID_PARAMETERS,
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getTechnicalMessage().getMessage())
                                .param(ex.getTechnicalMessage().getParam())
                                .build())))
                .onErrorResume(TechnicalException.class, ex -> buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        messageId,
                        TechnicalMessage.INTERNAL_ERROR,
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getTechnicalMessage().getMessage())
                                .param(ex.getTechnicalMessage().getParam())
                                .build())))
                .onErrorResume(CustomException.class, ex -> buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        messageId,
                        TechnicalMessage.INVALID_REQUEST,
                        List.of(ErrorDTO.builder()
                                .code(TechnicalMessage.INVALID_REQUEST.getCode())
                                .message(ex.getMessage())
                                .build())))
                .onErrorResume(ex -> {
                    log.error("Unexpected error occurred for messageId: {}", messageId, ex);
                    return buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            messageId,
                            TechnicalMessage.INTERNAL_ERROR,
                            List.of(ErrorDTO.builder()
                                    .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                    .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                                    .build()));
                });
    }

    @Operation(parameters = {
            @Parameter(name = "page", in = ParameterIn.QUERY, example = "0", description = "Número de página"),
            @Parameter(name = "size", in = ParameterIn.QUERY, example = "10", description = "Tamaño de la pàgina"),
            @Parameter(name = "sortBy", in = ParameterIn.QUERY, example = "name", description = "Ordenar por"),
            @Parameter(name = "sortDir", in = ParameterIn.QUERY, example = "ASC", description = "Dirección ASC/DESC")
    })
    public Mono<ServerResponse> listCapacity(ServerRequest request) {
        String messageId = getMessageId(request);
        //Parametros de paginacion
        String pageStr = request.queryParam("page").orElse("0");
        int page = Integer.parseInt(pageStr);
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String sortBy = request.queryParam("sortBy").orElse("name");
        String sortDir = request.queryParam("sortDir").orElse("ASC");
        Mono<PageResponse<CapacityTechnologyReportDto>> resultMono = capacityServicePort.listCapacitiesPage(page,  size,  sortBy,  sortDir, messageId);
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(resultMono, PageResponse.class);

    }

    private Mono<ServerResponse> buildErrorResponse(HttpStatus httpStatus, String identifier, TechnicalMessage error,
                                                    List<ErrorDTO> errors) {
        return Mono.defer(() -> {
            APIResponse apiErrorResponse = APIResponse
                    .builder()
                    .code(error.getCode())
                    .message(error.getMessage())
                    .identifier(identifier)
                    .date(Instant.now().toString())
                    .errors(errors)
                    .build();
            return ServerResponse.status(httpStatus)
                    .bodyValue(apiErrorResponse);
        });
    }

    private String getMessageId(ServerRequest serverRequest) {
        return serverRequest.headers().firstHeader(X_MESSAGE_ID);
    }
}
