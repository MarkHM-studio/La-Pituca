package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.AsignarMesasRequest;
import com.restobar.lapituca.dto.request.ComprobanteRequest;
import com.restobar.lapituca.dto.response.ComprobanteResponse;
import com.restobar.lapituca.dto.request.RegistrarVentaRequest;
import com.restobar.lapituca.service.ComprobanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comprobante")
@Validated
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @PostMapping
    public ResponseEntity<ComprobanteResponse> crear(@Valid @RequestBody ComprobanteRequest request) {
        ComprobanteResponse nuevocomprobanteResponse = comprobanteService.crearComprobante(request);

        URI location = URI.create("/api/comprobante/" + nuevocomprobanteResponse.getId());

        return ResponseEntity.created(location).body(nuevocomprobanteResponse);
    }

    @PutMapping("/asignar-mesas")
    public ResponseEntity<ComprobanteResponse> asignarMesas(@Valid @RequestBody AsignarMesasRequest request){
        return ResponseEntity.ok(comprobanteService.asignarGrupoYMesasSiEsComer(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id){
        comprobanteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/registrar-venta")
    public ResponseEntity<String> registrarVenta(@RequestBody RegistrarVentaRequest request) {
        return ResponseEntity.ok(comprobanteService.registrarVenta(request));
    }
}
