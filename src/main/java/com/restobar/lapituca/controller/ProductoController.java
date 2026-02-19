package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.ProductoRequest;
import com.restobar.lapituca.dto.ProductoResponse;
import com.restobar.lapituca.entity.Producto;
import com.restobar.lapituca.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/producto")
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request){
        ProductoResponse productoCreado = productoService.guardar(request);

        URI location = URI.create("/api/producto/" + productoCreado.getId());

        return ResponseEntity.created(location).body(productoCreado);
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos(){
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id){
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request){
        ProductoResponse productoActualizado = productoService.actualizar(id, request);
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
