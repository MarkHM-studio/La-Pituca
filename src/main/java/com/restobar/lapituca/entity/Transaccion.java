package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID que devuelve Mercado Pago (payment.id)
    @Column(length = 64)
    private String mercadoPagoPaymentId;

    // ID de preferencia de Mercado Pago
    @Column(length = 64)
    private String mercadoPagoPreferenceId;

    // Para vincular la notificación con la reserva
    @Column(nullable = false, length = 64)
    private String externalReference;

    // Estado interno de la transacción
    @Column(nullable = false, length = 40)
    private String estado;

    // Estado devuelto por Mercado Pago (approved, pending, rejected, etc.)
    @Column(length = 40)
    private String estadoMercadoPago;

    @Column(length = 120)
    private String detalleEstadoMercadoPago;

    @Digits(integer = 8, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPago;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    //Relaciones
    //Transaccion-Usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    //Transaccion-Reserva
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;
}
