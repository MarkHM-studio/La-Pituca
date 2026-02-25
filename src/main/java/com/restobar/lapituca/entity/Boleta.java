package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Boleta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Boleta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_comprobante")
    private Comprobante comprobante;
}
