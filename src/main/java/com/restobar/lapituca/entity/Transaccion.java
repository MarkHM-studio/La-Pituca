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
@Table(name = "Transaccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID que devuelve Mercado Pago (payment.id)
    @Column(nullable = false)
    private String mercadoPagoPaymentId;

    // Para vincular con tu reserva (external_reference)
    @Column(nullable = false)
    private String externalReference;

    // Estado del pago
    @Column(nullable = false)
    private String estado; // PENDING, APPROVED, REJECTED

    @Digits(integer = 6, fraction = 2)
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal monto;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPago;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    // Relación con usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    // 🔥 Relación correcta: MUCHAS transacciones pueden existir (intentos)
    @ManyToOne
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;
}