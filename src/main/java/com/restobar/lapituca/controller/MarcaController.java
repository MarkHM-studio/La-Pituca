package com.restobar.lapituca.controller;

import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.service.MarcaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/marca")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;

    @PostMapping
    public ResponseEntity<Marca> crear(@Valid @RequestBody Marca marca) {
        marca.setFecha_inscripcion(LocalDate.now());
        Marca nuevaMarca = marcaService.guardar(marca);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaMarca);
    }

    /*
    @PostMapping
    public Marca crear(@Valid @RequestBody Marca marca){
        marca.setFecha_inscripcion(LocalDate.now());
        return marcaService.guardar(marca);
    }*/

    @GetMapping
    public List<Marca> listarTodos(){
        return marcaService.listarTodos();
    }

    @GetMapping("/{id}")
    public Marca obtenerPorId(@RequestParam @PathVariable @Min(value = 1, message = "message") Long id){
        return marcaService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id){
        marcaService.eliminar(id);
    }

    @PutMapping("/{id}")
    public Marca actualizar(@PathVariable Long id, @RequestBody Marca marca){
        return marcaService.actualizar(id, marca);
    }
}
