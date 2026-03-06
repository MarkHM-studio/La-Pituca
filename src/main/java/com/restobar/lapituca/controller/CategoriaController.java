package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.CategoriaRequest;
import com.restobar.lapituca.dto.CategoriaResponse;
import com.restobar.lapituca.service.CategoriaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categoria")
@Validated
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping()
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request){
        CategoriaResponse nuevaCategoria =  categoriaService.guardar(request);

        URI location = URI.create("/api/categoria/" + nuevaCategoria.getId());

        /*
        URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(nuevaCategoria.getId())
        .toUri();*/

        return ResponseEntity.created(location).body(nuevaCategoria);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listarTodos(){
        return ResponseEntity.ok(categoriaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtenerPorId(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id,
            @Valid @RequestBody CategoriaRequest categoria){
        return ResponseEntity.ok(categoriaService.actualizar(id, categoria));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
