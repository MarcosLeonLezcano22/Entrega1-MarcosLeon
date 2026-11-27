package com.gamevault.servicio;

import com.gamevault.dto.GeneroDTO;

import java.util.List;

public interface GeneroServicio {
    GeneroDTO crear(GeneroDTO genero);

    GeneroDTO buscarPorId(int id);

    List<GeneroDTO> listar();

    GeneroDTO actualizar(GeneroDTO genero);

    void eliminar(int id);
}