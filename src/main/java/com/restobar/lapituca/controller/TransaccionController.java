package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.TransaccionRequest;
import com.restobar.lapituca.dto.response.TransaccionResponse;
import com.restobar.lapituca.service.TransaccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transacciones")
@RequiredArgsConstructor
@Validated
public class TransaccionController {

    private final TransaccionService transaccionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ADMINISTRADOR')")
    public ResponseEntity <List<TransaccionResponse>> listarTodos(){
        return ResponseEntity.ok(transaccionService.listarTodos());
    }
}