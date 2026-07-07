package com.mx.development.said.students.mapper;

import com.mx.development.said.students.dto.StudentsRequestDTO;
import com.mx.development.said.students.dto.StudentsResponseDTO;
import com.mx.development.said.students.model.Students;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudentsMapper {

    Students toEntity(StudentsRequestDTO requestDTO);

    StudentsResponseDTO toResponseDTO(Students entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(StudentsRequestDTO requestDTO, @MappingTarget Students entity);
}
