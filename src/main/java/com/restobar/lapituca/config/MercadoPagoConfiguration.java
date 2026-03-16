package com.restobar.lapituca.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoConfiguration {

    @Value("${mercadopago.access_token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        System.out.println("TOKEN: " + accessToken);
        MercadoPagoConfig.setAccessToken(accessToken);
    }
}
