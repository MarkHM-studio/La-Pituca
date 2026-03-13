package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.TipoPagoRequest;
import com.restobar.lapituca.dto.response.TipoPagoResponse;
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
        return ResponseEntity.ok(tipoPagoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoPagoResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(tipoPagoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoPagoResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody TipoPagoRequest request){
        return ResponseEntity.ok(tipoPagoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        tipoPagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
