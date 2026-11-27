package com.gamevault.dao;

import com.gamevault.modelo.Genero;

import java.util.List;
import java.util.Optional;

public interface GeneroDao {
    Genero crear(Genero genero);

    Optional<Genero> buscarPorId(int id);

    List<Genero> listar();

    boolean actualizar(Genero genero);

    boolean eliminar(int id);
}