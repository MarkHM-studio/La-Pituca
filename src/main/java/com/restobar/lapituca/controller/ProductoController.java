package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.ProductoRequest;
import com.restobar.lapituca.dto.ProductoResponse;
import com.restobar.lapituca.service.ProductoService;
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
@RequestMapping("/api/producto")
@Validated
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request){
        ProductoResponse productoCreado = productoService.guardar(request);

        URI location = URI.create("/api/producto/" + productoCreado.getId());

        return ResponseEntity.created(location).body(productoCreado);
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarTodos(){
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody ProductoRequest request){
        ProductoResponse productoActualizado = productoService.actualizar(id, request);
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
