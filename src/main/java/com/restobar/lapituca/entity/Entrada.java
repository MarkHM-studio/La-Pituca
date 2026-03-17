package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Entrada")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entrada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Digits(integer = 4, fraction = 2)
    private BigDecimal cantidad_total;

    @Size(min = 1, max = 25)
    @Column(nullable = false)
    private String unidad_medida;

    @Column(nullable = false)
    @Digits(integer = 5, fraction = 2)
    private BigDecimal costo_unitario;

    @Column(nullable = false)
    @Digits(integer = 5, fraction = 2)
    private BigDecimal costo_total;

    @CreationTimestamp()
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora_registro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora_actualizacion;

    //Relaciones
    //Entrada-Producto
    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    //Entrada-Insumo
    @ManyToOne
    @JoinColumn(name = "id_insumo")
    private Insumo insumo;

    //Entrada-Proveedor
    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    //Entrada-Usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}
