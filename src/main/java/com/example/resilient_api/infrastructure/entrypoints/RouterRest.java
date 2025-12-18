package com.example.resilient_api.infrastructure.entrypoints;

import com.example.resilient_api.infrastructure.entrypoints.handler.CapacityHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

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
                .POST("/capacity/bootcamp",
                        capacityHandler::createCapacityBootcamp,
                        ops -> ops.beanClass(CapacityHandlerImpl.class).beanMethod("createCapacityBootcamp")
                )
                .build();
    }
}
