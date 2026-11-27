package com.gamevault.servicio;

import com.gamevault.dto.DesarrolladoraDTO;

import java.util.List;

public interface DesarrolladoraServicio {
    DesarrolladoraDTO crear(DesarrolladoraDTO desarrolladora);

    DesarrolladoraDTO buscarPorId(int id);

    List<DesarrolladoraDTO> listar();

    DesarrolladoraDTO actualizar(DesarrolladoraDTO desarrolladora);

    void eliminar(int id);
}