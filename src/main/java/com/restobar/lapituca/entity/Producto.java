package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 5, max = 50)
    @Column(nullable = false, length = 50)
    private String nombre;

    @Positive
    @Column(nullable = false) @Digits(integer = 5, fraction = 2)
    private BigDecimal precio;

    @Min(value = 0)
    @Column(nullable = false)
    private Integer stock;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDate fecha_inscripcion;
}
