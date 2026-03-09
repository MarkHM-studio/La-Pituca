package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Size(min = 5, max = 50)
    @Column(nullable = false, length = 50)
    private String apellido;

    @Size(min = 8, max = 8)
    @Column(nullable = false, length = 8, unique = true)
    private String dni;

    @Size(min = 9, max = 9)
    @Column(nullable = false, length = 9, unique = true)
    private Integer telefono;

    @Email(message = "El correo debe ser válido")
    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private LocalDate fecha_inicio;

    @Size(min = 5, max = 25)
    @Column(nullable = false, length = 25)
    private String estado;

    @CreationTimestamp()
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora_registro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora_actualizacion;

    //Relaciones
    //Cliente-Usuario
    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}
