package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Factura")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 13, max = 13)
    @Column(nullable = false, length = 11, unique = true)
    private String RUC;

    @OneToOne
    @JoinColumn(name = "id_comprobante")
    private Comprobante comprobante;

}
