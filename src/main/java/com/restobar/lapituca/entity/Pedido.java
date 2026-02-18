package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "Pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //length no sirve para controlar numeros solo texto, precision y scale si
    @Positive
    @Digits(integer = 3, fraction = 0)
    @Column(nullable = false, precision = 3)
    private Integer cantidad;

    @Digits(integer = 4, fraction = 2)
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal precio_unitario;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal subtotal;

    @Size(min = 5, max = 25)
    @Column(nullable = false, length = 25)
    private String estado;

    //Se añadió esto
    @CreationTimestamp()
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora_registro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora_actualizacion;

    //Relaciones

    //Pedido-Producto
    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "id_comprobante")
    private Comprobante comprobante;
}
