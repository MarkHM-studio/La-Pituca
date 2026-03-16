package com.restobar.lapituca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Grupo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 0, max = 50)
    @Column(nullable = true, length = 50)
    private String nombre;

    @Column(nullable = false, length = 25)
    private String estado; // ACTIVO, CONSUMIENDO, CERRADO

    @Column(nullable = false)
    private Integer tipoGrupo; //1 = creado en local //2 = creado desde reserva

    private LocalDateTime fechaHora_InicioConsumo;

    private LocalDateTime fechaHora_Liberacion;

    @CreationTimestamp()
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora_registro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora_actualizacion;

    //Relaciones
    //Grupo - DetalleMesa
    @OneToMany(mappedBy = "grupo")
    private List<DetalleMesa> detalleMesas;
}
