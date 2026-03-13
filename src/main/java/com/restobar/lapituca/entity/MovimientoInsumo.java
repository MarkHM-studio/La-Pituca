package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "MovimientoInsumo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInsumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive
    @Digits(integer = 4, fraction = 2)
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal cantidad;

    @CreationTimestamp()
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora_registro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora_actualizacion;

    //Relaciones
    //MovimientoInsumo-Insumo
    @ManyToOne
    @JoinColumn(name = "id_insumo")
    private Insumo insumo;

    //MovimientoInsumo-Comprobante
    @ManyToOne
    @JoinColumn(name = "id_comprobante")
    private Comprobante comprobante;
}
