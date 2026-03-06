package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.TipoEntregaRequest;
import com.restobar.lapituca.dto.TipoEntregaResponse;
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
        List<TipoEntregaResponse> tiposEntregasResponse= tipoEntregaService.listarTodos();
        return ResponseEntity.ok(tiposEntregasResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoEntregaResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        TipoEntregaResponse tipoEntregaResponse = tipoEntregaService.obtenerPorId(id);
        return ResponseEntity.ok(tipoEntregaResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoEntregaResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody TipoEntregaRequest request){
        TipoEntregaResponse tipoEntregaResponseActualizado = tipoEntregaService.actualizar(id, request);
        return ResponseEntity.ok(tipoEntregaResponseActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        tipoEntregaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
