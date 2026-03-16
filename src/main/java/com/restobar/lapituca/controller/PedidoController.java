package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.response.PedidoDetalleResponse;
import com.restobar.lapituca.dto.request.PedidoRequest;
import com.restobar.lapituca.dto.response.PedidoResponse;
import com.restobar.lapituca.service.PedidoService;
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
@RequestMapping("/api/pedido")
@Validated
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoDetalleResponse> crear(@Valid @RequestBody PedidoRequest request){
        PedidoDetalleResponse detalleResponse = pedidoService.guardar(request);

        URI location = URI.create("/api/pedido/" + detalleResponse.getId());

        return ResponseEntity.created(location).body(detalleResponse);
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarTodos(){
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<PedidoDetalleResponse> obtenerDetallePorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(pedidoService.obtenerDetallePorId(id));
    }

    @GetMapping("/comprobante/{comprobanteId}") //la variable dinámica definida acá, tienes que referenciarla abajo ↓
    public ResponseEntity<List<PedidoResponse>> obtenerPorComprobanteId(@PathVariable @Positive(message = "El id debe ser mayor a 0")Long comprobanteId){  //exactamente con el mismo nombre
        return ResponseEntity.ok(pedidoService.obtenerPorComprobanteId(comprobanteId));
    }

    @GetMapping("/comprobante/{comprobanteId}/detalle")
    public ResponseEntity<List<PedidoDetalleResponse>> obtenerDetallePorComprobanteId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long comprobanteId){
        return ResponseEntity.ok(pedidoService.obtenerDetallePorComprobanteId(comprobanteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoDetalleResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @RequestBody PedidoRequest request){
        return ResponseEntity.ok(pedidoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/listo")
    public ResponseEntity<String> marcarComoListo(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        pedidoService.marcarComoListo(id);
        return ResponseEntity.ok("Pedido marcado como 'LISTO' correctamente");
    }

    @PutMapping("/{id}/preparando")
    public ResponseEntity<String> marcarComoPreparando(@PathVariable Long id) {
        pedidoService.marcarComoPreparando(id);
        return ResponseEntity.ok("Pedido marcado como 'PREPARANDO' correctamente");
    }

    @PutMapping("/{id}/entregado")
    public ResponseEntity<String> marcarComoEntregado(@PathVariable Long id) {
        pedidoService.marcarComoEntregado(id);
        return ResponseEntity.ok("Pedido marcado como 'ENTREGADO' correctamente");
    }
}
