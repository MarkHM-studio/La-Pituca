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

    @Size(min = 5, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = true, length = 100)
    private String password;

    @Column(nullable = true, length = 500)
    private String foto;

    @Size(min = 5, max = 25)
    @Column(nullable = false, length = 25)
    private String provider; // LOCAL o GOOGLE

    @Column(nullable = false)
    private Integer tipo_usuario;

    @Size(min = 5, max = 25)
    @Column(nullable = false, length = 25)
    private String estado;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora_registro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora_actualizacion;

    //Relaciones
    //Usuario-Rol
    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;
}