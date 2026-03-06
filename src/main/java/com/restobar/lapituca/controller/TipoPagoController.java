package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.TipoPagoRequest;
import com.restobar.lapituca.dto.TipoPagoResponse;
import com.restobar.lapituca.service.TipoPagoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/tipoPago")
@RequiredArgsConstructor
@Validated
public class TipoPagoController {

    private final TipoPagoService tipoPagoService;

    @PostMapping
    public ResponseEntity<TipoPagoResponse> crear(@Valid @RequestBody TipoPagoRequest request){
        TipoPagoResponse tipoPagoResponse = tipoPagoService.guardar(request);

        URI location = URI.create("api/tipoPago/" + tipoPagoResponse.getId());

        return ResponseEntity.created(location).body(tipoPagoResponse);
    }

    @GetMapping
    public ResponseEntity<List<TipoPagoResponse>> listarTodos(){
        List<TipoPagoResponse> tipoPagoResponse = tipoPagoService.listarTodos();
        return ResponseEntity.ok(tipoPagoResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoPagoResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        TipoPagoResponse tipoPagoResponse = tipoPagoService.obtenerPorId(id);
        return ResponseEntity.ok(tipoPagoResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoPagoResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody TipoPagoRequest request){
        TipoPagoResponse tipoPagoResponseActualizado = tipoPagoService.actualizar(id, request);
        return ResponseEntity.ok(tipoPagoResponseActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        tipoPagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
