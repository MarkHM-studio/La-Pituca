package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.TipoJornadaRequest;
import com.restobar.lapituca.dto.response.TipoJornadaResponse;
import com.restobar.lapituca.service.TipoJornadaService;
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
@RequestMapping("/api/tipoJornada")
@RequiredArgsConstructor
@Validated
public class TipoJornadaController {

    private final TipoJornadaService tipoJornadaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoJornadaResponse> crear(@Valid @RequestBody TipoJornadaRequest request) {
        TipoJornadaResponse tipoJornadaResponse = tipoJornadaService.guardar(request);

        URI location = URI.create("/api/tipoJornada" + tipoJornadaResponse.getId());

        return ResponseEntity.created(location).body(tipoJornadaResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<TipoJornadaResponse>> listarTodos() {
        return ResponseEntity.ok(tipoJornadaService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoJornadaResponse> obtenerPorId(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        return ResponseEntity.ok(tipoJornadaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoJornadaResponse> actualizar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, TipoJornadaRequest request) {
        return ResponseEntity.ok(tipoJornadaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        tipoJornadaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
