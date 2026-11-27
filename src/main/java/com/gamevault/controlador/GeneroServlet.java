package com.gamevault.controlador;

import com.gamevault.servicio.GeneroServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/generos")
public class GeneroServlet extends ApiServlet {
    private GeneroServicio servicio;

    public GeneroServlet() {
    }

    public GeneroServlet(GeneroServicio servicio) {
        this.servicio = servicio;
    }

    @Override
    public void init() throws ServletException {
        if (servicio == null) {
            servicio = obtenerServicio(GeneroServicio.class);
        }
    }

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        manejarErrores(respuesta, () -> escribirJson(respuesta, HttpServletResponse.SC_OK, servicio.listar()));
    }
}