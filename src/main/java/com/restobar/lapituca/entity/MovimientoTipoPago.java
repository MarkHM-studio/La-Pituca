package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "MovimientoTipoPago")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoTipoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive
    @Digits(integer = 6, fraction = 2)
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal monto;

    //Relaciones
    //MovimientoTipoPago-TipoPago
    @ManyToOne
    @JoinColumn(name = "id_tipoPago")
    private TipoPago tipoPago;

    //MovimientoTipoPago-Comprobante
    @ManyToOne
    @JoinColumn(name = "id_comprobante")
    private Comprobante comprobante;

    //MovimientoTipoPago-TipoBilleteraVirtual
    @ManyToOne
    @JoinColumn(name = "id_tipoBilleteraVirtual")
    private TipoBilleteraVirtual tipoBilleteraVirtual;
}
