package com.mx.development.said.students.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Students {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 30)
    private String firstName;

    @NotBlank
    @Column(nullable = false, length = 40)
    private String lastName;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String email;

}
