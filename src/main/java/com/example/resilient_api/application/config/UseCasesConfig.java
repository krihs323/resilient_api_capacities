package com.example.resilient_api.application.config;

import com.example.resilient_api.domain.api.BootcampCapacityServicePort;
import com.example.resilient_api.domain.spi.BootcampCapacityPersistencePort;
import com.example.resilient_api.domain.spi.CapacityPersistencePort;
import com.example.resilient_api.domain.spi.TechnologyGateway;
import com.example.resilient_api.domain.usecase.BootcampCapacityUseCase;
import com.example.resilient_api.domain.usecase.CapacityUseCase;
import com.example.resilient_api.domain.api.CapacityServicePort;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.BootcampCapacityPersistenceAdapter;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.CapacityPersistenceAdapter;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.BootcampCapacityEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.CapacityEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.CapacityTechnologyEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampCapacityRepository;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.CapacityRepository;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.CapacityTechnologyRepository;
import com.example.resilient_api.infrastructure.entrypoints.mapper.CapacityListMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
@RequiredArgsConstructor
public class UseCasesConfig {
        private final CapacityRepository capacityRepository;
        private final CapacityEntityMapper capacityEntityMapper;
        private final CapacityTechnologyRepository capacityTechnologyRepository;
        private final CapacityTechnologyEntityMapper capacityTechnologyEntityMapper;
        private final CapacityListMapper capacityListMapper;
        private final DatabaseClient databaseClient;

        private final BootcampCapacityRepository bootcampCapacityRepository;
        private final BootcampCapacityEntityMapper bootcampCapacityEntityMapper;

        private final TechnologyGateway technologyGateway;



    @Bean
        public CapacityPersistencePort capacitiesPersistencePort() {
                return new CapacityPersistenceAdapter(capacityRepository, capacityEntityMapper, capacityTechnologyRepository, capacityTechnologyEntityMapper, capacityListMapper, databaseClient);
        }

        @Bean
        public CapacityServicePort capacitiesServicePort(CapacityPersistencePort capacitiesPersistencePort){
                return new CapacityUseCase(capacitiesPersistencePort, technologyGateway);
        }


        @Bean
        public BootcampCapacityPersistencePort bootcampCapacityPersistencePort() {
            return new BootcampCapacityPersistenceAdapter(bootcampCapacityRepository, bootcampCapacityEntityMapper, databaseClient);
        }

        @Bean
        public BootcampCapacityServicePort bootcampCapacityServicePort(BootcampCapacityPersistencePort bootcampCapacityPersistencePort){
            return new BootcampCapacityUseCase(bootcampCapacityPersistencePort, technologyGateway);
        }
}
