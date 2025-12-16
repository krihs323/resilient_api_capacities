package com.example.resilient_api.application.config;

import com.example.resilient_api.domain.spi.EmailValidatorGateway;
import com.example.resilient_api.domain.spi.CapacityPersistencePort;
import com.example.resilient_api.domain.usecase.CapacityUseCase;
import com.example.resilient_api.domain.api.CapacityServicePort;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.CapacityPersistenceAdapter;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.CapacityEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.CapacityTechnologyEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.CapacityRepository;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.CapacityTechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UseCasesConfig {
        private final CapacityRepository capacityRepository;
        private final CapacityEntityMapper capacityEntityMapper;

        private final CapacityTechnologyRepository capacityTechnologyRepository;
        private final CapacityTechnologyEntityMapper capacityTechnologyEntityMapper;

        @Bean
        public CapacityPersistencePort capacitiesPersistencePort() {
                return new CapacityPersistenceAdapter(capacityRepository, capacityEntityMapper, capacityTechnologyRepository, capacityTechnologyEntityMapper);
        }

        @Bean
        public CapacityServicePort capacitiesServicePort(CapacityPersistencePort capacitiesPersistencePort, EmailValidatorGateway emailValidatorGateway){
                return new CapacityUseCase(capacitiesPersistencePort, emailValidatorGateway);
        }
}
