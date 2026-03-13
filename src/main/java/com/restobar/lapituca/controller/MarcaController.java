package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.MarcaRequest;
import com.restobar.lapituca.dto.response.MarcaResponse;
import com.restobar.lapituca.service.MarcaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/marca")
@RequiredArgsConstructor
@Validated
public class MarcaController {

    private final MarcaService marcaService;

    @PostMapping()
    public ResponseEntity<MarcaResponse> crear(@Valid @RequestBody MarcaRequest request){
        MarcaResponse marcaResponse= marcaService.guardar(request);

        URI location = URI.create("/api/marca/" + marcaResponse.getId());

        return ResponseEntity.created(location).body(marcaResponse);
    }
    /*
    @PostMapping
    public ResponseEntity<MarcaResponse> crear(@Valid @RequestBody Marca marca) {
        Marca nuevaMarca = marcaService.guardar(marca);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaMarca);
    }*/
    /*
    @PostMapping
    public Marca crear(@Valid @RequestBody Marca marca){
        marca.setFecha_inscripcion(LocalDate.now());
        return marcaService.guardar(marca);
    }*/
    @GetMapping
    public ResponseEntity<List<MarcaResponse>> listarTodos(){
        return ResponseEntity.ok(marcaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponse> obtenerPorId(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        return ResponseEntity.ok(marcaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponse> actualizar(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id, @Valid @RequestBody MarcaRequest request){
        return ResponseEntity.ok(marcaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive(message = "El id debe ser mayor a 0") Long id){
        marcaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
