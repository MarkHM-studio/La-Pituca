package com.restobar.lapituca.controller;

import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pago")
@CrossOrigin(origins = "http://localhost:3000")
public class MercadoPagoController {

    @PostMapping
    public Map<String, String> pagar() throws Exception{

        try {
            PreferenceItemRequest item =
                    PreferenceItemRequest.builder()
                            .title("Producto de prueba")
                            .quantity(1)
                            .currencyId("PEN")
                            .unitPrice(new BigDecimal("50"))
                            .build();

            PreferenceRequest preferenceRequest =
                    PreferenceRequest.builder()
                            .items(List.of(item))
                            .build();

            PreferenceRequest.builder()
                    .items(List.of(item))
                    .notificationUrl("http://localhost:8080/api/webhook");

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return Map.of("url", preference.getInitPoint());
        } catch (MPApiException e) {
            System.out.println(e.getApiResponse().getContent());
            return Map.of("error", e.getApiResponse().getContent());
        }
    }
}
