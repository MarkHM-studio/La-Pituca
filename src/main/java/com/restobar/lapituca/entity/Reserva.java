package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "Reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha_reserva;

    @Column(nullable = false)
    private LocalTime hora_reserva;

    @Positive
    @Min(1)
    @Column(nullable = false, length = 25)
    private Integer num_personas;

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
    //Reserva-Usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    //Reserva-Grupo
    @ManyToOne
    @JoinColumn(name = "id_Grupo")
    private Grupo grupo;

    //Reserva-Transaccion
    @OneToOne
    @JoinColumn(name = "id_transaccion")
    private Transaccion transaccion;
}
