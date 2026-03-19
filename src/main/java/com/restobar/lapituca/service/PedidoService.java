package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.PedidoRequest;
import com.restobar.lapituca.dto.response.*;
import com.restobar.lapituca.entity.*;
import com.restobar.lapituca.exception.*;
import com.restobar.lapituca.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final TipoEntregaRepository tipoEntregaRepository;
    private final DetalleMesaRepository detalleMesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final RecetaRepository recetaRepository;
    private final InsumoRepository insumoRepository;

    @Transactional
    public PedidoDetalleResponse guardar(PedidoRequest request) {
        Comprobante comprobante = findComprobante(request.getComprobanteId());
        Usuario usuario = findUsuario(request.getUsuarioId());

        if (!"MOZO".equalsIgnoreCase(usuario.getRol().getNombre())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Solo los usuarios con rol MOZO pueden registrar pedidos");
        }
        if ("PAGADO".equalsIgnoreCase(comprobante.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se pueden agregar pedidos a un comprobante pagado");
        }

        Producto producto = findProducto(request.getProductoId());
        TipoEntrega tipoEntrega = findTipoEntrega(request.getTipoEntregaId());

        descontarStockProductoEInsumos(producto, request.getCantidad());

        BigDecimal precioUnitario = producto.getPrecio();
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(request.getCantidad()));

        Pedido pedido = new Pedido();
        pedido.setCantidad(request.getCantidad());
        pedido.setProducto(producto);
        pedido.setComprobante(comprobante);
        pedido.setPrecio_unitario(precioUnitario);
        pedido.setSubtotal(subtotal);
        pedido.setEstado("PENDIENTE");
        pedido.setTipoEntrega(tipoEntrega);
        pedido.setUsuario(usuario);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        recalcularTotalesComprobante(comprobante.getId());

        return mapToPedidoDetalleResponse(pedidoGuardado);
    }

    private void recalcularTotalesComprobante(Long comprobanteId) {
        Comprobante comprobante = findComprobante(comprobanteId);
        List<Pedido> pedidos = pedidoRepository.findByComprobante_Id(comprobanteId);

        BigDecimal subtotal = pedidos.stream().map(Pedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18"));
        BigDecimal total = subtotal.add(igv);

        comprobante.setSubtotal(subtotal);
        comprobante.setIGV(igv);
        comprobante.setTotal(total);
        comprobanteRepository.save(comprobante);
    }

    private PedidoDetalleResponse mapToPedidoDetalleResponse(Pedido pedido) {
        Grupo grupo = pedido.getComprobante().getGrupo();
        GrupoResponse grupoResponse = null;

        if (grupo != null) {
            grupoResponse = new GrupoResponse(
                    grupo.getId(),
                    grupo.getNombre(),
                    detalleMesaRepository.findByGrupo_Id(grupo.getId()).stream()
                            .map(detalleMesa -> new DetalleMesaResponse(detalleMesa.getId(), detalleMesa.getGrupo().getId(), detalleMesa.getMesa().getId()))
                            .toList()
            );
        }

        MarcaResponse marcaResponse = pedido.getProducto().getMarca() == null ? null : new MarcaResponse(
                pedido.getProducto().getMarca().getId(),
                pedido.getProducto().getMarca().getNombre(),
                pedido.getProducto().getMarca().getFechaHora_registro(),
                pedido.getProducto().getMarca().getFechaHora_actualizacion()
        );

        return new PedidoDetalleResponse(
                pedido.getId(),
                pedido.getCantidad(),
                pedido.getSubtotal(),
                pedido.getEstado(),
                pedido.getFechaHora_registro(),
                new ProductoResponse(
                        pedido.getProducto().getId(),
                        pedido.getProducto().getNombre(),
                        pedido.getProducto().getPrecio(),
                        pedido.getProducto().getStock(),
                        new CategoriaResponse(
                                pedido.getProducto().getCategoria().getId(),
                                pedido.getProducto().getCategoria().getNombre(),
                                pedido.getProducto().getCategoria().getFechaHora_registro(),
                                pedido.getProducto().getCategoria().getFechaHora_actualizacion()
                        ),
                        marcaResponse
                ),
                new ComprobanteResponse(
                        pedido.getComprobante().getId(),
                        pedido.getComprobante().getTotal(),
                        pedido.getComprobante().getIGV(),
                        pedido.getComprobante().getFechaHora_venta(),
                        pedido.getComprobante().getEstado(),
                        grupoResponse
                ),
                new TipoEntregaResponse(
                        pedido.getTipoEntrega().getId(),
                        pedido.getTipoEntrega().getNombre(),
                        pedido.getFechaHora_registro(),
                        pedido.getFechaHora_actualizacion()
                ),
                new UsuarioResponse(
                        pedido.getUsuario().getId(),
                        pedido.getUsuario().getUsername(),
                        pedido.getUsuario().getTipo_usuario(),
                        pedido.getUsuario().getEstado(),
                        pedido.getUsuario().getFechaHora_registro(),
                        pedido.getUsuario().getFechaHora_actualizacion(),
                        pedido.getUsuario().getRol().getId(),
                        pedido.getUsuario().getRol().getNombre()
                )
        );
    }

    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll().stream().map(p -> new PedidoResponse(
                p.getId(), p.getCantidad(), p.getPrecio_unitario(), p.getSubtotal(), p.getEstado(), p.getFechaHora_registro(),
                p.getComprobante().getId(), p.getProducto().getId(), p.getTipoEntrega().getId(), p.getUsuario().getId()
        )).toList();
    }

    public PedidoResponse obtenerPorId(Long id) {
        Pedido p = pedidoRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Pedido con id: " + id + " no encontrado"));
        return new PedidoResponse(p.getId(), p.getCantidad(), p.getPrecio_unitario(), p.getSubtotal(), p.getEstado(), p.getFechaHora_registro(), p.getComprobante().getId(), p.getProducto().getId(), p.getTipoEntrega().getId(), p.getUsuario().getId());
    }

    public PedidoDetalleResponse obtenerDetallePorId(Long id) {
        return mapToPedidoDetalleResponse(pedidoRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Pedido con id: " + id + " no encontrado")));
    }

    public List<PedidoResponse> obtenerPorComprobanteId(Long comprobanteId) {
        findComprobante(comprobanteId);
        List<Pedido> pedidos = pedidoRepository.findByComprobante_Id(comprobanteId);
        if (pedidos.isEmpty()) throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No hay pedidos para este comprobante");
        return pedidos.stream().map(p -> new PedidoResponse(p.getId(), p.getCantidad(), p.getPrecio_unitario(), p.getSubtotal(), p.getEstado(), p.getFechaHora_registro(), p.getComprobante().getId(), p.getProducto().getId(), p.getTipoEntrega().getId(), p.getUsuario().getId())).toList();
    }

    public List<PedidoDetalleResponse> obtenerDetallePorComprobanteId(Long comprobanteId) {
        findComprobante(comprobanteId);
        List<Pedido> pedidos = pedidoRepository.findByComprobante_Id(comprobanteId);
        if (pedidos.isEmpty()) throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No hay pedidos para este comprobante");
        return pedidos.stream().map(this::mapToPedidoDetalleResponse).toList();
    }

    @Transactional
    public PedidoDetalleResponse actualizar(Long id, PedidoRequest request) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Pedido con id: " + id + " no encontrado"));

        devolverStockProductoEInsumos(pedido.getProducto(), pedido.getCantidad());

        Producto productoNuevo = findProducto(request.getProductoId());
        descontarStockProductoEInsumos(productoNuevo, request.getCantidad());

        BigDecimal precioUnitario = productoNuevo.getPrecio();
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(request.getCantidad()));

        pedido.setCantidad(request.getCantidad());
        pedido.setPrecio_unitario(precioUnitario);
        pedido.setSubtotal(subtotal);
        pedido.setProducto(productoNuevo);
        pedido.setEstado("MODIFICADO");
        pedido.setTipoEntrega(findTipoEntrega(request.getTipoEntregaId()));

        Pedido actualizado = pedidoRepository.save(pedido);
        recalcularTotalesComprobante(pedido.getComprobante().getId());
        return mapToPedidoDetalleResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Pedido con id: " + id + " no encontrado"));
        devolverStockProductoEInsumos(pedido.getProducto(), pedido.getCantidad());
        Long comprobanteId = pedido.getComprobante().getId();
        pedidoRepository.delete(pedido);
        recalcularTotalesComprobante(comprobanteId);
    }

    @Transactional
    public void marcarComoListo(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Pedido con id: " + pedidoId + " no encontrado"));
        if ("PAGADO".equalsIgnoreCase(pedido.getEstado())) throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se puede modificar un pedido PAGADO");
        pedido.setEstado("LISTO");
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void marcarComoPreparando(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Pedido con id: " + pedidoId + " no encontrado"));
        if ("PAGADO".equalsIgnoreCase(pedido.getEstado())) throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se puede modificar un pedido PAGADO");
        pedido.setEstado("PREPARANDO");
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void marcarComoEntregado(Long id) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Pedido no encontrado"));
        pedido.setEstado("ENTREGADO");
        pedidoRepository.save(pedido);

        Comprobante comprobante = pedido.getComprobante();
        Grupo grupo = comprobante.getGrupo();
        if (grupo != null && grupo.getFechaHora_InicioConsumo() == null) {
            LocalDateTime inicio = LocalDateTime.now();
            grupo.setFechaHora_InicioConsumo(inicio);
            grupo.setFechaHora_Liberacion(inicio.plusMinutes(45));
            grupo.setEstado("CONSUMIENDO");
            grupoRepository.save(grupo);
        }
    }

    private void descontarStockProductoEInsumos(Producto producto, int cantidadPedido) {
        if (producto.getStock() < cantidadPedido) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Stock insuficiente del producto");
        }

        if (isCategoriaPreparada(producto)) {
            List<Receta> recetas = recetaRepository.findByProducto_IdOrderByIdAsc(producto.getId());
            if (recetas.isEmpty()) throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "El producto preparado no tiene receta configurada");

            for (Receta receta : recetas) {
                Insumo insumo = receta.getInsumo();
                BigDecimal desc = convert(receta.getCantidad().multiply(BigDecimal.valueOf(cantidadPedido)), receta.getUnidad_medida(), insumo.getUnidad_medida());
                if (insumo.getStock().compareTo(desc) < 0) {
                    throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Stock insuficiente del insumo: " + insumo.getNombre());
                }
            }

            for (Receta receta : recetas) {
                Insumo insumo = receta.getInsumo();
                BigDecimal desc = convert(receta.getCantidad().multiply(BigDecimal.valueOf(cantidadPedido)), receta.getUnidad_medida(), insumo.getUnidad_medida());
                insumo.setStock(insumo.getStock().subtract(desc));
                insumoRepository.save(insumo);
            }
        }

        producto.setStock(producto.getStock() - cantidadPedido);
        productoRepository.save(producto);
    }

    private void devolverStockProductoEInsumos(Producto producto, int cantidadPedido) {
        if (isCategoriaPreparada(producto)) {
            List<Receta> recetas = recetaRepository.findByProducto_IdOrderByIdAsc(producto.getId());
            for (Receta receta : recetas) {
                Insumo insumo = receta.getInsumo();
                BigDecimal inc = convert(receta.getCantidad().multiply(BigDecimal.valueOf(cantidadPedido)), receta.getUnidad_medida(), insumo.getUnidad_medida());
                insumo.setStock(insumo.getStock().add(inc));
                insumoRepository.save(insumo);
            }
        }
        producto.setStock(producto.getStock() + cantidadPedido);
        productoRepository.save(producto);
    }

    private BigDecimal convert(BigDecimal cantidad, String source, String target) {
        String from = normalize(source);
        String to = normalize(target);
        if (from.equals(to)) return cantidad;
        if ((from.equals("G") || from.equals("GRAMOS")) && (to.equals("KG") || to.equals("KILOGRAMOS"))) return cantidad.divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);
        if ((from.equals("KG") || from.equals("KILOGRAMOS")) && (to.equals("G") || to.equals("GRAMOS"))) return cantidad.multiply(new BigDecimal("1000"));
        if (from.equals("ML") && (to.equals("L") || to.equals("LITROS"))) return cantidad.divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);
        if ((from.equals("L") || from.equals("LITROS")) && to.equals("ML")) return cantidad.multiply(new BigDecimal("1000"));
        if (isUnits(from) && isUnits(to)) return cantidad;
        throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Unidad incompatible: " + source + " -> " + target);
    }

    private boolean isCategoriaPreparada(Producto producto) {
        return producto.getCategoria() != null && (producto.getCategoria().getId() == 1L || producto.getCategoria().getId() == 2L);
    }

    private boolean isUnits(String unit) {
        return unit.equals("UNIDADES") || unit.equals("UNIDAD") || unit.equals("UDS") || unit.equals("UD");
    }

    private String normalize(String value) { return value.trim().toUpperCase(); }

    private Comprobante findComprobante(Long id) {
        if (id == null) throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Debe crear un comprobante antes de agregar pedidos");
        return comprobanteRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Comprobante con id: " + id + " no encontrado"));
    }

    private Usuario findUsuario(Long id) {
        if (id == null) throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Debe crear un usuario antes de agregar pedidos");
        return usuarioRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Usuario con id: " + id + " no encontrado"));
    }

    private Producto findProducto(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Producto con id: " + id + " no encontrado"));
    }

    private TipoEntrega findTipoEntrega(Long id) {
        return tipoEntregaRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Tipo de entrega con id: " + id + " no encontrado"));
    }
}