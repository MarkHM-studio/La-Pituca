package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.print.attribute.standard.MediaSize;
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

    @Column(nullable = false)
    private Long mercadoPagoId;

    @Digits(integer = 4, fraction = 2)
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal monto;

    @CreationTimestamp()
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha_Pago;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora_actualizacion;

    //Relaciones
    //Transaccion-Usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    //Transaccion-Comprobante
    @OneToOne()
    @JoinColumn(name = "id_comprobante")
    private Comprobante comprobante;
}
