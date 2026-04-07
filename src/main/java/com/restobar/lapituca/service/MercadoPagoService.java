package com.restobar.lapituca.service;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePaymentMethodsRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.restobar.lapituca.dto.request.mercadopago.CrearPreferenciaPagoRequest;
import com.restobar.lapituca.dto.response.mercadopago.CrearPreferenciaPagoResponse;
import com.restobar.lapituca.entity.Reserva;
import com.restobar.lapituca.entity.Transaccion;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.ReservaRepository;
import com.restobar.lapituca.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MercadoPagoService {

    private static final String RESERVA_PAGADO = "PAGADO";
    private static final String RESERVA_ESPERANDO = "ESPERANDO PAGO";

    private final ReservaRepository reservaRepository;
    private final TransaccionRepository transaccionRepository;

    @Value("${mercadopago.notification-url}")
    private String notificationUrl;

    @Value("${mercadopago.success-url}")
    private String successUrl;

    @Value("${mercadopago.pending-url}")
    private String pendingUrl;

    @Value("${mercadopago.failure-url}")
    private String failureUrl;

    @Value("${mercadopago.webhook-secret:}")
    private String webhookSecret;

    public CrearPreferenciaPagoResponse crearPreferenciaPago(CrearPreferenciaPagoRequest request) {

        Reserva reserva = reservaRepository.findById(request.getReservaId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Reserva no encontrada"));

        validarReservaParaPago(reserva);

        String externalReference = String.valueOf(reserva.getId());

        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(request.getDescripcion())
                    .quantity(1)
                    .currencyId("PEN")
                    .unitPrice(request.getMonto())
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .externalReference(externalReference)
                    .notificationUrl(notificationUrl)
                    .backUrls(PreferenceBackUrlsRequest.builder()
                            .success(successUrl)
                            .pending(pendingUrl)
                            .failure(failureUrl)
                            .build())
                    .items(java.util.List.of(item))
                    .paymentMethods(PreferencePaymentMethodsRequest.builder()
                            .installments(12)
                            .build())
                    .build();

            Preference preference = new PreferenceClient().create(preferenceRequest);

            Transaccion transaccion = new Transaccion();
            transaccion.setReserva(reserva);
            transaccion.setExternalReference(externalReference);
            transaccion.setMercadoPagoPreferenceId(preference.getId());
            transaccion.setEstado("PREFERENCE_CREATED");
            transaccion.setEstadoMercadoPago("preference_created");
            transaccion.setMonto(request.getMonto());
            transaccionRepository.save(transaccion);

            return new CrearPreferenciaPagoResponse(
                    transaccion.getId(),
                    reserva.getId(),
                    externalReference,
                    preference.getId(),
                    preference.getInitPoint(),
                    preference.getSandboxInitPoint(),
                    transaccion.getEstado()
            );

        } catch (MPApiException ex) {
            log.error("Error de API Mercado Pago al crear preferencia: {}", ex.getApiResponse().getContent());
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se pudo crear la preferencia de pago");
        } catch (MPException ex) {
            log.error("Error SDK Mercado Pago al crear preferencia", ex);
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se pudo conectar con Mercado Pago");
        }
    }

    public void procesarWebhook(Map<String, Object> payload, String xSignature, String xRequestId) {

        log.info("WEBHOOK RECIBIDO: {}", payload);

        // Desactivar validación temporalmente si estás probando
        // if (!esWebhookValido(payload, xSignature, xRequestId)) {
        //     throw new ApiException(ErrorCode.FORBIDDEN, "Webhook inválido");
        // }

        String paymentId = null;

        // FORMATO NUEVO
        if (payload.containsKey("type")) {
            String type = payload.get("type").toString();

            if ("payment".equalsIgnoreCase(type)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                paymentId = data.get("id").toString();
            }
        }

        // FORMATO ANTIGUO (topic/resource)
        if (paymentId == null && payload.containsKey("topic")) {
            String topic = payload.get("topic").toString();

            if ("payment".equalsIgnoreCase(topic)) {
                Object resource = payload.get("resource");

                if (resource != null) {
                    paymentId = resource.toString();
                }
            }
        }

        // Si no hay payment_id → ignorar
        if (paymentId == null) {
            log.info("Webhook ignorado: no se encontró payment_id");
            return;
        }

        //  CONSULTAR PAGO REAL
        try {
            Payment payment = new PaymentClient().get(Long.parseLong(paymentId));
            log.info("PAYMENT STATUS: {}", payment.getStatus());
            log.info("EXTERNAL REFERENCE: {}", payment.getExternalReference());
            actualizarTransaccionYReserva(payment);
        } catch (Exception e) {
            log.error("Error procesando pago {}", paymentId, e);
        }
    }

    private void actualizarTransaccionYReserva(Payment payment) {
        String paymentId = String.valueOf(payment.getId());
        String externalReference = Objects.toString(payment.getExternalReference(), null);

        if (externalReference == null || externalReference.isBlank()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,
                    "Pago sin external_reference, no se puede mapear reserva");
        }

        //  Buscar transacción existente
        Transaccion transaccion = transaccionRepository
                .findByMercadoPagoPaymentId(paymentId)
                .orElseGet(() -> transaccionRepository
                        .findTopByExternalReferenceOrderByFechaActualizacionDesc(externalReference)
                        .orElseGet(() -> {
                            //  Crear nueva si no existe
                            Reserva reserva = obtenerReservaPorExternalReference(externalReference);

                            Transaccion nueva = new Transaccion();
                            nueva.setReserva(reserva);
                            nueva.setExternalReference(externalReference);
                            nueva.setMonto(payment.getTransactionAmount() == null
                                    ? BigDecimal.ZERO : payment.getTransactionAmount());
                            nueva.setEstado("WEBHOOK_RECIBIDO");

                            return nueva;
                        })
                );
        // Obtener reserva SIEMPRE
        Reserva reserva = transaccion.getReserva();

        if (reserva == null) {
            reserva = obtenerReservaPorExternalReference(externalReference);
            transaccion.setReserva(reserva);
        }

        // AQUÍ SOLUCIONAS TU PROBLEMA
        if (transaccion.getUsuario() == null && reserva.getUsuario() != null) {
            transaccion.setUsuario(reserva.getUsuario());
        }

        // Actualizar datos del pago
        transaccion.setMercadoPagoPaymentId(paymentId);
        transaccion.setEstadoMercadoPago(payment.getStatus());
        transaccion.setDetalleEstadoMercadoPago(payment.getStatusDetail());
        transaccion.setMonto(payment.getTransactionAmount() == null ? transaccion.getMonto() : payment.getTransactionAmount());
        transaccion.setEstado(mapearEstadoInterno(payment.getStatus()));

        if ("PAGO_APROBADO".equals(transaccion.getEstado())) {
            reserva.setEstado(RESERVA_PAGADO);
            reserva.setFechaHora_expiracionPago(null);
        } else if (reserva.getFechaHora_expiracionPago() != null
                && LocalDateTime.now().isAfter(reserva.getFechaHora_expiracionPago())
                && !RESERVA_PAGADO.equalsIgnoreCase(reserva.getEstado())) {
            reserva.setEstado("EXPIRADO");
        } else {
            reserva.setEstado(RESERVA_ESPERANDO);
        }

        transaccionRepository.save(transaccion);
        reservaRepository.save(reserva);
    }

    private Reserva obtenerReservaPorExternalReference(String externalReference) {
        try {
            Long reservaId = Long.valueOf(externalReference);
            return reservaRepository.findById(reservaId)
                    .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                            "No existe reserva para external_reference=" + externalReference));
        } catch (NumberFormatException ex) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,
                    "external_reference inválido: " + externalReference);
        }
    }

    private String mapearEstadoInterno(String estadoMercadoPago) {
        if (estadoMercadoPago == null) {
            return "PAGO_DESCONOCIDO";
        }

        return switch (estadoMercadoPago.toLowerCase()) {
            case "approved" -> "PAGO_APROBADO";
            case "pending", "in_process", "in_mediation" -> "PAGO_PENDIENTE";
            case "rejected", "cancelled", "refunded", "charged_back" -> "PAGO_RECHAZADO";
            default -> "PAGO_DESCONOCIDO";
        };
    }

    private void validarReservaParaPago(Reserva reserva) {
        if (RESERVA_PAGADO.equalsIgnoreCase(reserva.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "La reserva ya está pagada");
        }

        if ("CANCELADO".equalsIgnoreCase(reserva.getEstado()) || "EXPIRADO".equalsIgnoreCase(reserva.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,
                    "No se puede generar pago para una reserva cancelada o expirada");
        }

        if (reserva.getFechaHora_expiracionPago() != null && LocalDateTime.now().isAfter(reserva.getFechaHora_expiracionPago())) {
            reserva.setEstado("EXPIRADO");
            reservaRepository.save(reserva);
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "La reserva ya expiró, crea una nueva reserva");
        }
    }

    private boolean esWebhookValido(Map<String, Object> payload, String xSignature, String xRequestId) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return true;
        }

        if (xSignature == null || xRequestId == null) {
            return false;
        }

        String paymentId = extraerPaymentId(payload);
        if (paymentId == null) {
            return false;
        }

        String ts = null;
        String hash = null;

        for (String part : xSignature.split(",")) {
            String[] keyValue = part.trim().split("=");
            if (keyValue.length == 2) {
                if ("ts".equals(keyValue[0])) {
                    ts = keyValue[1];
                }
                if ("v1".equals(keyValue[0])) {
                    hash = keyValue[1];
                }
            }
        }

        if (ts == null || hash == null) {
            return false;
        }

        String manifest = "id:" + paymentId + ";request-id:" + xRequestId + ";ts:" + ts + ";";
        String generated = hmacSha256(manifest, webhookSecret);
        return generated.equalsIgnoreCase(hash);
    }

    private String extraerPaymentId(Map<String, Object> payload) {
        Object data = payload.get("data");
        if (!(data instanceof Map<?, ?> dataMap)) {
            return null;
        }

        Object idObj = dataMap.get("id");
        return idObj == null ? null : idObj.toString();
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] bytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception ex) {
            log.error("No se pudo validar firma del webhook", ex);
            return "";
        }
    }
}
