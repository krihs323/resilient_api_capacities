package com.example.resilient_api;

import com.example.resilient_api.domain.spi.CapacityPersistencePort;
import com.example.resilient_api.domain.usecase.CapacityUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = ResilientApiApplication.class)
class ResilientApiApplicationTests {

	@MockBean
	private CapacityPersistencePort capacityPersistencePort;

	@Autowired
	private CapacityUseCase capacityUseCase;

	@Test
	void contextLoads() {
	}

}
