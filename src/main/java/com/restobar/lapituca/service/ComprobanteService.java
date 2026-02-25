package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.*;
import com.restobar.lapituca.entity.*;
import com.restobar.lapituca.exception.ComprobanteNotFoundException;
import com.restobar.lapituca.exception.MesaNotFoundException;
import com.restobar.lapituca.repository.ComprobanteRepository;
import com.restobar.lapituca.repository.DetalleMesaRepository;
import com.restobar.lapituca.repository.GrupoRepository;
import com.restobar.lapituca.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final GrupoRepository grupoRepository;
    private final MesaRepository mesaRepository;
    private final DetalleMesaRepository detalleMesaRepository;

    @Transactional
    public ComprobanteResponse crearComprobante() {

        Comprobante comprobante = new Comprobante();
        comprobante.setTotal(BigDecimal.ZERO);
        comprobante.setIGV(BigDecimal.ZERO);
        comprobante.setEstado("ABIERTO");

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
        Comprobante comprobante = comprobanteRepository.findById(request.getId())
                .orElseThrow(() -> new ComprobanteNotFoundException("Comprobante no encontrado"));

        //Validar que tenga pedidos
        List<Pedido> pedidos = comprobante.getPedidos();

        if (pedidos == null || pedidos.isEmpty()) {
            throw new RuntimeException("No se puede asignar mesas a un comprobante sin pedidos");
        }

        //Verificar si al menos 1 pedido es tipo COMER
        boolean tienePedidoComer = pedidos.stream()
                .anyMatch(p ->
                        p.getTipoEntrega() != null &&
                                "COMER".equalsIgnoreCase(p.getTipoEntrega().getNombre())
                );

        //Si ninguno es COMER, no se asigna grupo (regla de negocio correcta)
        if (!tienePedidoComer) {
            throw new RuntimeException(
                    "No se puede asignar mesas porque todos los pedidos son para LLEVAR"
            );
        }

        //Si ya tiene grupo, no crear otro (evita duplicados)
        if (comprobante.getGrupo() != null) {
            throw new RuntimeException("Este comprobante ya tiene un grupo asignado");
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
            throw new RuntimeException("Debe enviar al menos una mesa");
        }

        Set<Long> mesasUnicas = new HashSet<>(request.getMesasId());

        // Asignar mesas al grupo
        for (Long mesaId : mesasUnicas) {

            Mesa mesa = mesaRepository.findById(mesaId)
                    .orElseThrow(() -> new MesaNotFoundException("Mesa no encontrada con id: " + mesaId));

            if ("OCUPADO".equalsIgnoreCase(mesa.getEstado())) {
                throw new RuntimeException("La mesa " + mesa.getNombre() + " ya está ocupada");
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

    public void registrarVenta(){
        
    }
}
