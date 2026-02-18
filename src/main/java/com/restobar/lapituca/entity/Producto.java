package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 5, max = 50)
    @Column(nullable = false, length = 50)
    private String nombre;

    @Positive
    @Column(nullable = false) @Digits(integer = 5, fraction = 2)
    private BigDecimal precio;

    @Min(value = 0)
    @Column(nullable = false)
    private Integer stock;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDate fecha_inscripcion;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDate fecha_modificacion;

    //Relaciones:

    //Producto-Categoria
    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    //Producto-Marca
    @ManyToOne
    @JoinColumn(name = "id_marca")
    private Marca marca;

    /*
    //Producto-Pedido
    @OneToMany(mappedBy = "producto") //Si no necesitas navegar desde producto → pedidos, puedes quitarlo.
    private List<Pedido> pedido;*/
}
