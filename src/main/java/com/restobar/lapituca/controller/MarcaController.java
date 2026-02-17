package com.restobar.lapituca.controller;

import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.service.MarcaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
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
    public ResponseEntity<List<Marca>> listarTodos(){
        return ResponseEntity.ok(marcaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Marca> obtenerPorId(@PathVariable Long id){
        return ResponseEntity.ok(marcaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Marca> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Marca marca){
        return ResponseEntity.ok(marcaService.actualizar(id, marca));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        marcaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
