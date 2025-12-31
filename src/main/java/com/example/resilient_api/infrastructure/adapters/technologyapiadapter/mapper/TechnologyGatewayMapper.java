package com.example.resilient_api.infrastructure.adapters.technologyapiadapter.mapper;

import com.example.resilient_api.domain.model.Technology;
import com.example.resilient_api.infrastructure.adapters.technologyapiadapter.dto.TechnologyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface TechnologyGatewayMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    Technology toModel(TechnologyResponse technologyResponse);
}
