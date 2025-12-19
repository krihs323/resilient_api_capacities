package com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper;

import com.example.resilient_api.domain.model.BootcampCapacity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampCapacityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface BootcampCapacityEntityMapper {

    // dominio es target, entidad es fuente
    @Mapping(source = "idCapacity", target = "idCapacity")
    @Mapping(source = "idBootcamp", target = "idBootcamp")
    BootcampCapacity toModel(BootcampCapacityEntity entity);
}
