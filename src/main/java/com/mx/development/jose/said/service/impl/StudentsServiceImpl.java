package com.mx.development.jose.said.service.impl;

import com.mx.development.jose.said.dto.StudentsRequestDTO;
import com.mx.development.jose.said.dto.StudentsResponseDTO;
import com.mx.development.jose.said.exception.ResourceNotFoundException;
import com.mx.development.jose.said.mapper.StudentsMapper;
import com.mx.development.jose.said.model.Students;
import com.mx.development.jose.said.repository.StudentsRepository;
import com.mx.development.jose.said.service.StudentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentsServiceImpl implements StudentsService {

    private final StudentsRepository studentsRepository;
    private final StudentsMapper studentsMapper;

    @Override
    @Transactional
    public StudentsResponseDTO create(StudentsRequestDTO requestDTO) {
        log.debug("Creating students...");
        Students entity = studentsMapper.toEntity(requestDTO);
        Students saved = studentsRepository.save(entity);
        return studentsMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentsResponseDTO findById(Long id) {
        log.debug("Finding students by id: {}", id);
        Students entity = studentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Students not found with id: " + id));
        return studentsMapper.toResponseDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentsResponseDTO> findAll() {
        log.debug("Finding all students");
        return studentsRepository.findAll()
                .stream()
                .map(studentsMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public StudentsResponseDTO update(Long id, StudentsRequestDTO requestDTO) {
        log.debug("Updating students id: {}", id);
        Students existing = studentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Students not found with id: " + id));
        studentsMapper.updateEntityFromDTO(requestDTO, existing);
        return studentsMapper.toResponseDTO(studentsRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting students id: {}", id);
        if (!studentsRepository.existsById(id))
            throw new ResourceNotFoundException("Students not found with id: " + id);
        studentsRepository.deleteById(id);
    }
}
