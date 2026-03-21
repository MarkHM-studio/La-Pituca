package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.DistritoRequest;
import com.restobar.lapituca.dto.response.DistritoResponse;
import com.restobar.lapituca.service.DistritoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/distrito")
@RequiredArgsConstructor
@Validated
public class DistritoController {

    private final DistritoService distritoService;

    @PostMapping
    public ResponseEntity<DistritoResponse> crear(@Valid @RequestBody DistritoRequest request) {
        DistritoResponse response = distritoService.guardar(request);
        return ResponseEntity.created(URI.create("/api/distrito/" + response.getId())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DistritoResponse>> listarTodos() {
        return ResponseEntity.ok(distritoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DistritoResponse> obtenerPorId(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(distritoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DistritoResponse> actualizar(@PathVariable @Positive Long id, @Valid @RequestBody DistritoRequest request) {
        return ResponseEntity.ok(distritoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        distritoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
