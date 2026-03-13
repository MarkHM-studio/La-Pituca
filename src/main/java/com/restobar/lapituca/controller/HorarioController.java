package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.HorarioRequest;
import com.restobar.lapituca.dto.response.HorarioResponse;
import com.restobar.lapituca.service.HorarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/horario")
@RequiredArgsConstructor
@Validated
public class HorarioController {

    private final HorarioService horarioService;

    @PostMapping
    public ResponseEntity<HorarioResponse> crear(@Valid @RequestBody HorarioRequest request) {
        HorarioResponse horarioResponse = horarioService.guardar(request);

        URI location = URI.create("/api/horario" + horarioResponse.getId());

        return ResponseEntity.created(location).body(horarioResponse);
    }

    @GetMapping
    public ResponseEntity<List<HorarioResponse>> listarTodos() {
        return ResponseEntity.ok(horarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HorarioResponse> obtenerPorId(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        return ResponseEntity.ok(horarioService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HorarioResponse> actualizar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @RequestBody HorarioRequest request) {
        return ResponseEntity.ok(horarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        horarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
