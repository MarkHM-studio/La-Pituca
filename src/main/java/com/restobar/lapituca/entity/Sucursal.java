package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Sucursal")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 5, max = 50)
    @Column(nullable = false, length = 50)
    private String nombre;

    @Size(min = 5, max = 150)
    @Column(nullable = false, length = 150)
    private String direccion;

    @Size(min = 13, max = 13)
    @Column(nullable = false, length = 11, unique = true)
    private String RUC;
}
