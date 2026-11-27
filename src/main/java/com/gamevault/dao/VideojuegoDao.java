package com.gamevault.dao;

import com.gamevault.modelo.Videojuego;

import java.util.List;
import java.util.Optional;

public interface VideojuegoDao {
    Videojuego crear(Videojuego videojuego);

    Optional<Videojuego> buscarPorId(int id);

    List<Videojuego> listar();

    boolean actualizar(Videojuego videojuego);

    boolean eliminar(int id);
}