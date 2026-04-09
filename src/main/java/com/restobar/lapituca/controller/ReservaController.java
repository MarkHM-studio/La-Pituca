package com.restobar.lapituca.controller;
import com.restobar.lapituca.dto.request.ReservaRequest;
import com.restobar.lapituca.dto.response.MesasDisponiblesResponse;
import com.restobar.lapituca.dto.response.ReservaResponse;
import com.restobar.lapituca.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/reserva")
@RequiredArgsConstructor
@Validated
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest request){
        ReservaResponse reservaResponse = reservaService.crear(request);

        URI location = URI.create("/api/reserva/" + reservaResponse.getId());

        return ResponseEntity.created(location).body(reservaResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ReservaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReservaRequest request){

        return ResponseEntity.ok(reservaService.actualizar(id,request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReservaResponse>> listar(Authentication authentication){
        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));

        if (esCliente) {
            return ResponseEntity.ok(reservaService.listarPorUsername(authentication.getName()));
        }
        return ResponseEntity.ok(reservaService.listar());
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'RECEPCIONISTA')")
    public ResponseEntity<ReservaResponse> obtenerPorId(
            @PathVariable Long id,
            Authentication authentication){
        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));

        if (esCliente) {
            return ResponseEntity.ok(reservaService.obtenerPorIdParaUsername(id, authentication.getName()));
        }

        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @GetMapping("/mesas-disponibles")
    @PreAuthorize("hasAnyRole('CLIENTE', 'RECEPCIONISTA')")
    public ResponseEntity<List<MesasDisponiblesResponse>> verMesasDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime hora) {
        return ResponseEntity.ok(reservaService.verMesasDisponibles(fecha, hora));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'RECEPCIONISTA')")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Authentication authentication){
        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));
        reservaService.cancelar(id, authentication.getName(), esCliente);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/verificar")
    @PreAuthorize("hasRole('RECEPCIONISTA')")
    public ResponseEntity<ReservaResponse> verificarReserva(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(reservaService.verificarReserva(id, authentication.getName()));
    }
}