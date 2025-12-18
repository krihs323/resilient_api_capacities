package com.example.resilient_api.domain.model;

import java.util.List;

public record BootcampCapacities(Long idBootcamp, List<BootcampCapacity> bootcampCapacityList) {
}