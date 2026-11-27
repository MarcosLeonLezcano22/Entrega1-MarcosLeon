package com.gamevault.controlador;

import com.gamevault.excepcion.DaoException;
import com.gamevault.excepcion.NoEncontradoException;
import com.gamevault.excepcion.ValidacionException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public abstract class ApiServlet extends HttpServlet {
    private static final Gson GSON = new Gson();

    @FunctionalInterface
    protected interface OperacionHttp {
        void ejecutar() throws IOException;
    }

    protected final <T> T obtenerServicio(Class<T> tipo) throws ServletException {
        Object servicio = getServletContext().getAttribute(tipo.getName());
        if (!tipo.isInstance(servicio)) {
            throw new ServletException("No se encontró el servicio " + tipo.getSimpleName());
        }
        return tipo.cast(servicio);
    }

    protected final void manejarErrores(HttpServletResponse respuesta, OperacionHttp operacion)
            throws IOException {
        try {
            operacion.ejecutar();
        } catch (ValidacionException excepcion) {
            escribirError(respuesta, HttpServletResponse.SC_BAD_REQUEST, excepcion.getMessage());
        } catch (NoEncontradoException excepcion) {
            escribirError(respuesta, HttpServletResponse.SC_NOT_FOUND, excepcion.getMessage());
        } catch (DaoException excepcion) {
            escribirError(respuesta, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, excepcion.getMessage());
        } catch (JsonSyntaxException excepcion) {
            escribirError(respuesta, HttpServletResponse.SC_BAD_REQUEST, "El cuerpo JSON no es válido");
        }
    }

    protected final void escribirJson(HttpServletResponse respuesta, int estado, Object contenido)
            throws IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        respuesta.getWriter().write(GSON.toJson(contenido));
    }

    protected final void escribirError(HttpServletResponse respuesta, int estado, String mensaje)
            throws IOException {
        escribirJson(respuesta, estado, Map.of("error", mensaje == null ? "Error" : mensaje));
    }

    protected final <T> T leerJson(HttpServletRequest peticion, Class<T> tipo) throws IOException {
        T contenido = GSON.fromJson(peticion.getReader(), tipo);
        if (contenido == null) {
            throw new ValidacionException("El cuerpo JSON es obligatorio");
        }
        return contenido;
    }

    protected final boolean esRutaColeccion(HttpServletRequest peticion) {
        String ruta = peticion.getPathInfo();
        return ruta == null || ruta.isEmpty() || "/".equals(ruta);
    }

    protected final int obtenerId(HttpServletRequest peticion) {
        String ruta = peticion.getPathInfo();
        if (ruta == null || !ruta.matches("/\\d+")) {
            throw new ValidacionException("La ruta debe incluir un id numérico válido");
        }
        try {
            int id = Integer.parseInt(ruta.substring(1));
            if (id <= 0) {
                throw new ValidacionException("El id debe ser mayor que cero");
            }
            return id;
        } catch (NumberFormatException excepcion) {
            throw new ValidacionException("El id indicado no es válido");
        }
    }
}