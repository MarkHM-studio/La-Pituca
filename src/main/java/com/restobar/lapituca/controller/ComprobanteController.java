package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.AsignarMesasRequest;
import com.restobar.lapituca.dto.request.ComprobanteRequest;
import com.restobar.lapituca.dto.response.ComprobanteResponse;
import com.restobar.lapituca.dto.request.RegistrarVentaRequest;
import com.restobar.lapituca.service.ComprobanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comprobante")
@Validated
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @PostMapping
    @PreAuthorize("hasRole('MOZO')")
    public ResponseEntity<ComprobanteResponse> crear(@Valid @RequestBody ComprobanteRequest request) {
        ComprobanteResponse nuevocomprobanteResponse = comprobanteService.crearComprobante(request);

        URI location = URI.create("/api/comprobante/" + nuevocomprobanteResponse.getId());

        return ResponseEntity.created(location).body(nuevocomprobanteResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public ResponseEntity<List<ComprobanteResponse>> listarTodos(){
        return ResponseEntity.ok(comprobanteService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public ResponseEntity<ComprobanteResponse> obtenerPorId(@PathVariable Long id){
        return ResponseEntity.ok(comprobanteService.obtenerPorId(id));
    }

    @PutMapping("/asignar-mesas")
    @PreAuthorize("hasRole('MOZO')")
    public ResponseEntity<ComprobanteResponse> asignarMesas(@Valid @RequestBody AsignarMesasRequest request){
        return ResponseEntity.ok(comprobanteService.asignarGrupoYMesasSiEsComer(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MOZO')")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id){
        comprobanteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/registrar-venta")
    @PreAuthorize("hasRole('CAJERO')")
    public ResponseEntity<String> registrarVenta(@Valid @RequestBody RegistrarVentaRequest request) {
        return ResponseEntity.ok(comprobanteService.registrarVenta(request));
    }
}
