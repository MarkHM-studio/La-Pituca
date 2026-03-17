package com.restobar.lapituca.controller;


import com.restobar.lapituca.dto.request.InsumoRequest;
import com.restobar.lapituca.dto.request.ProductoRequest;
import com.restobar.lapituca.dto.request.ReservaRequest;
import com.restobar.lapituca.dto.response.InsumoResponse;
import com.restobar.lapituca.dto.response.ProductoResponse;
import com.restobar.lapituca.service.InsumoService;
import com.restobar.lapituca.service.ProductoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/insumo")
@Validated
public class InsumoController {

    private final InsumoService insumoService;

    @PostMapping
    public ResponseEntity<InsumoResponse> crear(@Valid @RequestBody InsumoRequest request){
        InsumoResponse insumoResponse = insumoService.crear(request);

        URI location = URI.create("/api/insumo/" + insumoResponse.getId());

        return ResponseEntity.created(location).body(insumoResponse);
    }

    @GetMapping
    public ResponseEntity<List<InsumoResponse>> listarTodos(){
        return ResponseEntity.ok(insumoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsumoResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(insumoService.obtenerPorId(id));
    }

    @PutMapping("/{id}/almacenero")
    public ResponseEntity<InsumoResponse> actualizarRolAlmacenero(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody InsumoRequest request){
        return ResponseEntity.ok(insumoService.actualizarRolAlmacenero(id, request));
    }

    @PutMapping("/{id}/admin")
    public ResponseEntity<InsumoResponse> actualizarRolAdmin(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody InsumoRequest request){
        return ResponseEntity.ok(insumoService.actualizarRolAdmin(id, request));
    }
}
