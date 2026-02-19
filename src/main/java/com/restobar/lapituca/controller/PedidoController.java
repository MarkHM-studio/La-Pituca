package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.PedidoDetalleResponse;
import com.restobar.lapituca.dto.PedidoRequest;
import com.restobar.lapituca.dto.PedidoResponse;
import com.restobar.lapituca.entity.Pedido;
import com.restobar.lapituca.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pedido")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    private ResponseEntity<PedidoDetalleResponse> crear(@Valid @RequestBody PedidoRequest request){
        PedidoDetalleResponse detalleResponse = pedidoService.guardar(request);

        URI location = URI.create("/api/pedido/" + detalleResponse.getId());

        return ResponseEntity.created(location).body(detalleResponse);
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarTodos(){
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPorId(@PathVariable Long id){
        PedidoResponse response = pedidoService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<PedidoDetalleResponse> obtenerDetallePorId(@PathVariable Long id){
        PedidoDetalleResponse detalleResponse = pedidoService.obtenerDetallePorId(id);
        return ResponseEntity.ok(detalleResponse);
    }

    @GetMapping("/comprobante/{comprobanteId}") //la variable dinámica definida acá, tienes que referenciarla abajo ↓
    public ResponseEntity<List<PedidoResponse>> obtenerPorComprobanteId(@PathVariable Long comprobanteId){  //exactamente con el mismo nombre
        return ResponseEntity.ok(pedidoService.obtenerPorComprobanteId(comprobanteId));
    }

    @GetMapping("/comprobante/{comprobanteId}/detalle")
    public ResponseEntity<List<PedidoDetalleResponse>> obtenerDetallePorComprobanteId(@PathVariable Long comprobanteId){

        return ResponseEntity.ok(pedidoService.obtenerDetallePorComprobanteId(comprobanteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoDetalleResponse> actualizar(@PathVariable Long id, @RequestBody PedidoRequest request){
        PedidoDetalleResponse detalleResponseActualizado = pedidoService.actualizar(id, request);
        return ResponseEntity.ok(detalleResponseActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }


}
