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

    @Transactional
    public PedidoDetalleResponse guardar(PedidoRequest request) {

        if (request.getComprobanteId() == null) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Debe crear un comprobante antes de agregar pedidos");
        }

        Comprobante comprobante = comprobanteRepository.findById(request.getComprobanteId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Comprobante con id: "+request.getComprobanteId()+" no encontrado"));

        if (request.getUsuarioId() == null) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Debe crear un usuario antes de agregar pedidos");
        }

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: "+request.getUsuarioId()+" no encontrado"));

        if ("CERRADO".equalsIgnoreCase(comprobante.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"No se pueden agregar pedidos a un comprobante cerrado");
        }

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Producto con id: "+request.getProductoId()+" no encontrado"));

        if (producto.getStock() < request.getCantidad()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Stock insuficiente");
        }

        TipoEntrega tipoEntrega = tipoEntregaRepository.findById(request.getTipoEntregaId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Tipo de entrega con id: "+request.getTipoEntregaId()+" no encontrado"));
        /*
        // Asignar grupo SOLO si es comer y aún no tiene grupo
        if (comprobante.getGrupo() == null) {
            asignarGrupoYMesasSiEsComer(tipoEntrega, request, comprobante);
        }*/
        BigDecimal precioUnitario = producto.getPrecio();
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(request.getCantidad()));

        // Descontar stock
        producto.setStock(producto.getStock() - request.getCantidad());
        productoRepository.save(producto);

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

        Comprobante comprobante = comprobanteRepository.findById(comprobanteId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Comprobante con id: "+comprobanteId+" no encontrado"));

        //Buscar todos los pedidos pertenecientes a un comprobante
        List<Pedido> pedidos = pedidoRepository.findByComprobante_Id(comprobanteId);

        //Recorreme todos "subtotal" tantas veces, como pedidos pertenezcan a un comprobante
        BigDecimal total = pedidos.stream()
                .map(Pedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);//en cada recorrido autoincrementame el total

        // IGV 18% (Perú)
        BigDecimal igv = total.multiply(new BigDecimal("0.18"));

        comprobante.setTotal(total);
        comprobante.setIGV(igv);

        comprobanteRepository.save(comprobante);
    }

    private PedidoDetalleResponse mapToPedidoDetalleResponse(Pedido pedido) {

        Grupo grupo = pedido.getComprobante().getGrupo();

        GrupoResponse grupoResponse = null;

        if (grupo != null) {
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
                        new MarcaResponse(
                                pedido.getProducto().getMarca().getId(),
                                pedido.getProducto().getMarca().getNombre(),
                                pedido.getProducto().getMarca().getFechaHora_registro(),
                                pedido.getProducto().getMarca().getFechaHora_actualizacion()
                        )
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

        return pedidoRepository.findAll()
                .stream()
                .map(p -> new PedidoResponse(
                        p.getId(),
                        p.getCantidad(),
                        p.getPrecio_unitario(),
                        p.getSubtotal(),
                        p.getEstado(),
                        p.getFechaHora_registro(),
                        p.getComprobante().getId(),
                        p.getProducto().getId(),
                        p.getTipoEntrega().getId(),
                        p.getUsuario().getId()
                ))
                .toList();
    }

    public PedidoResponse obtenerPorId(Long id){

        Pedido p = pedidoRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Pedido con id: "+id+" no encontrado"));

        return new PedidoResponse(
                        p.getId(),
                        p.getCantidad(),
                        p.getPrecio_unitario(),
                        p.getSubtotal(),
                        p.getEstado(),
                        p.getFechaHora_registro(),
                        p.getComprobante().getId(),
                        p.getProducto().getId(),
                        p.getTipoEntrega().getId(),
                        p.getUsuario().getId()
                );
    }

    public PedidoDetalleResponse obtenerDetallePorId(Long id) {

        Pedido p = pedidoRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Pedido con id: "+id+" no encontrado"));

        return mapToPedidoDetalleResponse(p);
    }

    public List<PedidoResponse> obtenerPorComprobanteId(Long comprobanteId) {

        comprobanteRepository.findById(comprobanteId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Comprobante con id: " + comprobanteId + " no encontrado"));

        List<Pedido> pedidos = pedidoRepository.findByComprobante_Id(comprobanteId);

        if (pedidos.isEmpty()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No hay pedidos para este comprobante");
        }

        return pedidos.stream()
                .map(p -> new PedidoResponse(
                        p.getId(),
                        p.getCantidad(),
                        p.getPrecio_unitario(),
                        p.getSubtotal(),
                        p.getEstado(),
                        p.getFechaHora_registro(),
                        p.getComprobante().getId(),
                        p.getProducto().getId(),
                        p.getTipoEntrega().getId(),
                        p.getUsuario().getId()
                ))
                .toList();
    }

    public List<PedidoDetalleResponse> obtenerDetallePorComprobanteId(Long comprobanteId) {

        Comprobante comprobante = comprobanteRepository.findById(comprobanteId).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Comprobante con id: "+comprobanteId+" no encontrado"));

        List<Pedido> pedidos = pedidoRepository.findByComprobante_Id(comprobanteId);

        if (pedidos.isEmpty()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"No hay pedidos para este comprobante");
        }

        Grupo grupo = comprobante.getGrupo();

        GrupoResponse grupoResponse = null;

        if (grupo != null) {
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

        GrupoResponse finalGrupoResponse = grupoResponse; // Necesario para usar en lambda

        return pedidos.stream()
                .map(p -> new PedidoDetalleResponse(
                        p.getId(),
                        p.getCantidad(),
                        p.getSubtotal(),
                        p.getEstado(),
                        p.getFechaHora_registro(),
                        new ProductoResponse(
                                p.getProducto().getId(),
                                p.getProducto().getNombre(),
                                p.getProducto().getPrecio(),
                                p.getProducto().getStock(),
                                new CategoriaResponse(
                                        p.getProducto().getCategoria().getId(),
                                        p.getProducto().getCategoria().getNombre(),
                                        p.getProducto().getCategoria().getFechaHora_registro(),
                                        p.getProducto().getCategoria().getFechaHora_actualizacion()
                                ),
                                new MarcaResponse(
                                        p.getProducto().getMarca().getId(),
                                        p.getProducto().getMarca().getNombre(),
                                        p.getProducto().getMarca().getFechaHora_registro(),
                                        p.getProducto().getMarca().getFechaHora_actualizacion()
                                )
                        ),
                        new ComprobanteResponse(
                                p.getComprobante().getId(),
                                p.getComprobante().getTotal(),
                                p.getComprobante().getIGV(),
                                p.getComprobante().getFechaHora_venta(),
                                p.getComprobante().getEstado(),
                                finalGrupoResponse
                        ),
                        new TipoEntregaResponse(
                                p.getTipoEntrega().getId(),
                                p.getTipoEntrega().getNombre(),
                                p.getTipoEntrega().getFechaHora_registro(),
                                p.getTipoEntrega().getFechaHora_actualizacion()
                        ),
                        new UsuarioResponse(
                                p.getUsuario().getId(),
                                p.getUsuario().getUsername(),
                                p.getUsuario().getTipo_usuario(),
                                p.getUsuario().getEstado(),
                                p.getUsuario().getFechaHora_registro(),
                                p.getUsuario().getFechaHora_actualizacion(),

                                p.getUsuario().getRol().getId(),
                                p.getUsuario().getRol().getNombre()
                        )
                ))
                .toList();
    }

    @Transactional
    public PedidoDetalleResponse actualizar(Long id, PedidoRequest request){

        Pedido pedidoExistente = pedidoRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Pedido con id: "+id+" no encontrado"));

        //Obtener producto anterior y cantidad de cuanto se pidiò para devolver el stock , posteriormente
        Producto productoAnterior = pedidoExistente.getProducto();
        int cantidadAnterior = pedidoExistente.getCantidad();

        //Devuelve el stock anterior
        productoAnterior.setStock(productoAnterior.getStock() + cantidadAnterior);
        productoRepository.save(productoAnterior);

        Producto productoExistente = productoRepository.findById(request.getProductoId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Producto con id: "+request.getProductoId()+" no encontrado"));

        //Verificar stock (aunque en un principio este producto no debería estar disponible ni aparecer en el frontend
        if (productoExistente.getStock() < request.getCantidad()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Stock insuficiente"); //Reemplazar por exception personalizada
        }

        //Descontar stock
        productoExistente.setStock(productoExistente.getStock() - request.getCantidad());
        productoRepository.save(productoExistente);

        //Si es el mismo producto, no pasa nada, solo se actualiza la cantidad
        //Si es diferente producto, se devuelve la cantidad del anterior prod, y se resta stock al nuevoProd.

        BigDecimal precio_unitario = productoExistente.getPrecio();
        BigDecimal subtotal = precio_unitario.multiply(BigDecimal.valueOf(request.getCantidad()));

        TipoEntrega tipoEntrega = tipoEntregaRepository.findById(request.getTipoEntregaId()).orElseThrow(()->new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Tipo de Entrega con id: "+request.getTipoEntregaId()+" no encontrado"));

        pedidoExistente.setCantidad(request.getCantidad());
        pedidoExistente.setPrecio_unitario(precio_unitario);
        pedidoExistente.setSubtotal(subtotal);
        pedidoExistente.setProducto(productoExistente);
        pedidoExistente.setEstado("MODIFICADO");
        pedidoExistente.setTipoEntrega(tipoEntrega);

        Pedido pedidoActualizado = pedidoRepository.save(pedidoExistente);

        //Ya no se coloca, por que si el pedido existe, el comprobante tambièn (por las relaciones), que no admiten que Id_comprobante, en Pedido, sea nulo
        /*
        Comprobante comprobanteExistente = comprobanteRepository.findById(pedidoExistente.getComprobante().getId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Comprobante con id: "+pedidoExistente.getComprobante().getId()+" no encontrado"));
        */
        recalcularTotalesComprobante(
                pedidoExistente.getComprobante().getId()
        );
        /* En primer lugar, no te permitirìa cambiar, dado que el idComp. es foranea, y tendrìas que eliminar el comprobante primero
        pedidoExistente.setComprobante(comprobanteExistente);*/

        return mapToPedidoDetalleResponse(pedidoActualizado);
    }

    @Transactional
    public void eliminar(Long id) {

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Pedido con id: "+id+" no encontrado"));

        // Devolver stock
        Producto producto = pedido.getProducto();
        producto.setStock(producto.getStock() + pedido.getCantidad());
        productoRepository.save(producto);

        Long comprobanteId = pedido.getComprobante().getId();

        pedidoRepository.delete(pedido);

        // Recalcular totales
        recalcularTotalesComprobante(comprobanteId);
    }

    @Transactional
    public void marcarComoListo(Long pedidoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Pedido con id: "+pedidoId+" no encontrado"));

        if ("PAGADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"No se puede modificar un pedido PAGADO");
        }

        pedido.setEstado("LISTO");
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void marcarComoPreparando(Long pedidoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Pedido con id: "+pedidoId+" no encontrado"));

        if ("PAGADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"No se puede modificar un pedido PAGADO");
        }

        pedido.setEstado("PREPARANDO");
        pedidoRepository.save(pedido);
    }
}
