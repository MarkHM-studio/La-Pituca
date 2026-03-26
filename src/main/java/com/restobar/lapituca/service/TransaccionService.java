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

}