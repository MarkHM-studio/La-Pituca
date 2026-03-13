package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.AsignarMesasRequest;
import com.restobar.lapituca.dto.request.ComprobanteRequest;
import com.restobar.lapituca.dto.request.RegistrarVentaRequest;
import com.restobar.lapituca.dto.response.ComprobanteResponse;
import com.restobar.lapituca.dto.response.DetalleMesaResponse;
import com.restobar.lapituca.dto.response.GrupoResponse;
import com.restobar.lapituca.entity.*;
import com.restobar.lapituca.exception.*;
import com.restobar.lapituca.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final GrupoRepository grupoRepository;
    private final MesaRepository mesaRepository;
    private final DetalleMesaRepository detalleMesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoPagoRepository tipoPagoRepository;
    private final TipoBilleteraVirtualRepository tipoBilleteraVirtualRepository;
    private final PedidoRepository pedidoRepository;
    private final MovimientoTipoPagoRepository movimientoTipoPagoRepository;
    private final SucursalRepository sucursalRepository;


    @Transactional
    public ComprobanteResponse crearComprobante(ComprobanteRequest request) {

        Sucursal sucursal = sucursalRepository.findById(request.getSucursalId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Sucursal con id: "+request.getSucursalId()+" no encontrado"));

        Comprobante comprobante = new Comprobante();
        comprobante.setTotal(BigDecimal.ZERO);
        comprobante.setIGV(BigDecimal.ZERO);
        comprobante.setEstado("ABIERTO");
        comprobante.setSucursal(sucursal);

        comprobanteRepository.save(comprobante);

        return new ComprobanteResponse(
                comprobante.getId(),
                comprobante.getTotal(),
                comprobante.getIGV(),
                comprobante.getFechaHora_venta(),
                comprobante.getEstado(),
                null
        );
    }

    @Transactional
    public ComprobanteResponse asignarGrupoYMesasSiEsComer(AsignarMesasRequest request) {

        //Buscar comprobante
        Comprobante comprobante = comprobanteRepository.findById(request.getComprobanteId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Comprobante con id: "+request.getComprobanteId()+" no encontrado"));

        //Validar que tenga pedidos
        List<Pedido> pedidos = comprobante.getPedidos();

        if (pedidos == null || pedidos.isEmpty()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se puede asignar mesas a un comprobante sin pedidos");
        }

        //Verificar si al menos 1 pedido es tipo COMER
        boolean tienePedidoComer = pedidos.stream()
                .anyMatch(p ->
                        p.getTipoEntrega() != null &&
                                "COMER".equalsIgnoreCase(p.getTipoEntrega().getNombre())
                );

        //Si ninguno es COMER, no se asigna grupo (regla de negocio correcta)
        if (!tienePedidoComer) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,
                    "No se puede asignar mesas porque todos los pedidos son para LLEVAR"
            );
        }

        //Si ya tiene grupo, no crear otro (evita duplicados)
        if (comprobante.getGrupo() != null) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Este comprobante ya tiene un grupo asignado");
        }

        //Crear grupo
        Grupo grupo = new Grupo();
        grupo.setNombre(
                request.getNombreGrupo() == null || request.getNombreGrupo().isBlank()
                        ? "NA"
                        : request.getNombreGrupo()
        );
        grupoRepository.save(grupo);

        // Validar mesas
        if (request.getMesasId() == null || request.getMesasId().isEmpty()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Debe enviar al menos una mesa");
        }

        Set<Long> mesasUnicas = new HashSet<>(request.getMesasId());

        // Asignar mesas al grupo
        for (Long mesaId : mesasUnicas) {

            Mesa mesa = mesaRepository.findById(mesaId)
                    .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Mesa con id: "+mesaId+" no encontrada"));

            if ("OCUPADO".equalsIgnoreCase(mesa.getEstado())) {
                throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"La mesa " + mesa.getNombre() + " ya está ocupada");
            }

            // Marcar mesa ocupada
            mesa.setEstado("OCUPADO");
            mesaRepository.save(mesa);

            // Crear detalle mesa
            DetalleMesa detalleMesa = new DetalleMesa();
            detalleMesa.setGrupo(grupo);
            detalleMesa.setMesa(mesa);
            detalleMesaRepository.save(detalleMesa);
        }

        // Asociar grupo al comprobante
        comprobante.setGrupo(grupo);
        comprobanteRepository.save(comprobante);

        // Mapear a DTO ComprobanteResponse
        Grupo g = comprobante.getGrupo();
        GrupoResponse grupoResponse = null;

        if (g != null) {
            grupoResponse = new GrupoResponse(
                    grupo.getId(),
                    grupo.getNombre(),
                    detalleMesaRepository.findByGrupo_Id(grupo.getId())
                            .stream()
                            .map(detalleMesa -> new DetalleMesaResponse(
                                    detalleMesa.getId(),
                                    detalleMesa.getGrupo().getId(),
                                    detalleMesa.getMesa().getId()
                            ))
                            .toList()
            );
        }

        return new ComprobanteResponse(
                comprobante.getId(),
                comprobante.getTotal(),
                comprobante.getIGV(),
                comprobante.getFechaHora_venta(),
                comprobante.getEstado(),
                grupoResponse
        );
    }

    public void eliminar(Long id){
        comprobanteRepository.deleteById(id);
    }

    @Transactional
    public String registrarVenta(RegistrarVentaRequest request) {

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Usuario con id: "+request.getUsuarioId()+" no encontrado"));

        Comprobante comprobante = comprobanteRepository.findById(request.getComprobanteId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Comprobante con id: "+request.getComprobanteId()+" no encontrado"));

        if ("PAGADO".equalsIgnoreCase(comprobante.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "El Comprobante ya fue pagado");
        }

        BigDecimal totalPagar = comprobante.getTotal();

        Set<Long> tiposPago = request.getTipoPagoId();
        List<BigDecimal> montos = request.getMontos();

        if (tiposPago.size() != montos.size()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "La cantidad de tipos de pago y montos no coincide");
        }

        List<Long> tiposPagoList = new ArrayList<>(tiposPago);

        BigDecimal sumaPagos = montos.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean tieneEfectivo = tiposPagoList.contains(1L);
        boolean tieneBilletera = tiposPagoList.contains(2L);

        // Validación billetera (debe coincidir exactamente)
        if (tieneBilletera && !tieneEfectivo) {

            if (sumaPagos.compareTo(totalPagar) != 0) {
                throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "El monto de billetera debe coincidir con el total: " + totalPagar);}
        }

        // Validación general
        if (sumaPagos.compareTo(totalPagar) < 0) {

            BigDecimal falta = totalPagar.subtract(sumaPagos);

            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Falta pagar: " + falta);
        }

        BigDecimal vuelto = sumaPagos.subtract(totalPagar);

        registrarMovimientos(
                tiposPagoList,
                montos,
                comprobante,
                request.getTipoBilleteraVirtualId()
        );

        cerrarComprobante(comprobante, usuario);

        if (vuelto.compareTo(BigDecimal.ZERO) > 0) {
            return "Pago realizado correctamente. Vuelto: " + vuelto;
        }

        return "Pago realizado correctamente";
    }
    /*
    @Transactional
    public String registrarVenta(RegistrarVentaRequest request) {

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: "+request.getUsuarioId()+" no encontrado"));

        Comprobante comprobante = comprobanteRepository.findById(request.getComprobanteId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Comprobante con id: "+request.getComprobanteId()+" no encontrado"));

        if ("PAGADO".equalsIgnoreCase(comprobante.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "El comprobante ya fue pagado");
        }

        BigDecimal totalPagar = comprobante.getTotal();

        Set<Long> tiposPago = request.getTipoPagoId();
        List<BigDecimal> montos = request.getMontos();

        if (tiposPago.size() != montos.size()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,"La cantidad de tipos de pago y montos no coincide");
        }

        List<Long> tiposPagoList = new ArrayList<>(tiposPago);

        BigDecimal sumaPagos = montos.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // CASO 1: SOLO EFECTIVO (id = 1)
        if (tiposPagoList.size() == 1 && tiposPagoList.contains(1L)) {

            BigDecimal montoRecibido = montos.get(0);

            List<BigDecimal> montoPagado = Collections.singletonList(totalPagar);

            if (montoRecibido.compareTo(totalPagar) < 0) {
                BigDecimal falta = totalPagar.subtract(montoRecibido);
                throw new ApiException(ErrorCode.VALIDATION_ERROR,"Falta pagar: " + falta);
            }

            BigDecimal vuelto = montoRecibido.subtract(totalPagar);

            registrarMovimientos(tiposPagoList, montoPagado, comprobante, request.getTipoBilleteraVirtualId());

            cerrarComprobante(comprobante, usuario);

            return "Pago realizado correctamente. Vuelto: " + vuelto;
        }

        // CASO 2: SOLO BILLETERA (id = 2)
        if (tiposPagoList.size() == 1 && tiposPagoList.contains(2L)) {

            BigDecimal montoRecibido = montos.get(0);

            if (montoRecibido.compareTo(totalPagar) != 0) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR,"El monto debe coincidir exactamente con el total: " + totalPagar);
            }

            registrarMovimientos(tiposPagoList, montos, comprobante, request.getTipoBilleteraVirtualId());

            cerrarComprobante(comprobante, usuario);

            return "Pago realizado correctamente";
        }

        // CASO 3: MIXTO
        if (tiposPagoList.size() == 2) {

            if (sumaPagos.compareTo(totalPagar) < 0) {
                BigDecimal falta = totalPagar.subtract(sumaPagos);
                throw new ApiException(ErrorCode.VALIDATION_ERROR,"Falta pagar: " + falta);
            }

            BigDecimal vuelto = sumaPagos.subtract(totalPagar);

            registrarMovimientos(tiposPagoList, montos, comprobante, request.getTipoBilleteraVirtualId());

            cerrarComprobante(comprobante, usuario);

            return "Pago realizado correctamente. Vuelto: " + vuelto;
        }

        throw new ApiException(ErrorCode.VALIDATION_ERROR,"Tipo de pago no válido");
    }*/
    private void registrarMovimientos(List<Long> tiposPago, List<BigDecimal> montos, Comprobante comprobante, Long tipoBilleteraVirtualId) {

        for (int i = 0; i < tiposPago.size(); i++) {

            Long tipoPagoId = tiposPago.get(i);

            TipoPago tipoPago = tipoPagoRepository.findById(tiposPago.get(i))
                    .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"TipoPago con id: "+tipoPagoId+" no encontrado"));

            MovimientoTipoPago movimiento = new MovimientoTipoPago();
            movimiento.setComprobante(comprobante);
            movimiento.setTipoPago(tipoPago);
            movimiento.setMonto(montos.get(i));

            if (tipoPagoId.equals(2L)) {
                TipoBilleteraVirtual billetera = tipoBilleteraVirtualRepository
                        .findById(tipoBilleteraVirtualId)
                        .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"TipoBilleteraVirtual con id: "+tipoBilleteraVirtualId+ " no encontrado"));
                movimiento.setTipoBilleteraVirtual(billetera);
            }

            movimientoTipoPagoRepository.save(movimiento);
        }
    }

    private void cerrarComprobante(Comprobante comprobante, Usuario usuario) {

        comprobante.setEstado("PAGADO");
        comprobante.setFechaHora_venta(LocalDateTime.now());
        comprobante.setUsuario(usuario);

        comprobanteRepository.save(comprobante);

        // Actualizar pedidos
        comprobante.getPedidos().forEach(p -> {
            p.setEstado("PAGADO");
            pedidoRepository.save(p);
        });

        // Liberar mesas
        if (comprobante.getGrupo() != null) {

            List<DetalleMesa> detalles =
                    detalleMesaRepository.findByGrupo_Id(comprobante.getGrupo().getId());

            for (DetalleMesa d : detalles) {
                Mesa mesa = d.getMesa();
                mesa.setEstado("DESOCUPADO");
                mesaRepository.save(mesa);
            }
        }
    }
}
