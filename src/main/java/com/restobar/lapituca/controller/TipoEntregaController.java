package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.TipoEntregaRequest;
import com.restobar.lapituca.entity.TipoEntrega;
import com.restobar.lapituca.service.TipoEntregaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ReactiveOffsetScrollPositionHandlerMethodArgumentResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tipoEntrega")
@RequiredArgsConstructor
public class TipoEntregaController {

    private final TipoEntregaService tipoEntregaService;

    @PostMapping
    public ResponseEntity<TipoEntrega> crear(@Valid @RequestBody TipoEntregaRequest request){

        TipoEntrega tipoEntrega = tipoEntregaService.guardar(request);

        URI location = URI.create("/api/tipoEntrega" + request.getId());

        return ResponseEntity.created(location).body(tipoEntrega);
    }

    @GetMapping
    public ResponseEntity<List<TipoEntrega>> listarTodos(){
        List<TipoEntrega> tipoEntregas= tipoEntregaService.listarTodos();
        return ResponseEntity.ok(tipoEntregas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoEntrega> obtenerPorId(@PathVariable Long id){
        TipoEntrega tipoEntrega = tipoEntregaService.obtenerPorId(id);
        return ResponseEntity.ok(tipoEntrega);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoEntrega> actualizar(@PathVariable Long id, @Valid @RequestBody TipoEntregaRequest request){
        TipoEntrega tipoEntregaActualizado = tipoEntregaService.actualizar(id, request);
        return ResponseEntity.ok(tipoEntregaActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        tipoEntregaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
