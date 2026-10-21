package com.mx.development.jose.said.dto;

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
