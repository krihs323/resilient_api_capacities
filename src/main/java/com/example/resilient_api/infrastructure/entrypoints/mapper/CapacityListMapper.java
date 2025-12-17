package com.example.resilient_api.infrastructure.entrypoints.mapper;

import com.example.resilient_api.domain.model.CapacityList;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityTechnologyReportDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CapacityListMapper {

    @Mapping(source = "name", target = "name")
    @Mapping(source = "cantTechnologies", target = "cantTechnologies")
    CapacityTechnologyReportDto capacityListToCapacityListDTO(CapacityList capacityList);

    CapacityList capacityListDTOToCapacityList(CapacityTechnologyReportDto capacityTechnologyReportDto);
}
