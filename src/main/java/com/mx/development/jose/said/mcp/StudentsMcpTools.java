package com.mx.development.jose.said.mcp;

import com.mx.development.jose.said.dto.StudentsRequestDTO;
import com.mx.development.jose.said.dto.StudentsResponseDTO;
import com.mx.development.jose.said.service.StudentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Expone las operaciones del microservicio Students como tools MCP,
 * delegando siempre en {@link StudentsService} (sin duplicar lógica de negocio).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StudentsMcpTools {

    private final StudentsService studentsService;

    @Tool(name = "create_student", description = "Crea un nuevo estudiante con nombre, apellido y email")
    public StudentsResponseDTO createStudent(
            @ToolParam(description = "Nombre del estudiante", required = true) String firstName,
            @ToolParam(description = "Apellido del estudiante", required = true) String lastName,
            @ToolParam(description = "Correo electrónico del estudiante", required = true) String email) {
        log.info("MCP tool create_student invocada");
        StudentsRequestDTO request = new StudentsRequestDTO(firstName, lastName, email);
        return studentsService.create(request);
    }

    @Tool(name = "get_student", description = "Obtiene un estudiante existente por su id")
    public StudentsResponseDTO getStudent(
            @ToolParam(description = "Id del estudiante", required = true) Long id) {
        log.info("MCP tool get_student invocada para id={}", id);
        return studentsService.findById(id);
    }

    @Tool(name = "list_students", description = "Lista todos los estudiantes registrados")
    public List<StudentsResponseDTO> listStudents() {
        log.info("MCP tool list_students invocada");
        return studentsService.findAll();
    }

    @Tool(name = "update_student", description = "Actualiza nombre, apellido y/o email de un estudiante existente")
    public StudentsResponseDTO updateStudent(
            @ToolParam(description = "Id del estudiante a actualizar", required = true) Long id,
            @ToolParam(description = "Nombre del estudiante", required = true) String firstName,
            @ToolParam(description = "Apellido del estudiante", required = true) String lastName,
            @ToolParam(description = "Correo electrónico del estudiante", required = true) String email) {
        log.info("MCP tool update_student invocada para id={}", id);
        StudentsRequestDTO request = new StudentsRequestDTO(firstName, lastName, email);
        return studentsService.update(id, request);
    }

    @Tool(name = "delete_student", description = "Elimina un estudiante existente por su id")
    public String deleteStudent(
            @ToolParam(description = "Id del estudiante a eliminar", required = true) Long id) {
        log.info("MCP tool delete_student invocada para id={}", id);
        studentsService.delete(id);
        return "Estudiante " + id + " eliminado correctamente";
    }

    @Tool(name = "count_students", description = "Devuelve el número total de estudiantes registrados")
    public int countStudents() {
        int total = studentsService.findAll().size();
        log.info("MCP tool count_students invocada, total={}", total);
        return total;
    }

}
