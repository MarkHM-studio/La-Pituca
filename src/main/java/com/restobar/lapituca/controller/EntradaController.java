package com.restobar.lapituca.controller;


import com.restobar.lapituca.dto.request.EntradaRequest;
import com.restobar.lapituca.dto.response.EntradaResponse;
import com.restobar.lapituca.service.EntradaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/entrada")
@Validated
public class EntradaController {

    private final EntradaService entradaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ALMACENERO','ADMINISTRADOR')")
    public ResponseEntity<EntradaResponse> crear(@Valid @RequestBody EntradaRequest request) {
        EntradaResponse creada = entradaService.crear(request);
        return ResponseEntity.created(URI.create("/api/entrada/" + creada.getId())).body(creada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ALMACENERO','ADMINISTRADOR')")
    public ResponseEntity<List<EntradaResponse>> listarTodos() {
        return ResponseEntity.ok(entradaService.listarTodos());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ALMACENERO','ADMINISTRADOR')")
    public ResponseEntity<EntradaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody EntradaRequest request) {
        return ResponseEntity.ok(entradaService.actualizar(id, request));
    }
}
