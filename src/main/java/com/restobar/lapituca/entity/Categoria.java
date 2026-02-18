package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.sound.sampled.Port;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Categoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 5, max = 50)
    @Column(nullable = false, length = 50)
    private String nombre;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDate fecha_inscripcion;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDate fecha_modificacion;

    /*
    @OneToMany(mappedBy = "categoria")
    private List<Producto> productos;*/
}
