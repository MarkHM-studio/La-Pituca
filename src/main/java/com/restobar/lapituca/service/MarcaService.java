package com.restobar.lapituca.service;

import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.exception.MarcaNotFoundException;
import com.restobar.lapituca.repository.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public Marca guardar(Marca marca){
        return marcaRepository.save(marca);
    }

    public List<Marca> listarTodos(){
        return marcaRepository.findAll();
    }

    public Marca obtenerPorId(Long id) {
        return marcaRepository.findById(id).orElseThrow(()
                -> new MarcaNotFoundException("Marca no encontrada"));
    }

    public Marca actualizar(Long id, Marca marca) {
        Marca marcaExistente = marcaRepository.findById(id).orElseThrow(()
                -> new MarcaNotFoundException("Marca no encontrada"));

        marcaExistente.setNombre(marca.getNombre());
        marcaExistente.setFecha_modificacion(LocalDate.now());

        return  marcaRepository.save(marcaExistente);
    }

    public void eliminar(Long id) {
        marcaRepository.deleteById(id);
    }
}
