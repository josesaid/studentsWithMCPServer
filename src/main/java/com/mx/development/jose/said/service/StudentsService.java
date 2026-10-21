package com.mx.development.jose.said.service;

import com.mx.development.jose.said.dto.StudentsRequestDTO;
import com.mx.development.jose.said.dto.StudentsResponseDTO;

import java.util.List;

public interface StudentsService {

    StudentsResponseDTO create(StudentsRequestDTO requestDTO);

    StudentsResponseDTO findById(Long id);

    List<StudentsResponseDTO> findAll();

    StudentsResponseDTO update(Long id, StudentsRequestDTO requestDTO);

    void delete(Long id);
}
