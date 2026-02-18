package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.PedidoRequest;
import com.restobar.lapituca.entity.Comprobante;
import com.restobar.lapituca.entity.Pedido;
import com.restobar.lapituca.entity.Producto;
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
    public Pedido crear(PedidoRequest request) {

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

    public Pedido obtenerPorId(Long id){
        return pedidoRepository.findById(id).orElseThrow(()-> new PedidoNotFoundException("Pedido no encontrado"));
    }

    public Pedido actualizar(Long id, Pedido pedido){
        Pedido pedidoExistente = pedidoRepository.findById(id).orElseThrow(()-> new PedidoNotFoundException("Pedido no encontrado"));

        return null;
    }

}
