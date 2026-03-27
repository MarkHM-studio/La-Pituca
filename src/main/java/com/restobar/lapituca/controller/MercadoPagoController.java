package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.request.mercadopago.CrearPreferenciaPagoRequest;
import com.restobar.lapituca.dto.response.mercadopago.CrearPreferenciaPagoResponse;
import com.restobar.lapituca.dto.response.mercadopago.WebhookProcesadoResponse;
import com.restobar.lapituca.service.MercadoPagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.util.Map;

@RestController
@RequestMapping("/api/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

    @PostMapping("/preferencias")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<CrearPreferenciaPagoResponse> crearPreferencia(
            @Valid @RequestBody CrearPreferenciaPagoRequest request
    ) {
        CrearPreferenciaPagoResponse response = mercadoPagoService.crearPreferenciaPago(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<WebhookProcesadoResponse> recibirWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId
    ) {
        mercadoPagoService.procesarWebhook(payload, xSignature, xRequestId);
        return ResponseEntity.ok(new WebhookProcesadoResponse("Webhook recibido"));
    }
}