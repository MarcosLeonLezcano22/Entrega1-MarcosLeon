package com.gamevault.controlador;

import com.gamevault.dto.DesarrolladoraDTO;
import com.gamevault.servicio.DesarrolladoraServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/desarrolladoras")
public class DesarrolladoraServlet extends ApiServlet {
    private DesarrolladoraServicio servicio;

    public DesarrolladoraServlet() {
    }

    public DesarrolladoraServlet(DesarrolladoraServicio servicio) {
        this.servicio = servicio;
    }

    @Override
    public void init() throws ServletException {
        if (servicio == null) {
            servicio = obtenerServicio(DesarrolladoraServicio.class);
        }
    }

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        manejarErrores(respuesta, () -> escribirJson(respuesta, HttpServletResponse.SC_OK, servicio.listar()));
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        manejarErrores(respuesta, () -> {
            DesarrolladoraDTO desarrolladora = leerJson(peticion, DesarrolladoraDTO.class);
            escribirJson(respuesta, HttpServletResponse.SC_CREATED, servicio.crear(desarrolladora));
        });
    }
}