package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.*;
import com.restobar.lapituca.entity.Comprobante;
import com.restobar.lapituca.entity.Pedido;
import com.restobar.lapituca.entity.Producto;
import com.restobar.lapituca.exception.ComprobanteNotFoundException;
import com.restobar.lapituca.exception.PedidoNotFoundException;
import com.restobar.lapituca.exception.ProductoNotFoundException;
import com.restobar.lapituca.repository.ComprobanteRepository;
import com.restobar.lapituca.repository.PedidoRepository;
import com.restobar.lapituca.repository.ProductoRepository;
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

    @Transactional
    public PedidoDetalleResponse guardar(PedidoRequest request) {

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado"));

        //validaciòn de stock
        if (producto.getStock() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente"); //Validar con exception personalizada
        }

        //Creas o reasignas un comprobante
        Comprobante comprobante = comprobanteRepository
                .findById(request.getComprobanteId())
                .orElseGet(() -> {
                    Comprobante nuevo = new Comprobante();
                    nuevo.setTotal(BigDecimal.ZERO);
                    nuevo.setIGV(BigDecimal.ZERO);
                    return comprobanteRepository.save(nuevo);
                });

        BigDecimal precioUnitario = producto.getPrecio();
        BigDecimal subtotal = precioUnitario.multiply(
                BigDecimal.valueOf(request.getCantidad())
        );

        //reducciòn de stock
        producto.setStock(producto.getStock() - request.getCantidad());
        productoRepository.save(producto);

        Pedido pedido = new Pedido();
        pedido.setCantidad(request.getCantidad());
        pedido.setProducto(producto);
        pedido.setComprobante(comprobante);
        pedido.setPrecio_unitario(precioUnitario);
        pedido.setSubtotal(subtotal);
        pedido.setEstado("PENDIENTE");

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        recalcularTotalesComprobante(comprobante.getId()); //No puede ser request.getComprobanteId(), por que te arriesgas a que ese id de comprobante mandado, no exista.

        return new PedidoDetalleResponse(
                pedidoGuardado.getId(),
                pedidoGuardado.getCantidad(),
                pedidoGuardado.getSubtotal(),
                pedidoGuardado.getEstado(),
                pedidoGuardado.getFechaHora_registro(),
                new ProductoResponse(
                        pedidoGuardado.getProducto().getId(),
                        pedidoGuardado.getProducto().getNombre(),
                        pedidoGuardado.getProducto().getPrecio(),
                        pedidoGuardado.getProducto().getStock(),
                        new CategoriaResponse(
                                pedidoGuardado.getProducto().getCategoria().getId(),
                                pedidoGuardado.getProducto().getCategoria().getNombre()
                        ),
                        new MarcaResponse(
                                pedidoGuardado.getProducto().getMarca().getId(),
                                pedidoGuardado.getProducto().getMarca().getNombre()
                        )
                ),
                new ComprobanteResponse(
                        pedidoGuardado.getComprobante().getId(),
                        pedidoGuardado.getComprobante().getTotal(),
                        pedidoGuardado.getComprobante().getIGV(),
                        pedidoGuardado.getComprobante().getFechaHora_venta()
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
                        p.getProducto().getId(),
                        p.getComprobante().getId()
                ))
                .toList();
    }

    public PedidoResponse obtenerPorId(Long id){

        Pedido p = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException("Pedido no encontrado"));

        return new PedidoResponse(
                        p.getId(),
                        p.getCantidad(),
                        p.getPrecio_unitario(),
                        p.getSubtotal(),
                        p.getEstado(),
                        p.getFechaHora_registro(),
                        p.getProducto().getId(),
                        p.getComprobante().getId()
                );
    }

    public PedidoDetalleResponse obtenerDetallePorId(Long id) {

        Pedido p = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException("Pedido no encontrado"));

        return new PedidoDetalleResponse(
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
                                        p.getProducto().getCategoria().getNombre()
                                ),
                                new MarcaResponse(
                                        p.getProducto().getMarca().getId(),
                                        p.getProducto().getMarca().getNombre()
                                )
                        ),
                        new ComprobanteResponse(
                                p.getComprobante().getId(),
                                p.getComprobante().getTotal(),
                                p.getComprobante().getIGV(),
                                p.getComprobante().getFechaHora_venta()
                        )
                );
    }


    public List<PedidoResponse> obtenerPorComprobanteId(Long comprobanteId){

        return pedidoRepository.findByComprobante_Id(comprobanteId)
                .stream()
                .map(p -> new PedidoResponse(
                        p.getId(),
                        p.getCantidad(),
                        p.getPrecio_unitario(),
                        p.getSubtotal(),
                        p.getEstado(),
                        p.getFechaHora_registro(),
                        p.getProducto().getId(),
                        p.getComprobante().getId()
                ))
                .toList();
    }

    public List<PedidoDetalleResponse> obtenerDetallePorComprobanteId(Long comprobanteId) {
        List<Pedido> pedidos = pedidoRepository.findByComprobante_Id(comprobanteId);

        if (pedidos.isEmpty()) {
            throw new PedidoNotFoundException("No hay pedidos para este comprobante");
        }

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
                                        p.getProducto().getCategoria().getNombre()
                                ),
                                new MarcaResponse(
                                        p.getProducto().getMarca().getId(),
                                        p.getProducto().getMarca().getNombre()
                                )
                        ),
                        new ComprobanteResponse(
                                p.getComprobante().getId(),
                                p.getComprobante().getTotal(),
                                p.getComprobante().getIGV(),
                                p.getComprobante().getFechaHora_venta()
                        )
                ))
                .toList();
    }

    @Transactional
    public PedidoDetalleResponse actualizar(Long id, PedidoRequest request){

        Pedido pedidoExistente = pedidoRepository.findById(id).orElseThrow(()-> new PedidoNotFoundException("Pedido no encontrado"));

        //Obtener producto anterior y cantidad de cuanto se pidiò para devolver el stock , posteriormente
        Producto productoAnterior = pedidoExistente.getProducto();
        int cantidadAnterior = pedidoExistente.getCantidad();

        //Devuelve el stock anterior
        productoAnterior.setStock(productoAnterior.getStock() + cantidadAnterior);
        productoRepository.save(productoAnterior);

        Producto productoExistente = productoRepository.findById(request.getProductoId()).orElseThrow(()-> new ProductoNotFoundException("Producto no encontrado"));

        //Verificar stock (aunque en un principio este producto no debería estar disponible ni aparecer en el frontend
        if (productoExistente.getStock() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente"); //Reemplazar por exception personalizada
        }

        //Descontar stock
        productoExistente.setStock(productoExistente.getStock() - request.getCantidad());
        productoRepository.save(productoExistente);

        //Si es el mismo producto, no pasa nada, solo se actualiza la cantidad
        //Si es diferente producto, se devuelve la cantidad del anterior prod, y se resta stock al nuevoProd.

        BigDecimal precio_unitario = productoExistente.getPrecio();
        BigDecimal subtotal = precio_unitario.multiply(BigDecimal.valueOf(request.getCantidad()));

        pedidoExistente.setCantidad(request.getCantidad());
        pedidoExistente.setPrecio_unitario(precio_unitario);
        pedidoExistente.setSubtotal(subtotal);
        pedidoExistente.setProducto(productoExistente);
        pedidoExistente.setEstado("ACTUALIZADO");

        Pedido pedidoActualizado = pedidoRepository.save(pedidoExistente);

        //Ya no se coloca, por que si el pedido existe, el comprobante tambièn (por las relaciones), que no admiten que Id_comprobante, en Pedido, sea nulo
        /*
        Comprobante comprobanteExistente = comprobanteRepository.findById(pedidoExistente.getComprobante().getId()).orElseThrow(()-> new ComprobanteNotFoundException("Comprobante no encontrado"));
        */

        recalcularTotalesComprobante(
                pedidoExistente.getComprobante().getId()
        );

        /* En primer lugar, no te permitirìa cambiar, dado que el idComp. es foranea, y tendrìas que eliminar el comprobante primero
        pedidoExistente.setComprobante(comprobanteExistente);*/

        return new PedidoDetalleResponse(
                pedidoActualizado.getId(),
                pedidoActualizado.getCantidad(),
                pedidoActualizado.getSubtotal(),
                pedidoActualizado.getEstado(),
                pedidoActualizado.getFechaHora_registro(),
                new ProductoResponse(
                        pedidoActualizado.getProducto().getId(),
                        pedidoActualizado.getProducto().getNombre(),
                        pedidoActualizado.getProducto().getPrecio(),
                        pedidoActualizado.getProducto().getStock(),
                        new CategoriaResponse(
                                pedidoActualizado.getProducto().getCategoria().getId(),
                                pedidoActualizado.getProducto().getCategoria().getNombre()
                        ),
                        new MarcaResponse(
                                pedidoActualizado.getProducto().getMarca().getId(),
                                pedidoActualizado.getProducto().getMarca().getNombre()
                        )
                ),
                new ComprobanteResponse(
                        pedidoActualizado.getComprobante().getId(),
                        pedidoActualizado.getComprobante().getTotal(),
                        pedidoActualizado.getComprobante().getIGV(),
                        pedidoActualizado.getComprobante().getFechaHora_venta()
                )
        );
    }

    private void recalcularTotalesComprobante(Long comprobanteId) {

        Comprobante comprobante = comprobanteRepository.findById(comprobanteId)
                .orElseThrow(() -> new ComprobanteNotFoundException("Comprobante no encontrado"));

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

    @Transactional
    public void eliminar(Long id) {

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException("Pedido no encontrado"));

        // Devolver stock
        Producto producto = pedido.getProducto();
        producto.setStock(producto.getStock() + pedido.getCantidad());
        productoRepository.save(producto);

        Long comprobanteId = pedido.getComprobante().getId();

        pedidoRepository.delete(pedido);

        // Recalcular totales
        recalcularTotalesComprobante(comprobanteId);
    }

}
