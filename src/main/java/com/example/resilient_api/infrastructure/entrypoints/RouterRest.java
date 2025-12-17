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

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;


@Configuration
public class RouterRest {


    @Bean
    public RouterFunction<ServerResponse> routerFunction(CapacityHandlerImpl capacityHandler) {
        return route()
                .POST("/capacity",
                        capacityHandler::createCapacity,
                        ops -> ops.beanClass(CapacityHandlerImpl.class).beanMethod("createCapacity"))

                .GET("/capacity",
                        capacityHandler::listCapacity,
                        ops -> ops.beanClass(CapacityHandlerImpl.class).beanMethod("listCapacity")
                )
                .build();
    }
}
