package com.restobar.lapituca.controller;


import com.restobar.lapituca.dto.request.EntradaRequest;
import com.restobar.lapituca.dto.response.EntradaResponse;
import com.restobar.lapituca.service.EntradaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<EntradaResponse> crear(@Valid @RequestBody EntradaRequest request) {
        EntradaResponse creada = entradaService.crear(request);
        return ResponseEntity.created(URI.create("/api/entrada/" + creada.getId())).body(creada);
    }

    @GetMapping
    public ResponseEntity<List<EntradaResponse>> listarTodos() {
        return ResponseEntity.ok(entradaService.listarTodos());
    }
}
