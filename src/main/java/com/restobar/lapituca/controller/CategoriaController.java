package com.restobar.lapituca.controller;

import com.restobar.lapituca.entity.Categoria;
import com.restobar.lapituca.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/categoria")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping()
    public ResponseEntity<Categoria> crear(@Valid @RequestBody Categoria categoria){
        Categoria nuevaCategoria =  categoriaService.guardar(categoria);

        URI location = URI.create("/api/marca/" + nuevaCategoria.getId());

        return ResponseEntity.created(location).body(nuevaCategoria);
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> listarTodos(){
        return ResponseEntity.ok(categoriaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable Long id){
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizar(@PathVariable Long id, @Valid @RequestBody Categoria categoria){
        return ResponseEntity.ok(categoriaService.actualizar(id, categoria));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
