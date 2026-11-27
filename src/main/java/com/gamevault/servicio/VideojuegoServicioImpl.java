package com.gamevault.servicio;

import com.gamevault.dao.DesarrolladoraDao;
import com.gamevault.dao.VideojuegoDao;
import com.gamevault.dto.GeneroDTO;
import com.gamevault.dto.VideojuegoDTO;
import com.gamevault.excepcion.NoEncontradoException;
import com.gamevault.excepcion.ValidacionException;
import com.gamevault.modelo.Genero;
import com.gamevault.modelo.Videojuego;

import java.time.Year;
import java.util.List;

public class VideojuegoServicioImpl implements VideojuegoServicio {
    private final VideojuegoDao videojuegoDao;
    private final DesarrolladoraDao desarrolladoraDao;

    public VideojuegoServicioImpl(VideojuegoDao videojuegoDao, DesarrolladoraDao desarrolladoraDao) {
        this.videojuegoDao = videojuegoDao;
        this.desarrolladoraDao = desarrolladoraDao;
    }

    @Override
    public VideojuegoDTO crear(VideojuegoDTO videojuego) {
        validar(videojuego);
        validarDesarrolladora(videojuego.getDesarrolladoraId());
        return convertir(videojuegoDao.crear(convertir(videojuego)));
    }

    @Override
    public VideojuegoDTO buscarPorId(int id) {
        return videojuegoDao.buscarPorId(id)
                .map(this::convertir)
                .orElseThrow(() -> new NoEncontradoException("No existe el videojuego con id " + id));
    }

    @Override
    public List<VideojuegoDTO> listar() {
        return videojuegoDao.listar().stream().map(this::convertir).toList();
    }

    @Override
    public VideojuegoDTO actualizar(VideojuegoDTO videojuego) {
        validar(videojuego);
        if (videojuegoDao.buscarPorId(videojuego.getId()).isEmpty()) {
            throw new NoEncontradoException("No existe el videojuego con id " + videojuego.getId());
        }
        validarDesarrolladora(videojuego.getDesarrolladoraId());
        if (!videojuegoDao.actualizar(convertir(videojuego))) {
            throw new NoEncontradoException("No existe el videojuego con id " + videojuego.getId());
        }
        return buscarPorId(videojuego.getId());
    }

    @Override
    public void eliminar(int id) {
        if (!videojuegoDao.eliminar(id)) {
            throw new NoEncontradoException("No existe el videojuego con id " + id);
        }
    }

    private void validar(VideojuegoDTO videojuego) {
        if (videojuego == null) {
            throw new ValidacionException("Los datos del videojuego son obligatorios");
        }
        String titulo = videojuego.getTitulo();
        if (titulo == null || titulo.isBlank()) {
            throw new ValidacionException("El título del videojuego es obligatorio");
        }
        if (titulo.length() > 100) {
            throw new ValidacionException("El título del videojuego no puede superar los 100 caracteres");
        }
        int anioActual = Year.now().getValue();
        if (videojuego.getAnio() < 1970 || videojuego.getAnio() > anioActual) {
            throw new ValidacionException("El año del videojuego debe estar entre 1970 y " + anioActual);
        }
        if (videojuego.getPrecio() == null || videojuego.getPrecio().signum() < 0) {
            throw new ValidacionException("El precio del videojuego debe ser mayor o igual a cero");
        }
    }

    private void validarDesarrolladora(int id) {
        if (desarrolladoraDao.buscarPorId(id).isEmpty()) {
            throw new NoEncontradoException("No existe la desarrolladora con id " + id);
        }
    }

    private Videojuego convertir(VideojuegoDTO dto) {
        List<Genero> generos = dto.getGeneros().stream()
                .map(genero -> new Genero(genero.getId(), genero.getNombre()))
                .toList();
        return new Videojuego(dto.getId(), dto.getTitulo(), dto.getAnio(), dto.getPrecio(),
                dto.getDesarrolladoraId(), generos);
    }

    private VideojuegoDTO convertir(Videojuego videojuego) {
        List<GeneroDTO> generos = videojuego.getGeneros().stream()
                .map(genero -> new GeneroDTO(genero.getId(), genero.getNombre()))
                .toList();
        return new VideojuegoDTO(videojuego.getId(), videojuego.getTitulo(), videojuego.getAnio(),
                videojuego.getPrecio(), videojuego.getDesarrolladoraId(), generos);
    }
}