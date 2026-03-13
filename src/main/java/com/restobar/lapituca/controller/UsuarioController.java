package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.UsuarioRequest;
import com.restobar.lapituca.dto.response.UsuarioResponse;
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

        URI location = URI.create("api/usuario/" + usuarioResponse.getId());

        return ResponseEntity.created(location).body(usuarioResponse);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos(){
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody UsuarioRequest request){
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
