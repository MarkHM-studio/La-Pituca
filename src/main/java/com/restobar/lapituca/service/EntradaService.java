package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.EntradaRequest;
import com.restobar.lapituca.dto.response.EntradaResponse;
import com.restobar.lapituca.entity.*;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntradaService {

    private final EntradaRepository entradaRepository;
    private final ProductoRepository productoRepository;
    private final InsumoRepository insumoRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final RecetaRepository recetaRepository;

    @Transactional
    public EntradaResponse crear(EntradaRequest request) {
        validarSeleccion(request);

        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Proveedor con id: " + request.getProveedorId() + " no encontrado"));

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Usuario con id: " + request.getUsuarioId() + " no encontrado"));

        Entrada entrada = new Entrada();
        entrada.setCantidad_total(request.getCantidadTotal());
        entrada.setUnidad_medida(normalize(request.getUnidadMedida()));
        entrada.setCosto_unitario(request.getCostoUnitario());
        entrada.setCosto_total(request.getCostoUnitario().multiply(request.getCantidadTotal()));
        entrada.setProveedor(proveedor);
        entrada.setUsuario(usuario);

        if (request.getProductoId() != null) {
            Producto producto = productoRepository.findById(request.getProductoId())
                    .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Producto con id: " + request.getProductoId() + " no encontrado"));

            if (producto.getCategoria() != null && (producto.getCategoria().getId() == 1L || producto.getCategoria().getId() == 2L)) {
                throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se permiten entradas directas para productos de categoría 1 o 2");
            }
            if (!isUnits(request.getUnidadMedida())) {
                throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Para productos directos la unidad debe ser UNIDADES/UDS");
            }
            producto.setStock(producto.getStock() + request.getCantidadTotal().intValue());
            productoRepository.save(producto);
            entrada.setProducto(producto);
        } else {
            Insumo insumo = insumoRepository.findById(request.getInsumoId())
                    .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Insumo con id: " + request.getInsumoId() + " no encontrado"));

            BigDecimal incremento = convert(request.getCantidadTotal(), request.getUnidadMedida(), insumo.getUnidad_medida());
            insumo.setStock(insumo.getStock().add(incremento));
            insumoRepository.save(insumo);
            entrada.setInsumo(insumo);

            recalcularStockProductosPreparados();
        }

        return map(entradaRepository.save(entrada));
    }

    public List<EntradaResponse> listarTodos() {
        return entradaRepository.findAll().stream().map(this::map).toList();
    }

    private void recalcularStockProductosPreparados() {
        List<Producto> preparados = productoRepository.findByCategoria_IdIn(List.of(1L, 2L));

        for (Producto producto : preparados) {
            List<Receta> recetas = recetaRepository.findByProducto_IdOrderByIdAsc(producto.getId());
            if (recetas.isEmpty()) {
                producto.setStock(0);
                productoRepository.save(producto);
                continue;
            }

            int maximo = Integer.MAX_VALUE;
            for (Receta receta : recetas) {
                BigDecimal requeridoEnUnidadInsumo = convert(receta.getCantidad(), receta.getUnidad_medida(), receta.getInsumo().getUnidad_medida());
                if (requeridoEnUnidadInsumo.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Receta inválida para producto " + producto.getNombre());
                }
                int posible = receta.getInsumo().getStock().divide(requeridoEnUnidadInsumo, 0, RoundingMode.DOWN).intValue();
                maximo = Math.min(maximo, posible);
            }
            producto.setStock(Math.max(0, maximo));
            productoRepository.save(producto);
        }
    }

    private void validarSeleccion(EntradaRequest request) {
        boolean tieneProducto = request.getProductoId() != null;
        boolean tieneInsumo = request.getInsumoId() != null;

        if (tieneProducto == tieneInsumo) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Debe enviar solo productoId o solo insumoId");
        }
    }

    private boolean isUnits(String unit) {
        String n = normalize(unit);
        return n.equals("UNIDADES") || n.equals("UNIDAD") || n.equals("UDS") || n.equals("UD");
    }

    private BigDecimal convert(BigDecimal cantidad, String source, String target) {
        String from = normalize(source);
        String to = normalize(target);
        if (from.equals(to)) {
            return cantidad;
        }

        if ((from.equals("G") || from.equals("GRAMOS")) && (to.equals("KG") || to.equals("KILOGRAMOS"))) {
            return cantidad.divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);
        }
        if ((from.equals("KG") || from.equals("KILOGRAMOS")) && (to.equals("G") || to.equals("GRAMOS"))) {
            return cantidad.multiply(new BigDecimal("1000"));
        }
        if ((from.equals("ML")) && (to.equals("L") || to.equals("LITROS"))) {
            return cantidad.divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);
        }
        if ((from.equals("L") || from.equals("LITROS")) && to.equals("ML")) {
            return cantidad.multiply(new BigDecimal("1000"));
        }

        if (isUnits(from) && isUnits(to)) {
            return cantidad;
        }

        throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Unidad de medida incompatible: " + source + " -> " + target);
    }

    private String normalize(String value) {
        return value.trim().toUpperCase();
    }

    private EntradaResponse map(Entrada e) {
        return new EntradaResponse(
                e.getId(),
                e.getProducto() != null ? e.getProducto().getId() : null,
                e.getInsumo() != null ? e.getInsumo().getId() : null,
                e.getCantidad_total(),
                e.getUnidad_medida(),
                e.getCosto_unitario(),
                e.getCosto_total(),
                e.getProveedor().getId(),
                e.getUsuario().getId(),
                e.getFechaHora_registro()
        );
    }
}
