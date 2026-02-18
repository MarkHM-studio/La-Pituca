package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.PedidoRequest;
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
    private ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequest request){
        Pedido pedido = pedidoService.guardar(request);

        URI location = URI.create("/api/pedido/" + pedido.getId());

        return ResponseEntity.created(location).body(pedido);
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos(){

        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Pedido>> obtenerPorComprobanteId(@PathVariable Long id){

        return ResponseEntity.ok(pedidoService.obtenerPorComprobanteId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pedido> actualizar(@PathVariable Long id, @RequestBody PedidoRequest request){

        Pedido pedidoActualizado = pedidoService.actualizar(id, request);
        return ResponseEntity.ok(pedidoActualizado);
    }


}
