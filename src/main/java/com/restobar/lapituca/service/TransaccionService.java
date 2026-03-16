package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TransaccionRequest;
import com.restobar.lapituca.entity.Reserva;
import com.restobar.lapituca.entity.Transaccion;
import com.restobar.lapituca.entity.Usuario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.ReservaRepository;
import com.restobar.lapituca.repository.TransaccionRepository;
import com.restobar.lapituca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    public void registrarPagoReserva(TransaccionRequest request){

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Usuario no encontrado"));

        Reserva reserva = reservaRepository.findById(request.getReservaId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Reserva no encontrada"));

        if(reserva.getTransaccion()!=null){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,
                    "La reserva ya tiene una transacción registrada");
        }

        Transaccion transaccion = new Transaccion();

        transaccion.setMercadoPagoId(request.getMercadoPagoId());
        transaccion.setMonto(request.getMonto());
        transaccion.setUsuario(usuario);

        Transaccion guardada = transaccionRepository.save(transaccion);

        reserva.setTransaccion(guardada);
        reservaRepository.save(reserva);
    }
}