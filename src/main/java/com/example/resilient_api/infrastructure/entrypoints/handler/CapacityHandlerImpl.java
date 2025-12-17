package com.example.resilient_api.infrastructure.entrypoints.handler;

import com.example.resilient_api.domain.api.CapacityServicePort;
import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.exceptions.CustomException;
import com.example.resilient_api.domain.exceptions.TechnicalException;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import com.example.resilient_api.infrastructure.entrypoints.mapper.CapacityListMapper;
import com.example.resilient_api.infrastructure.entrypoints.mapper.CapacityMapper;
import com.example.resilient_api.infrastructure.entrypoints.util.APIResponse;
import com.example.resilient_api.infrastructure.entrypoints.util.ErrorDTO;
import com.example.resilient_api.infrastructure.validation.ObjectValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
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

    public Mono<ServerResponse> listCapacity(ServerRequest request) {
        String messageId = getMessageId(request);

        //Parametros de paginacion
        String pageStr = request.queryParam("page").orElse("0");
        int page = Integer.parseInt(pageStr);
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String sortBy = request.queryParam("sortBy").orElse("name");
        String sortDir = request.queryParam("sortDir").orElse("ASC");


        //Mono<PageResult<CapacityListDTO>> resultMono = capacityServicePort.listCapacities(page, size, sortBy, sortDir);

        //Flux<CapacityTechnologyReportDto> products = capacityServicePort.listCapacitiesNoPage(page,  size,  sortBy,  sortDir, messageId).map(capacityListMapper::capacityListToCapacityListDTO);
        //return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(products, CapacityTechnologyReportDto.class);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(capacityServicePort.listCapacitiesNoPage(page,  size,  sortBy,  sortDir, messageId), CapacityTechnologyReportDto.class);

        //TODO SE DEBE CAMBIAR A LA PAGIGACION pageResult<OrderResponse>
        /*return request.bodyToMono(CapacityListDTO.class)
                .flatMap(capacity -> capacityServicePort.listCapacities(capacityListMapper.capacityListDTOToCapacityList(capacity), page,  size,  sortBy,  sortDir, messageId)
                        .doOnSuccess(savedCapacity -> log.info("Capacity listed successfully with messageId: {}", messageId))
                )
                .flatMap(savedCapacity -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(TechnicalMessage.CAPACITY_CREATED.getMessage()))
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnError(ex -> log.error(CAPACITY_ERROR, ex))
               ;*/
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
