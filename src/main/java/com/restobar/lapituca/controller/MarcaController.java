package com.restobar.lapituca.controller;

import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.service.MarcaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/marca")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;

    @PostMapping
    public Marca crearMarca(@RequestBody Marca marca){
        marca.setFecha_inscripcion(LocalDate.now());
        return marcaService.crearMarca(marca);
    }

    @GetMapping
    public List<Marca> obtenerMarcas(){
        return marcaService.obtenerMarcas();
    }

    @GetMapping("/{id}")
    public Marca obtenerMarcaId(@PathVariable Long id){
        return marcaService.obtenerMarcaId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminarMarcaId(@PathVariable Long id){
        marcaService.eliminarMarcaId(id);
    }

    @PutMapping("/{id")
    public Marca actualizarMarcaId(@PathVariable Long id, String nombre){
        return marcaService.actualizarMarcaId(id, nombre);
    }
}
