package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.RecetaRequest;
import com.restobar.lapituca.dto.response.RecetaResponse;
import com.restobar.lapituca.service.RecetaService;
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
@RequestMapping("/api/receta")
@Validated
public class RecetaController {

    private final RecetaService recetaService;

    @PostMapping
    public ResponseEntity<List<RecetaResponse>> crear(@Valid @RequestBody RecetaRequest request) {
        List<RecetaResponse> creadas = recetaService.crear(request);
        Long productoId = creadas.isEmpty() ? request.getProductoId() : creadas.get(0).getProductoId();
        return ResponseEntity.created(URI.create("/api/receta/producto/" + productoId)).body(creadas);
    }

    @PutMapping("/producto/{productoId}")
    public ResponseEntity<List<RecetaResponse>> actualizar(
            @PathVariable @Positive(message = "El productoId debe ser mayor a 0") Long productoId,
            @Valid @RequestBody RecetaRequest request) {
        return ResponseEntity.ok(recetaService.actualizar(productoId, request));
    }

    @GetMapping
    public ResponseEntity<List<RecetaResponse>> listarTodos() {
        return ResponseEntity.ok(recetaService.listarTodos());
    }
}
