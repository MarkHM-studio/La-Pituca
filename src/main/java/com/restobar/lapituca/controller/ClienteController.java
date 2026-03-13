package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.ClienteRequest;
import com.restobar.lapituca.dto.request.UsuarioClienteRequest;
import com.restobar.lapituca.dto.response.ClienteResponse;
import com.restobar.lapituca.service.ClienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cliente")
@RequiredArgsConstructor
@Validated
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse clienteResponse = clienteService.guardar(request);

        URI location = URI.create("/api/cliente" + clienteResponse.getId());

        return ResponseEntity.created(location).body(clienteResponse);
    }

    @PostMapping("/registrar")
    public ResponseEntity<ClienteResponse> registrar(@Valid @RequestBody UsuarioClienteRequest request) {
        ClienteResponse clienteResponse = clienteService.registrar(request);

        URI location = URI.create("/api/cliente" + clienteResponse.getId());

        return ResponseEntity.created(location).body(clienteResponse);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerPorId(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, UsuarioClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
