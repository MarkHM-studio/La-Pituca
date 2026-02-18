package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.PedidoRequest;
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
    public Pedido guardar(PedidoRequest request) {

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado"));

        Comprobante comprobante = comprobanteRepository
                .findById(request.getComprobanteId())
                .orElseGet(() -> {
                    Comprobante nuevo = new Comprobante();
                    return comprobanteRepository.save(nuevo);
                });

        BigDecimal precioUnitario = producto.getPrecio();
        BigDecimal subtotal = precioUnitario.multiply(
                BigDecimal.valueOf(request.getCantidad())
        );

        Pedido pedido = new Pedido();
        pedido.setCantidad(request.getCantidad());
        pedido.setProducto(producto);
        pedido.setComprobante(comprobante);
        pedido.setPrecio_unitario(precioUnitario);
        pedido.setSubtotal(subtotal);
        pedido.setEstado("PENDIENTE");

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarTodos(){
        return pedidoRepository.findAll();
    }

    public List<Pedido> obtenerPorComprobanteId(Long id){

        List<Pedido> pedidos = pedidoRepository.findByComprobante_Id(id);

        if (pedidos.isEmpty()) {
            throw new PedidoNotFoundException("No hay pedidos para este comprobante");
        }

        return pedidos;
    }

    @Transactional
    public Pedido actualizar(Long id, PedidoRequest request){

        Pedido pedidoExistente = pedidoRepository.findById(id).orElseThrow(()-> new PedidoNotFoundException("Pedido no encontrado"));
        pedidoExistente.setCantidad(request.getCantidad());

        Producto productoExistente = productoRepository.findById(request.getProductoId()).orElseThrow(()-> new ProductoNotFoundException("Producto no encontrado"));
        pedidoExistente.setProducto(productoExistente);
        pedidoExistente.setPrecio_unitario(productoExistente.getPrecio());

        BigDecimal precio_unitario = productoExistente.getPrecio();
        BigDecimal subtotal = precio_unitario.multiply(BigDecimal.valueOf(request.getCantidad()));
        pedidoExistente.setSubtotal(subtotal);

        pedidoExistente.setEstado("LISTO");

        pedidoExistente.setProducto(productoExistente);

        Comprobante comprobanteExistente = comprobanteRepository.findById(pedidoExistente.getComprobante().getId()).orElseThrow(()-> new ComprobanteNotFoundException("Comprobante no encontrado"));

        pedidoExistente.setComprobante(comprobanteExistente);

        return pedidoRepository.save(pedidoExistente);
    }
}
