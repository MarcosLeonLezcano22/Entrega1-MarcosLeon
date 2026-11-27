package com.gamevault.dao;

import com.gamevault.modelo.Desarrolladora;

import java.util.List;
import java.util.Optional;

public interface DesarrolladoraDao {
    Desarrolladora crear(Desarrolladora desarrolladora);

    Optional<Desarrolladora> buscarPorId(int id);

    List<Desarrolladora> listar();

    boolean actualizar(Desarrolladora desarrolladora);

    boolean eliminar(int id);
}