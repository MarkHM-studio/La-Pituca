package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.TipoBilleteraVirtualRequest;
import com.restobar.lapituca.dto.response.TipoBilleteraVirtualResponse;
import com.restobar.lapituca.service.TipoBilleteraVirtualService;
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
@RequestMapping("/api/billeteraVirtual")
@RequiredArgsConstructor
@Validated
public class TipoBilleteraVirtualController {

    private final TipoBilleteraVirtualService tipoBilleteraVirtualService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoBilleteraVirtualResponse> crear(@Valid @RequestBody TipoBilleteraVirtualRequest request){
        TipoBilleteraVirtualResponse tipoBilleteraVirtualResponse = tipoBilleteraVirtualService.guardar(request);

        URI location = URI.create("/api/billeteraVirtual/" + tipoBilleteraVirtualResponse.getId());

        return ResponseEntity.created(location).body(tipoBilleteraVirtualResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<TipoBilleteraVirtualResponse>> listarTodos(){
        return ResponseEntity.ok(tipoBilleteraVirtualService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoBilleteraVirtualResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(tipoBilleteraVirtualService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoBilleteraVirtualResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody TipoBilleteraVirtualRequest request){
        return ResponseEntity.ok(tipoBilleteraVirtualService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        tipoBilleteraVirtualService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

