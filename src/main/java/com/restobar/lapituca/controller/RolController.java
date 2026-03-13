package com.restobar.lapituca.controller;


import com.restobar.lapituca.dto.request.RolRequest;
import com.restobar.lapituca.dto.response.RolResponse;
import com.restobar.lapituca.service.RolService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rol")
@RequiredArgsConstructor
@Validated
public class RolController {

    private final RolService rolService;

    @PostMapping
    public ResponseEntity<RolResponse> crear(@Valid @RequestBody RolRequest request) {
        RolResponse rolResponse = rolService.guardar(request);

        URI location = URI.create("/api/rol" + rolResponse.getId());

        return ResponseEntity.created(location).body(rolResponse);
    }

    @GetMapping
    public ResponseEntity<List<RolResponse>> listarTodos() {
        return ResponseEntity.ok(rolService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolResponse> obtenerPorId(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        return ResponseEntity.ok(rolService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolResponse> actualizar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, RolRequest request) {
        return ResponseEntity.ok(rolService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
