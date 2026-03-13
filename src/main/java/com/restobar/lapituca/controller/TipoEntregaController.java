package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.TipoEntregaRequest;
import com.restobar.lapituca.dto.response.TipoEntregaResponse;
import com.restobar.lapituca.service.TipoEntregaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tipoEntrega")
@RequiredArgsConstructor
@Validated
public class TipoEntregaController {

    private final TipoEntregaService tipoEntregaService;

    @PostMapping
    public ResponseEntity<TipoEntregaResponse> crear(@Valid @RequestBody TipoEntregaRequest request){

       TipoEntregaResponse tipoEntregaResponse = tipoEntregaService.guardar(request);

        URI location = URI.create("/api/tipoEntrega/" + tipoEntregaResponse.getId());

        return ResponseEntity.created(location).body(tipoEntregaResponse);
    }

    @GetMapping
    public ResponseEntity<List<TipoEntregaResponse>> listarTodos(){
        return ResponseEntity.ok(tipoEntregaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoEntregaResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(tipoEntregaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoEntregaResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody TipoEntregaRequest request){
        return ResponseEntity.ok(tipoEntregaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        tipoEntregaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
