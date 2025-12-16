package com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper;

import com.example.resilient_api.domain.model.Capacity;
import com.example.resilient_api.domain.model.CapacityTechnology;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.CapacityEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.CapacityTechnologyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CapacityTechnologyEntityMapper {

    @Mapping(source = "id_capacity", target = "id_capacity")// dominio es target, entidad es fuente
    @Mapping(source = "id_tecnology", target = "id_tecnology")// dominio es target, entidad es fuente
    CapacityTechnology toModel(CapacityTechnologyEntity entity);
    //CapacityEntity toEntity(Capacity capacity);
}
