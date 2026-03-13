package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, length = 8, unique = true)
    private String dni;

    @Column(nullable = false, length = 9, unique = true)
    private String telefono;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false, length = 25)
    private String estado;

    @Column(nullable = false, length = 25)
    private String tipo_cliente;

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
