package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.AsignarMesasRequest;
import com.restobar.lapituca.dto.request.ComprobanteRequest;
import com.restobar.lapituca.dto.request.RegistrarVentaRequest;
import com.restobar.lapituca.dto.response.*;
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
    private final MovimientoInsumoRepository movimientoInsumoRepository;
    private final RecetaRepository recetaRepository;
    private final SucursalRepository sucursalRepository;
    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;


    @Transactional
    public ComprobanteResponse crearComprobante(ComprobanteRequest request) {

        Sucursal sucursal = sucursalRepository.findById(request.getSucursalId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Sucursal con id: "+request.getSucursalId()+" no encontrado"));

        Comprobante comprobante = new Comprobante();
        comprobante.setTotal(BigDecimal.ZERO);
        comprobante.setIGV(BigDecimal.ZERO);
        comprobante.setEstado("ABIERTO");
        comprobante.setSucursal(sucursal);

        comprobante = comprobanteRepository.save(comprobante);

        return new ComprobanteResponse(
                comprobante.getId(),
                comprobante.getTotal(),
                comprobante.getIGV(),
                comprobante.getFechaHora_apertura(),
                comprobante.getFechaHora_venta(),
                comprobante.getEstado(),
                null
        );
    }

    public List<ComprobanteListadoResponse> listarTodos(){
        return comprobanteRepository.findAll().stream()
                .filter(c -> !"ELIMINADO".equalsIgnoreCase(c.getEstado()))
                .map(this::mapToListadoResponse)
                .toList();
    }

    public ComprobanteDetalleResponse obtenerDetallePorId(Long id) {
        Comprobante comprobante = comprobanteRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Comprobante con id: " + id + " no encontrado"));

        if ("ELIMINADO".equalsIgnoreCase(comprobante.getEstado())) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Comprobante con id: " + id + " no disponible");
        }

        return mapToDetalleResponse(comprobante);
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

        Grupo grupo = new Grupo();
        grupo.setNombre(
                request.getNombreGrupo() == null || request.getNombreGrupo().isBlank()
                        ? "NA"
                        : request.getNombreGrupo()
        );

        grupo.setEstado("ACTIVO");
        grupo.setTipoGrupo(1);

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
                comprobante.getFechaHora_apertura(),
                comprobante.getFechaHora_venta(),
                comprobante.getEstado(),
                grupoResponse
        );
    }

    public void eliminar(Long id){
        Comprobante comprobante = comprobanteRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Comprobante con id: " + id + " no encontrado"));

        if ("PAGADO".equalsIgnoreCase(comprobante.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se puede eliminar lógicamente un comprobante pagado");
        }

        comprobante.setEstado("ELIMINADO");
        comprobanteRepository.save(comprobante);
    }

    @Transactional
    public String registrarVenta(RegistrarVentaRequest request) {

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Usuario con id: "+request.getUsuarioId()+" no encontrado"));

        if (!"CAJERO".equalsIgnoreCase(usuario.getRol().getNombre())) {
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "Solo los usuarios con rol CAJERO pueden registrar ventas"
            );
        }

        Comprobante comprobante = comprobanteRepository.findById(request.getComprobanteId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Comprobante con id: "+request.getComprobanteId()+" no encontrado"));

        if ("PAGADO".equalsIgnoreCase(comprobante.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "El Comprobante ya fue pagado");
        }
        if ("ELIMINADO".equalsIgnoreCase(comprobante.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se puede registrar la venta de un comprobante eliminado");
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

        validarDocumentoComprobante(request);

        registrarMovimientos(
                tiposPagoList,
                montos,
                comprobante,
                request.getTipoBilleteraVirtualId()
        );

        registrarMovimientoInsumos(comprobante);
        cerrarComprobante(comprobante, usuario);

        if (vuelto.compareTo(BigDecimal.ZERO) > 0) {
            return "Pago realizado correctamente. Vuelto: " + vuelto;
        }

        return "Pago realizado correctamente";
    }
    private void validarDocumentoComprobante(RegistrarVentaRequest request) {
        String tipoComprobante = request.getTipoComprobante() == null ? "" : request.getTipoComprobante().trim().toUpperCase();

        if (!tipoComprobante.equals("BOLETA") && !tipoComprobante.equals("FACTURA")) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "El tipoComprobante debe ser BOLETA o FACTURA");
        }

        if ("BOLETA".equals(tipoComprobante)) {
            if (request.getDni() == null || !request.getDni().matches("\\d{8}")) {
                throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Para BOLETA debe enviar un DNI válido de 8 dígitos");
            }
            if (request.getRuc() != null && !request.getRuc().isBlank()) {
                throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Para BOLETA no debe enviar RUC");
            }
            return;
        }

        if (request.getRuc() == null || !request.getRuc().matches("\\d{11}")) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Para FACTURA debe enviar un RUC válido de 11 dígitos");
        }
        if (request.getDni() != null && !request.getDni().isBlank()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Para FACTURA no debe enviar DNI");
        }
    }

    private ComprobanteListadoResponse mapToListadoResponse(Comprobante comprobante) {
        return new ComprobanteListadoResponse(
                comprobante.getId(),
                comprobante.getTotal(),
                comprobante.getIGV(),
                comprobante.getFechaHora_apertura(),
                comprobante.getFechaHora_venta(),
                comprobante.getEstado(),
                comprobante.getSucursal() != null ? comprobante.getSucursal().getId() : null,
                comprobante.getUsuario() != null ? comprobante.getUsuario().getId() : null
        );
    }

    private ComprobanteDetalleResponse mapToDetalleResponse(Comprobante comprobante) {
        SucursalResumenResponse sucursal = null;
        if (comprobante.getSucursal() != null) {
            sucursal = new SucursalResumenResponse(
                    comprobante.getSucursal().getId(),
                    comprobante.getSucursal().getNombre(),
                    comprobante.getSucursal().getDireccion(),
                    comprobante.getSucursal().getRUC()
            );
        }

        UsuarioResumenResponse cajero = null;
        if (comprobante.getUsuario() != null) {
            cajero = new UsuarioResumenResponse(
                    comprobante.getUsuario().getId(),
                    comprobante.getUsuario().getUsername(),
                    comprobante.getUsuario().getRol() != null ? comprobante.getUsuario().getRol().getNombre() : null
            );
        }

        Grupo grupo = comprobante.getGrupo();
        GrupoDetalleResponse grupoResponse = null;

        if (grupo != null) {
            List<MesaAsignadaResponse> detalles = detalleMesaRepository.findByGrupo_Id(grupo.getId())
                    .stream()
                    .map(detalleMesa -> new MesaAsignadaResponse(
                            detalleMesa.getId(),
                            detalleMesa.getMesa().getId(),
                            detalleMesa.getMesa().getNombre(),
                            detalleMesa.getMesa().getEstado()
                    ))
                    .toList();

            grupoResponse = new GrupoDetalleResponse(
                    grupo.getId(),
                    grupo.getNombre(),
                    grupo.getEstado(),
                    grupo.getTipoGrupo(),
                    detalles
            );
        }

        ReservaDetalleEnComprobanteResponse reservaResponse = null;
        if (grupo != null) {
            reservaResponse = reservaRepository.findByGrupo_Id(grupo.getId())
                    .map(reserva -> {
                        ClienteResumenResponse clienteResponse = null;
                        if (reserva.getUsuario() != null) {
                            clienteResponse = clienteRepository.findByUsuario(reserva.getUsuario())
                                    .map(cliente -> new ClienteResumenResponse(
                                            cliente.getId(),
                                            cliente.getNombre() + " " + cliente.getApellido(),
                                            cliente.getCorreo(),
                                            cliente.getTelefono()
                                    ))
                                    .orElse(null);
                        }
                        return new ReservaDetalleEnComprobanteResponse(
                                reserva.getId(),
                                reserva.getFecha_reserva(),
                                reserva.getHora_reserva(),
                                reserva.getNum_personas(),
                                reserva.getEstado(),
                                reserva.getFechaHora_registro(),
                                reserva.getFechaHora_verificacionReserva(),
                                clienteResponse
                        );
                    })
                    .orElse(null);
        }

        List<PedidoEnComprobanteResponse> pedidos = pedidoRepository.findByComprobante_Id(comprobante.getId())
                .stream()
                .map(pedido -> new PedidoEnComprobanteResponse(
                        pedido.getId(),
                        pedido.getCantidad(),
                        pedido.getPrecio_unitario(),
                        pedido.getSubtotal(),
                        pedido.getEstado(),
                        pedido.getProducto() != null ? pedido.getProducto().getId() : null,
                        pedido.getProducto() != null ? pedido.getProducto().getNombre() : null,
                        pedido.getTipoEntrega() != null ? pedido.getTipoEntrega().getId() : null,
                        pedido.getTipoEntrega() != null ? pedido.getTipoEntrega().getNombre() : null,
                        pedido.getUsuario() != null ? pedido.getUsuario().getId() : null,
                        pedido.getUsuario() != null ? pedido.getUsuario().getUsername() : null
                ))
                .toList();

        List<MovimientoTipoPagoDetalleResponse> movimientosTipoPago = movimientoTipoPagoRepository
                .findByComprobante_Id(comprobante.getId()).stream()
                .map(movimiento -> new MovimientoTipoPagoDetalleResponse(
                        movimiento.getId(),
                        movimiento.getMonto(),
                        movimiento.getTipoPago() != null ? movimiento.getTipoPago().getId() : null,
                        movimiento.getTipoPago() != null ? movimiento.getTipoPago().getNombre() : null,
                        movimiento.getTipoBilleteraVirtual() != null ? movimiento.getTipoBilleteraVirtual().getId() : null,
                        movimiento.getTipoBilleteraVirtual() != null ? movimiento.getTipoBilleteraVirtual().getNombre() : null
                ))
                .toList();

        return new ComprobanteDetalleResponse(
                comprobante.getId(),
                comprobante.getTotal(),
                comprobante.getIGV(),
                comprobante.getSubtotal(),
                comprobante.getFechaHora_apertura(),
                comprobante.getFechaHora_venta(),
                comprobante.getEstado(),
                sucursal,
                cajero,
                grupoResponse,
                reservaResponse,
                pedidos,
                movimientosTipoPago
        );
    }

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

        if (comprobante.getGrupo() != null) {

            Grupo grupo = comprobante.getGrupo();

            grupo.setEstado("CERRADO");

            grupoRepository.save(grupo);

        }
    }

    private void registrarMovimientoInsumos(Comprobante comprobante) {

        Map<Long, BigDecimal> acumuladoPorInsumo = new HashMap<>();
        Map<Long, Insumo> insumoMap = new HashMap<>();

        List<Pedido> pedidos = pedidoRepository.findByComprobante_Id(comprobante.getId());

        for (Pedido pedido : pedidos) {
            Producto producto = pedido.getProducto();
            if (producto.getCategoria() == null || (producto.getCategoria().getId() != 1L && producto.getCategoria().getId() != 2L)) {
                continue;
            }

            List<Receta> recetas = recetaRepository.findByProducto_IdOrderByIdAsc(producto.getId());

            for (Receta receta : recetas) {
                Insumo insumo = receta.getInsumo();
                BigDecimal cantidad = convertir(receta.getCantidad().multiply(BigDecimal.valueOf(pedido.getCantidad())), receta.getUnidad_medida(), insumo.getUnidad_medida());
                acumuladoPorInsumo.merge(insumo.getId(), cantidad, BigDecimal::add);
                insumoMap.put(insumo.getId(), insumo);
            }
        }

        for (Map.Entry<Long, BigDecimal> item : acumuladoPorInsumo.entrySet()) {
            Insumo insumo = insumoMap.get(item.getKey());
            if (insumo == null) continue;

            MovimientoInsumo mov = new MovimientoInsumo();
            mov.setComprobante(comprobante);
            mov.setInsumo(insumo);
            mov.setCantidad(item.getValue());
            mov.setUnidad_medida(insumo.getUnidad_medida());
            movimientoInsumoRepository.save(mov);
        }
    }

    private BigDecimal convertir(BigDecimal cantidad, String source, String target) {
        String from = source.trim().toUpperCase();
        String to = target.trim().toUpperCase();
        if (from.equals(to)) return cantidad;
        if ((from.equals("G") || from.equals("GRAMOS")) && (to.equals("KG") || to.equals("KILOGRAMOS"))) {
            return cantidad.divide(new BigDecimal("1000"), 6, java.math.RoundingMode.HALF_UP);
        }
        if ((from.equals("KG") || from.equals("KILOGRAMOS")) && (to.equals("G") || to.equals("GRAMOS"))) {
            return cantidad.multiply(new BigDecimal("1000"));
        }
        if (from.equals("ML") && (to.equals("L") || to.equals("LITROS"))) {
            return cantidad.divide(new BigDecimal("1000"), 6, java.math.RoundingMode.HALF_UP);
        }
        if ((from.equals("L") || from.equals("LITROS")) && to.equals("ML")) {
            return cantidad.multiply(new BigDecimal("1000"));
        }
        if ((from.equals("UD") || from.equals("UDS") || from.equals("UNIDAD") || from.equals("UNIDADES"))
                && (to.equals("UD") || to.equals("UDS") || to.equals("UNIDAD") || to.equals("UNIDADES"))) {
            return cantidad;
        }
        throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Unidad incompatible para movimiento de insumo: " + source + " -> " + target);
    }
}
