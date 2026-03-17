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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receta")
@Validated
public class RecetaController {

    private final RecetaService recetaService;

    @PostMapping
    public ResponseEntity<List<RecetaResponse>> crear(@Valid @RequestBody RecetaRequest request){
        return ResponseEntity.ok(recetaService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<RecetaResponse>> listarTodos(){
        return ResponseEntity.ok(recetaService.listarTodos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<List<RecetaResponse>> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody RecetaRequest request){
        return ResponseEntity.ok(recetaService.actualizar(id, request));
    }
}
