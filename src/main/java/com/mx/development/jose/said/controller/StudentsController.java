package com.mx.development.jose.said.controller;

import com.mx.development.jose.said.dto.StudentsRequestDTO;
import com.mx.development.jose.said.dto.StudentsResponseDTO;
import com.mx.development.jose.said.service.StudentsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentsController {

    private final StudentsService studentsService;

    @PostMapping
    public ResponseEntity<StudentsResponseDTO> create(@Valid @RequestBody StudentsRequestDTO requestDTO) {
        log.info("POST /api/v1/students");
        return ResponseEntity.status(HttpStatus.CREATED).body(studentsService.create(requestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentsResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(studentsService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<StudentsResponseDTO>> findAll() {
        return ResponseEntity.ok(studentsService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentsResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody StudentsRequestDTO requestDTO) {
        return ResponseEntity.ok(studentsService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
