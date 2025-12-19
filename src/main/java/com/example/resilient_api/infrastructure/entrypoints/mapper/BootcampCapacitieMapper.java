package com.example.resilient_api.infrastructure.entrypoints.mapper;

import com.example.resilient_api.domain.model.BootcampCapacities;
import com.example.resilient_api.domain.model.BootcampCapacity;
import com.example.resilient_api.infrastructure.entrypoints.dto.BootcampCapacitiesDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.BootcampCapacityDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface BootcampCapacitieMapper {

    @Mapping(source = "idBootcamp", target = "idBootcamp")
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "idCapacity", target = "idCapacity")
    BootcampCapacity toBootcampCapacity(BootcampCapacityDTO bootcampCapacityDTO);

    //mapeo nuevo
    @Mapping(source = "idBootcamp", target = "idBootcamp")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "idCapacity", target = "idCapacity")
    BootcampCapacityDTO toBootcampCapacityDTO(BootcampCapacity bootcampCapacity);

}
