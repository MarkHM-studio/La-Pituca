package com.restobar.lapituca.service;

import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.exception.MarcaNotFoundException;
import com.restobar.lapituca.repository.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public Marca crearMarca(Marca marca){
        return marcaRepository.save(marca);
    }

    public List<Marca> obtenerMarcas(){
        return marcaRepository.findAll();
    }

    public Marca obtenerMarcaId(Long id) {
        return marcaRepository.findById(id).orElseThrow(() -> new MarcaNotFoundException("Marca no encontrada"));
    }

    public void eliminarMarcaId(Long id) {
        marcaRepository.deleteById(id);
    }

    public Marca actualizarMarcaId(Long id, String nombre) {
        Marca actualizar = marcaRepository.findById(id).orElseThrow(()-> new MarcaNotFoundException("Marca no encontrada"));
        actualizar.setNombre(actualizar.getNombre());
        return  marcaRepository.save(actualizar);
    }
}
