package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.TransaccionRequest;
import com.restobar.lapituca.service.TransaccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transacciones")
@RequiredArgsConstructor
@Validated
public class TransaccionController {

    private final TransaccionService transaccionService;

    @PostMapping
    public ResponseEntity<String> registrarPagoReserva(
            @Valid @RequestBody TransaccionRequest request){

        transaccionService.registrarPagoReserva(request);

        return ResponseEntity.ok("Pago registrado correctamente");
    }
}