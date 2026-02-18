package com.restobar.lapituca.service;

import com.restobar.lapituca.entity.Comprobante;
import com.restobar.lapituca.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;

    public List<Comprobante> listarTodos(){
        return comprobanteRepository.findAll();
    }
}
