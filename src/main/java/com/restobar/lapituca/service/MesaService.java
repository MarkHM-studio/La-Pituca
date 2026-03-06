package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.MesaRequest;
import com.restobar.lapituca.dto.MesaResponse;
import com.restobar.lapituca.dto.MesasOcupadasResponse;
import com.restobar.lapituca.entity.Comprobante;
import com.restobar.lapituca.entity.DetalleMesa;
import com.restobar.lapituca.entity.Grupo;
import com.restobar.lapituca.entity.Mesa;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.ComprobanteRepository;
import com.restobar.lapituca.repository.DetalleMesaRepository;
import com.restobar.lapituca.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final MesaRepository mesaRepository;
    private final DetalleMesaRepository detalleMesaRepository;
    private final ComprobanteRepository comprobanteRepository;

    public MesaResponse guardar(MesaRequest request){

        if (mesaRepository.existsByNombre(request.getNombre())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe una Mesa con este nombre");
        }

        Mesa mesa = new Mesa();
        mesa.setNombre(request.getNombre());
        mesa.setEstado("DESOCUPADO");
        mesaRepository.save(mesa);

        return new MesaResponse(
                mesa.getId(),
                mesa.getNombre(),
                mesa.getEstado(),
                mesa.getFechaHora_registro(),
                mesa.getFechaHora_actualizacion()
        );
    }

    public List<MesaResponse> listarTodos(){
        return mesaRepository.findAll()
                .stream()
                .map(mesa -> new MesaResponse(
                        mesa.getId(),
                        mesa.getNombre(),
                        mesa.getEstado(),
                        mesa.getFechaHora_registro(),
                        mesa.getFechaHora_actualizacion()
                ))
                .toList();//Modificar para mostrar solo las mesas con estado = DESOCUPADO
    }

    public MesaResponse obtenerPorId(Long id){
        Mesa mesa = mesaRepository.findById(id).orElseThrow(() ->new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Mesa con id: "+id+" no encontrada"));
        return new MesaResponse(
                mesa.getId(),
                mesa.getNombre(),
                mesa.getEstado(),
                mesa.getFechaHora_registro(),
                mesa.getFechaHora_actualizacion()
        );
    }

    public MesaResponse actualizar(Long id, MesaRequest request){

        Mesa mesa = mesaRepository.findById(id).orElseThrow(() ->new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Mesa con id: "+id+" no encontrada"));

        if (mesaRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe una Mesa con este nombre");
        }

        mesa.setNombre(request.getNombre());
        mesaRepository.save(mesa);

        return new MesaResponse(
                mesa.getId(),
                mesa.getNombre(),
                mesa.getEstado(),
                mesa.getFechaHora_registro(),
                mesa.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){
        Mesa mesa = mesaRepository.findById(id).orElseThrow(() ->new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Mesa con id: "+id+" no encontrada"));
        mesaRepository.delete(mesa);
    }

    public List<MesasOcupadasResponse> obtenerMesasOcupadas() {

        List<DetalleMesa> detalles = detalleMesaRepository.findAll();

        return detalles.stream()
                .filter(d -> "OCUPADO".equalsIgnoreCase(d.getMesa().getEstado()))
                .map(d -> {

                    Grupo grupo = d.getGrupo();

                    Comprobante comprobante = comprobanteRepository
                            .findByGrupo_Id(grupo.getId())
                            .orElse(null);

                    return new MesasOcupadasResponse(
                            d.getMesa().getId(),
                            d.getMesa().getNombre(),
                            grupo.getId(),
                            d.getMesa().getEstado(),
                            comprobante != null ? comprobante.getId() : null,
                            comprobante != null ? comprobante.getEstado() : null
                    );
                })
                .toList();
    }
}
