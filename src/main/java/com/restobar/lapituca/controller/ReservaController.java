package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.ReservaRequest;
import com.restobar.lapituca.dto.response.ReservaResponse;
import com.restobar.lapituca.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reserva")
@RequiredArgsConstructor
@Validated
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest request){
        ReservaResponse reservaResponse = reservaService.crear(request);

        URI location = URI.create("/api/reserva/" + reservaResponse.getId());

        return ResponseEntity.created(location).body(reservaResponse);
    }

    /*
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReservaRequest request){

        return ResponseEntity.ok(reservaService.actualizar(id,request));
    }*/

    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listar(){

        return ResponseEntity.ok(reservaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtenerPorId(
            @PathVariable Long id){

        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id){

        reservaService.cancelar(id);

        return ResponseEntity.noContent().build();
    }
}