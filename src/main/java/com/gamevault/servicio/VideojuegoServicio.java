package com.gamevault.servicio;

import com.gamevault.dto.VideojuegoDTO;

import java.util.List;

public interface VideojuegoServicio {
    VideojuegoDTO crear(VideojuegoDTO videojuego);

    VideojuegoDTO buscarPorId(int id);

    List<VideojuegoDTO> listar();

    VideojuegoDTO actualizar(VideojuegoDTO videojuego);

    void eliminar(int id);
}