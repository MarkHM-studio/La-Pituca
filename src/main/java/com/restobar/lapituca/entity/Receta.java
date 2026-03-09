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
import java.time.LocalDate;

@Entity
@Table(name = "Receta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive
    @Column(nullable = false)
    private Integer cantidad;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDate fecha_inscripcion;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDate fecha_modificacion;

    //Relaciones
    //Receta-Producto
    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    //Receta-Insumo
    @ManyToOne
    @JoinColumn(name = "id_insumo")
    private Insumo insumo;

}
