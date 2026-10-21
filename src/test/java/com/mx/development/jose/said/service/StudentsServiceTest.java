package com.mx.development.jose.said.service;

import com.mx.development.jose.said.dto.StudentsRequestDTO;
import com.mx.development.jose.said.dto.StudentsResponseDTO;
import com.mx.development.jose.said.exception.ResourceNotFoundException;
import com.mx.development.jose.said.mapper.StudentsMapper;
import com.mx.development.jose.said.model.Students;
import com.mx.development.jose.said.repository.StudentsRepository;
import com.mx.development.jose.said.service.impl.StudentsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentsServiceTest {

    @Mock
    private StudentsRepository studentsRepository;

    @Mock
    private StudentsMapper studentsMapper;

    @InjectMocks
    private StudentsServiceImpl studentsService;

    private Students entity;
    private StudentsRequestDTO requestDTO;
    private StudentsResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        entity = new Students();
        entity.setId(1L);

        requestDTO = new StudentsRequestDTO();
        responseDTO = new StudentsResponseDTO();
        responseDTO.setId(1L);
    }

    @Test
    void create_ShouldReturnResponseDTO_WhenValidRequest() {
        when(studentsMapper.toEntity(requestDTO)).thenReturn(entity);
        when(studentsRepository.save(entity)).thenReturn(entity);
        when(studentsMapper.toResponseDTO(entity)).thenReturn(responseDTO);

        StudentsResponseDTO result = studentsService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(studentsRepository, times(1)).save(entity);
    }

    @Test
    void findById_ShouldReturnResponseDTO_WhenExists() {
        when(studentsRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(studentsMapper.toResponseDTO(entity)).thenReturn(responseDTO);

        StudentsResponseDTO result = studentsService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(studentsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentsService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_ShouldReturnList() {
        when(studentsRepository.findAll()).thenReturn(List.of(entity));
        when(studentsMapper.toResponseDTO(entity)).thenReturn(responseDTO);

        List<StudentsResponseDTO> results = studentsService.findAll();

        assertThat(results).hasSize(1);
    }

    @Test
    void delete_ShouldDelete_WhenExists() {
        when(studentsRepository.existsById(1L)).thenReturn(true);
        doNothing().when(studentsRepository).deleteById(1L);

        assertThatCode(() -> studentsService.delete(1L)).doesNotThrowAnyException();
        verify(studentsRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        when(studentsRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studentsService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
