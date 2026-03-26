package com.restobar.lapituca.dto.response.mercadopago;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebhookProcesadoResponse {
    private String message;
}