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
@Table(name = "Usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 5,max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Size(min = 5,max = 25)
    @Column(nullable = true, length = 25)
    private String password;

    @Size(min = 5,max = 25)
    @Column(nullable = false, length = 25)
    private String provider; // LOCAL o GOOGLE

    @Column(nullable = false)
    private Integer tipo_usuario;

    @Size(min = 5, max = 25)
    @Column(nullable = false, length = 25)
    private String estado;

    @CreationTimestamp()
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora_registro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora_actualizacion;

    //Usuario-Rol
    @ManyToOne
    @JoinColumn(name = "id_rol")
    private Rol rol;
}
