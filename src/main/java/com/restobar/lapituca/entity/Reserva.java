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
import java.util.List;

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
    @Column(nullable = false)
    private Integer num_personas;

    @Column(nullable = false)
    private String estado;
    // ESPERANDO_PAGO, PAGADO, CANCELADO, EXPIRADO

    private LocalDateTime fechaHora_expiracionPago;

    private LocalDateTime fechaHora_verificacionReserva;

    @ManyToOne
    @JoinColumn(name = "id_usuario_verificador")
    private Usuario usuarioVerificador;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora_registro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora_actualizacion;

    // Relaciones

    // Reserva-Usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    //Reserva-Grupo
    @OneToOne
    @JoinColumn(name = "id_grupo")
    private Grupo grupo;

    //Reserva-Transaccion
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    private List<Transaccion> transacciones;
}