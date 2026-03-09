package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Proveedor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3, max = 50)
    @Column(nullable = false, length = 50)
    private String contacto;

    @Size(min = 5, max = 50)
    @Column(nullable = false, length = 50)
    private String razon_social;

    @Size(min = 11, max = 11)
    @Column(nullable = false, length = 11)
    private String RUC;

    @Size(min = 15, max = 150)
    @Column(nullable = false, length = 150)
    private String direccion;

    @Size(min = 9, max = 9)
    @Column(nullable = false, length = 9)
    private String telefono;

    @Email(message = "El correo debe ser válido")
    @Column(nullable = false, unique = true)
    private String correo;

    @Size(min = 5, max = 25)
    @Column(nullable = false, length = 25)
    private String estado;

    @CreationTimestamp()
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora_registro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora_actualizacion;
}
