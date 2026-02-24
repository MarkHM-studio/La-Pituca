package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.AsignarMesasRequest;
import com.restobar.lapituca.dto.ComprobanteResponse;
import com.restobar.lapituca.service.ComprobanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comprobante")
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @PostMapping
    public ResponseEntity<ComprobanteResponse> crear() {
        ComprobanteResponse comprobanteResponse = comprobanteService.crearComprobante();

        URI location = URI.create("/api/comprobante" + comprobanteResponse.getId());

        return ResponseEntity.created(location).body(comprobanteResponse);
    }

    @PutMapping("/asignar-mesas")
    public ResponseEntity<ComprobanteResponse> asignarMesas(@Valid @RequestBody AsignarMesasRequest request){
        ComprobanteResponse comprobanteResponse = comprobanteService.asignarGrupoYMesasSiEsComer(request);
        return ResponseEntity.ok(comprobanteResponse);
    }
}
