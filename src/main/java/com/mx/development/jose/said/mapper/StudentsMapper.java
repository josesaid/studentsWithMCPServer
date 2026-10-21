package com.mx.development.jose.said.mapper;

import com.mx.development.jose.said.dto.StudentsRequestDTO;
import com.mx.development.jose.said.dto.StudentsResponseDTO;
import com.mx.development.jose.said.model.Students;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudentsMapper {

    Students toEntity(StudentsRequestDTO requestDTO);

    StudentsResponseDTO toResponseDTO(Students entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(StudentsRequestDTO requestDTO, @MappingTarget Students entity);
}
