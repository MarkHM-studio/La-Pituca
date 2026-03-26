package com.restobar.lapituca.dto.response.mercadopago;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPreferenciaPagoResponse {

    private Long transaccionId;
    private Long reservaId;
    private String externalReference;
    private String mercadoPagoPreferenceId;
    private String initPoint;
    private String sandboxInitPoint;
    private String estadoTransaccion;
}
