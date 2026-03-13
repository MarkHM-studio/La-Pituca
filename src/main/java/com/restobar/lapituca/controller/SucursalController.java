package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.SucursalRequest;
import com.restobar.lapituca.dto.response.SucursalResponse;
import com.restobar.lapituca.service.SucursalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sucursal")
@RequiredArgsConstructor
@Validated
public class SucursalController {

    private final SucursalService sucursalService;

    @PostMapping
    public ResponseEntity<SucursalResponse> crear(@Valid @RequestBody SucursalRequest request){
        SucursalResponse sucursalResponse = sucursalService.guardar(request);

        URI location = URI.create("/api/sucursal" + sucursalResponse.getId());

        return ResponseEntity.created(location).body(sucursalResponse);
    }

    @GetMapping
    public ResponseEntity<List<SucursalResponse>> listarTodos(){
        return ResponseEntity.ok(sucursalService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalResponse> obtenerPorId(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(sucursalService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SucursalResponse> actualizar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, SucursalRequest request){
        return ResponseEntity.ok(sucursalService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        sucursalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}