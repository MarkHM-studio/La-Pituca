package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.TrabajadorRequest;
import com.restobar.lapituca.dto.response.TrabajadorResponse;
import com.restobar.lapituca.service.TrabajadorService;
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
@RequestMapping("/api/trabajador")
@RequiredArgsConstructor
@Validated
public class TrabajadorController {

    private final TrabajadorService trabajadorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TrabajadorResponse> crear(@Valid @RequestBody TrabajadorRequest request) {
        TrabajadorResponse trabajadorResponse = trabajadorService.guardar(request);

        URI location = URI.create("/api/trabajador/" + trabajadorResponse.getId());

        return ResponseEntity.created(location).body(trabajadorResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<TrabajadorResponse>> listarTodos(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(trabajadorService.listarTodos(estado));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TrabajadorResponse> obtenerPorId(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        return ResponseEntity.ok(trabajadorService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TrabajadorResponse> actualizar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody TrabajadorRequest request) {
        return ResponseEntity.ok(trabajadorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        trabajadorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> activar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){

        trabajadorService.activar(id);
        return ResponseEntity.noContent().build();
    }
}
