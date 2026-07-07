package com.mx.development.said.students.dto;

import jakarta.validation.constraints.*;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentsRequestDTO {

    @NotNull
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String email;

}
