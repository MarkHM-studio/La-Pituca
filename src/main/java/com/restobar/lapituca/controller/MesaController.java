package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.MesaRequest;
import com.restobar.lapituca.dto.MesaResponse;
import com.restobar.lapituca.dto.MesasOcupadasResponse;
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
        List<MesaResponse> mesaResponse = mesaService.listarTodos();
        return ResponseEntity.ok(mesaResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        MesaResponse mesaResponse = mesaService.obtenerPorId(id);
        return ResponseEntity.ok(mesaResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MesaResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id , @Valid @RequestBody MesaRequest request){
        MesaResponse mesaResponse = mesaService.actualizar(id, request);
        return ResponseEntity.ok(mesaResponse);
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
