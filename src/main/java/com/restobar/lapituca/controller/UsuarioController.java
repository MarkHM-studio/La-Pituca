package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.UsuarioRequest;
import com.restobar.lapituca.dto.UsuarioResponse;
import com.restobar.lapituca.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/usuario")
@RequiredArgsConstructor
@Validated
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request){
        UsuarioResponse usuarioResponse = usuarioService.guardar(request);

        URI location = URI.create("api/usuario/" + usuarioResponse.getUsuarioId());

        return ResponseEntity.created(location).body(usuarioResponse);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos(){
        List<UsuarioResponse> usuariosResponse = usuarioService.listarTodos();
        return ResponseEntity.ok(usuariosResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        UsuarioResponse usuarioResponse = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(usuarioResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody UsuarioRequest request){
        UsuarioResponse usuarioResponseActualizado = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(usuarioResponseActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
