package com.gamevault.servicio;

import com.gamevault.dao.GeneroDao;
import com.gamevault.dto.GeneroDTO;
import com.gamevault.excepcion.NoEncontradoException;
import com.gamevault.modelo.Genero;

import java.util.List;

public class GeneroServicioImpl implements GeneroServicio {
    private final GeneroDao generoDao;

    public GeneroServicioImpl(GeneroDao generoDao) {
        this.generoDao = generoDao;
    }

    @Override
    public GeneroDTO crear(GeneroDTO genero) {
        return convertir(generoDao.crear(convertir(genero)));
    }

    @Override
    public GeneroDTO buscarPorId(int id) {
        return generoDao.buscarPorId(id)
                .map(this::convertir)
                .orElseThrow(() -> new NoEncontradoException("No existe el género con id " + id));
    }

    @Override
    public List<GeneroDTO> listar() {
        return generoDao.listar().stream().map(this::convertir).toList();
    }

    @Override
    public GeneroDTO actualizar(GeneroDTO genero) {
        if (!generoDao.actualizar(convertir(genero))) {
            throw new NoEncontradoException("No existe el género con id " + genero.getId());
        }
        return buscarPorId(genero.getId());
    }

    @Override
    public void eliminar(int id) {
        if (!generoDao.eliminar(id)) {
            throw new NoEncontradoException("No existe el género con id " + id);
        }
    }

    private Genero convertir(GeneroDTO dto) {
        return new Genero(dto.getId(), dto.getNombre());
    }

    private GeneroDTO convertir(Genero genero) {
        return new GeneroDTO(genero.getId(), genero.getNombre());
    }
}