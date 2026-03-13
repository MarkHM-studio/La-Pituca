package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.TrabajadorRequest;
import com.restobar.lapituca.dto.response.TrabajadorResponse;
import com.restobar.lapituca.service.TrabajadorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/trabajador")
@RequiredArgsConstructor
@Validated
public class TrabajadorController {

    private final TrabajadorService trabajadorService;

    @PostMapping
    public ResponseEntity<TrabajadorResponse> crear(@Valid @RequestBody TrabajadorRequest request) {
        TrabajadorResponse trabajadorResponse = trabajadorService.guardar(request);

        URI location = URI.create("/api/trabajador" + trabajadorResponse.getId());

        return ResponseEntity.created(location).body(trabajadorResponse);
    }

    @GetMapping
    public ResponseEntity<List<TrabajadorResponse>> listarTodos() {
        return ResponseEntity.ok(trabajadorService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrabajadorResponse> obtenerPorId(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        return ResponseEntity.ok(trabajadorService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrabajadorResponse> actualizar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, TrabajadorRequest request) {
        return ResponseEntity.ok(trabajadorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        trabajadorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
