package com.example.resilient_api.infrastructure.entrypoints.mapper;

import com.example.resilient_api.domain.model.*;
import com.example.resilient_api.infrastructure.entrypoints.dto.BootcampCapacitiesDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        uses = {BootcampCapacitieMapper.class})
public interface BootcampCapacitiesMapper {

    @Mapping(source = "idBootcamp", target = "idBootcamp")
    @Mapping(source = "bootcampCapacityList", target = "bootcampCapacityList")
    BootcampCapacities bootcampCapacitiesDTOToBootcampCapacities(BootcampCapacitiesDTO bootcampCapacitiesDTO);

}
