package com.mx.development.said.students.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentsResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;

}
