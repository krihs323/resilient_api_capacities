package com.example.resilient_api.infrastructure.entrypoints.mapper;

import com.example.resilient_api.domain.model.CapacityTechnology;
import com.example.resilient_api.infrastructure.entrypoints.dto.TechnologyDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CapacityTechnologyMapper {
    @Mapping(source = "idTechnology", target = "id_tecnology")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "id_capacity", ignore = true)
    CapacityTechnology toCapacityTechnology(TechnologyDTO technologyDto);
}
