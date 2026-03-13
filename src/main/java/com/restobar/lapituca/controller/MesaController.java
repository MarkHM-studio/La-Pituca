package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.MesaRequest;
import com.restobar.lapituca.dto.response.MesaResponse;
import com.restobar.lapituca.dto.response.MesasOcupadasResponse;
import com.restobar.lapituca.service.MesaService;
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
@RequestMapping("/api/mesa")
@Validated
public class MesaController {

    private final MesaService mesaService;

    @PostMapping
    public ResponseEntity<MesaResponse> crear(@Valid @RequestBody MesaRequest request){
        MesaResponse mesaResponse = mesaService.guardar(request);

        URI location = URI.create("/api/mesa/" + mesaResponse.getId());

        return ResponseEntity.created(location).body(mesaResponse);
    }

    @GetMapping
    public ResponseEntity<List<MesaResponse>> listarTodos(){
        return ResponseEntity.ok(mesaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(mesaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MesaResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id , @Valid @RequestBody MesaRequest request){
        return ResponseEntity.ok(mesaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        mesaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ocupadas")
    public ResponseEntity<List<MesasOcupadasResponse>> obtenerMesasOcupadas() {
        return ResponseEntity.ok(mesaService.obtenerMesasOcupadas());
    }
}
