package com.example.resilient_api.infrastructure.documentation;


//public class CapacityRouter {
//    @Bean
//    public RouterFunction<ServerResponse> route(CapacityHandlerImpl capacityHandler) {
//        return route()
//                .POST("/user", capacityHandler::createCapacity, spec -> spec
//                        .operation(op -> op
//                                .operationId("createUser")
//                                .summary("Crear nuevo usuario")
//                                .description("Registra un nuevo usuario.")
//                                // Define el cuerpo de la petición
//                                .requestBody(req -> req.content(content -> content.schema(schema -> schema.implementation(UserDTO.class))))
//                                // Define las respuestas
//                                .response(201, res -> res.description("Usuario creado exitosamente"))
//                                .response(400, res -> res.description("Datos de entrada inválidos"))
//                        )
//                ).build();
//    }
//}
