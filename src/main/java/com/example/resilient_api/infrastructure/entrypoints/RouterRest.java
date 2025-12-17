package com.example.resilient_api.infrastructure.entrypoints;

import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityDTO;
import com.example.resilient_api.infrastructure.entrypoints.handler.CapacityHandlerImpl;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @RouterOperations({ @RouterOperation(path = "/getAllPersons", beanClass = CapacityDTO.class, beanMethod = "getAll"),
            @RouterOperation(path = "/getPerson/{id}", beanClass = CapacityDTO.class, beanMethod = "getById"),
            @RouterOperation(path = "/createPerson", beanClass = CapacityDTO.class, beanMethod = "save"),
            @RouterOperation(path = "/deletePerson/{id}", beanClass = CapacityDTO.class, beanMethod = "delete") })
    @Bean
    public RouterFunction<ServerResponse> routerFunction(CapacityHandlerImpl capacityHandler) {
        return route(POST("/capacity"), capacityHandler::createCapacity).
                andRoute(GET("/capacity"), capacityHandler::listCapacity);
    }
}
