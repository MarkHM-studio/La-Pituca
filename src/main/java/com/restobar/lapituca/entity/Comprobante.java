package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Comprobante")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal IGV;

    private LocalDateTime fechaHora_venta;

    @Column(nullable = false, length = 25)
    private String estado;

    //Relaciones
    //Comprobante-Pedido
    @OneToMany(mappedBy = "comprobante")
    private List<Pedido> pedidos;

    //Comprobante-Grupo
    @OneToOne
    @JoinColumn(name = "id_grupo")
    private Grupo grupo;
}

