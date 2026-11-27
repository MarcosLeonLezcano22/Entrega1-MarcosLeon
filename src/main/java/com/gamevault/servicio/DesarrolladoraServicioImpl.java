package com.gamevault.servicio;

import com.gamevault.dao.DesarrolladoraDao;
import com.gamevault.dao.VideojuegoDao;
import com.gamevault.dto.DesarrolladoraDTO;
import com.gamevault.excepcion.NoEncontradoException;
import com.gamevault.excepcion.ValidacionException;
import com.gamevault.modelo.Desarrolladora;

import java.util.List;

public class DesarrolladoraServicioImpl implements DesarrolladoraServicio {
    private final DesarrolladoraDao desarrolladoraDao;
    private final VideojuegoDao videojuegoDao;

    public DesarrolladoraServicioImpl(DesarrolladoraDao desarrolladoraDao, VideojuegoDao videojuegoDao) {
        this.desarrolladoraDao = desarrolladoraDao;
        this.videojuegoDao = videojuegoDao;
    }

    @Override
    public DesarrolladoraDTO crear(DesarrolladoraDTO desarrolladora) {
        return convertir(desarrolladoraDao.crear(convertir(desarrolladora)));
    }

    @Override
    public DesarrolladoraDTO buscarPorId(int id) {
        return desarrolladoraDao.buscarPorId(id)
                .map(this::convertir)
                .orElseThrow(() -> new NoEncontradoException("No existe la desarrolladora con id " + id));
    }

    @Override
    public List<DesarrolladoraDTO> listar() {
        return desarrolladoraDao.listar().stream().map(this::convertir).toList();
    }

    @Override
    public DesarrolladoraDTO actualizar(DesarrolladoraDTO desarrolladora) {
        if (!desarrolladoraDao.actualizar(convertir(desarrolladora))) {
            throw new NoEncontradoException("No existe la desarrolladora con id " + desarrolladora.getId());
        }
        return buscarPorId(desarrolladora.getId());
    }

    @Override
    public void eliminar(int id) {
        if (desarrolladoraDao.buscarPorId(id).isEmpty()) {
            throw new NoEncontradoException("No existe la desarrolladora con id " + id);
        }
        boolean tieneVideojuegos = videojuegoDao.listar().stream()
                .anyMatch(videojuego -> videojuego.getDesarrolladoraId() == id);
        if (tieneVideojuegos) {
            throw new ValidacionException("No se puede eliminar la desarrolladora porque tiene videojuegos asociados");
        }
        if (!desarrolladoraDao.eliminar(id)) {
            throw new NoEncontradoException("No existe la desarrolladora con id " + id);
        }
    }

    private Desarrolladora convertir(DesarrolladoraDTO dto) {
        return new Desarrolladora(dto.getId(), dto.getNombre(), dto.getPais());
    }

    private DesarrolladoraDTO convertir(Desarrolladora desarrolladora) {
        return new DesarrolladoraDTO(desarrolladora.getId(), desarrolladora.getNombre(), desarrolladora.getPais());
    }
}