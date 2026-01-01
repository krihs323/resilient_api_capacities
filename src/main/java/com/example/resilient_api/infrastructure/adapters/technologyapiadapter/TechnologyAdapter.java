package com.example.resilient_api.infrastructure.adapters.technologyapiadapter;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.exceptions.TechnicalException;
import com.example.resilient_api.domain.model.TechnologyApiResult;
import com.example.resilient_api.domain.model.Technology;
import com.example.resilient_api.domain.spi.TechnologyGateway;
import com.example.resilient_api.infrastructure.adapters.technologyapiadapter.dto.TechnologyApiProperties;
import com.example.resilient_api.infrastructure.adapters.technologyapiadapter.util.Constants;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TechnologyAdapter implements TechnologyGateway {

    private final WebClient webClient;
    private final TechnologyApiProperties technologyApiProperties;
    private final Retry retry;
    private final Bulkhead bulkhead;

    @Value("${technology-api}")
    private String technologyPath;

    @Override
    @CircuitBreaker(name = "technologyApi", fallbackMethod = "fallback")
    public Flux<Technology> getTechnologiesByCapacity(String idCapacity, String messageId) {
        log.info("Starting get all capacities x technologies");

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("capacity/capacities-by-Bootcamps/")
                        .queryParam("idCapacity", idCapacity) // Agregamos el query param aquí
                        .build())
                // Definir el tipo de contenido (JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("x-message-id", messageId)
                .retrieve()
                // Manejo de errores basado en códigos de estado HTTP
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        buildErrorResponse(response, TechnicalMessage.INTERNAL_ERROR_IN_ADAPTERS))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new RuntimeException("Error del servidor externo")))
                // Mapear el cuerpo de la respuesta a un objeto
                .bodyToFlux(Technology.class)
                .doOnNext(response -> log.info("Received get API response for messageId {}: {}", messageId, response))
                .doOnTerminate(() -> log.info("Completed technologies x capacities get process for messageId: {}", messageId))
                .doOnError(e -> log.error("Error getting technologies x capacity for messageId: {}", messageId, e));
    }

    @Override
    public Mono<Boolean> deleteTechnologyByCapacity(int id, String messageId) {
        log.info("Starting deletion for: {} with messageId: {}", id, messageId);

        return webClient.delete() // Usamos el verbo DELETE
                .uri(uriBuilder -> uriBuilder
                        .path("capacity/{id}") // Asumiendo que el nombre va en el path o usa queryParam según tu API
                        .queryParam("api_key", technologyApiProperties.getApiKey())
                        .build(id))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(Messages.MSJ_HEADER.getValue(), messageId)
                .retrieve()
                .onStatus(status -> status.value() == 400, response ->
                        response.bodyToMono(String.class) // Leemos el cuerpo del error (puede ser JSON o texto)
                                .flatMap(errorBody -> {
                                    log.error("Error 400 - Bad Request for messageId {}: {}", messageId, errorBody);
                                    // Lógica para propagar el error o lanzar una excepción personalizada
                                    return Mono.error(new RuntimeException("API Error: " + errorBody));
                                })
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        buildErrorResponse(response, TechnicalMessage.INTERNAL_ERROR_IN_ADAPTERS))
                .toEntity(Void.class)
                .map(response -> response.getStatusCode().is2xxSuccessful())
                // Si ocurre un error (incluido el 400), el operador onErrorResume lo captura
                .onErrorResume(e -> {
                    log.error("Final failure in delete for messageId {}: {}", messageId, e.getMessage());
                    return Mono.just(false);
                })
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(mono -> Mono.defer(() -> bulkhead.executeSupplier(() -> mono)));
    }

    public Mono<TechnologyApiResult> fallback(Throwable t) {
        return Mono.defer(() ->
                Mono.justOrEmpty(t instanceof TimeoutException
                                ? new TechnologyApiResult("UNKOWN", "0.0") // Respuesta por timeout
                                : null)
                        .switchIfEmpty(Mono.error(t))  // Si no es timeout, lanza el error
        );
    }

    private Mono<Throwable> buildErrorResponse(ClientResponse response, TechnicalMessage technicalMessage) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty(Constants.NO_ADITIONAL_ERROR_DETAILS)
                .flatMap(errorBody -> {
                    log.error(Constants.STRING_ERROR_BODY_DATA, errorBody);
                    return Mono.error(
                            response.statusCode().is5xxServerError() ?
                                    new TechnicalException(technicalMessage):
                                    new BusinessException(technicalMessage));
                });
    }


}
