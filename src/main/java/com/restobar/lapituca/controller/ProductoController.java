package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.ProductoRequest;
import com.restobar.lapituca.dto.response.ProductoResponse;
import com.restobar.lapituca.service.ProductoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request){
        ProductoResponse productoCreado = productoService.guardar(request);

        URI location = URI.create("/api/producto/" + productoCreado.getId());

        return ResponseEntity.created(location).body(productoCreado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO', 'MOZO')")
    public ResponseEntity<List<ProductoResponse>> listarTodos(){
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO')")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO')")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody ProductoRequest request){
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO')")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
