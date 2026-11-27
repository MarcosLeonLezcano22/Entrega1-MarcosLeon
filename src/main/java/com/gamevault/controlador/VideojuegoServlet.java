package com.gamevault.controlador;

import com.gamevault.dto.VideojuegoDTO;
import com.gamevault.excepcion.NoEncontradoException;
import com.gamevault.servicio.VideojuegoServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/videojuegos", "/api/videojuegos/*"})
public class VideojuegoServlet extends ApiServlet {
    private VideojuegoServicio servicio;

    public VideojuegoServlet() {
    }

    public VideojuegoServlet(VideojuegoServicio servicio) {
        this.servicio = servicio;
    }

    @Override
    public void init() throws ServletException {
        if (servicio == null) {
            servicio = obtenerServicio(VideojuegoServicio.class);
        }
    }

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        manejarErrores(respuesta, () -> {
            if (esRutaColeccion(peticion)) {
                escribirJson(respuesta, HttpServletResponse.SC_OK, servicio.listar());
            } else {
                escribirJson(respuesta, HttpServletResponse.SC_OK, servicio.buscarPorId(obtenerId(peticion)));
            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        manejarErrores(respuesta, () -> {
            if (!esRutaColeccion(peticion)) {
                throw new NoEncontradoException("Ruta no encontrada");
            }
            VideojuegoDTO videojuego = leerJson(peticion, VideojuegoDTO.class);
            escribirJson(respuesta, HttpServletResponse.SC_CREATED, servicio.crear(videojuego));
        });
    }

    @Override
    protected void doPut(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        manejarErrores(respuesta, () -> {
            int id = obtenerId(peticion);
            VideojuegoDTO videojuego = leerJson(peticion, VideojuegoDTO.class);
            videojuego.setId(id);
            escribirJson(respuesta, HttpServletResponse.SC_OK, servicio.actualizar(videojuego));
        });
    }

    @Override
    protected void doDelete(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        manejarErrores(respuesta, () -> {
            int id = obtenerId(peticion);
            servicio.eliminar(id);
            escribirJson(respuesta, HttpServletResponse.SC_OK, Map.of("mensaje", "Videojuego eliminado"));
        });
    }
}