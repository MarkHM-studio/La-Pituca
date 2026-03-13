package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.TurnoRequest;
import com.restobar.lapituca.dto.response.TurnoResponse;
import com.restobar.lapituca.service.TurnoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/turno")
@RequiredArgsConstructor
@Validated
public class TurnoController {

    private final TurnoService turnoService;

    @PostMapping
    public ResponseEntity<TurnoResponse> crear(@Valid @RequestBody TurnoRequest request) {
        TurnoResponse turnoResponse = turnoService.guardar(request);

        URI location = URI.create("/api/turno" + turnoResponse.getId());

        return ResponseEntity.created(location).body(turnoResponse);
    }

    @GetMapping
    public ResponseEntity<List<TurnoResponse>> listarTodos() {
        return ResponseEntity.ok(turnoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponse> obtenerPorId(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        return ResponseEntity.ok(turnoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TurnoResponse> actualizar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, TurnoRequest request) {
        return ResponseEntity.ok(turnoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        turnoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
