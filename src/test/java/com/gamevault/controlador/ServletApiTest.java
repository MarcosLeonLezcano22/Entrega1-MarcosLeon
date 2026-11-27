package com.gamevault.controlador;

import com.gamevault.dto.DesarrolladoraDTO;
import com.gamevault.dto.GeneroDTO;
import com.gamevault.dto.VideojuegoDTO;
import com.gamevault.excepcion.DaoException;
import com.gamevault.excepcion.NoEncontradoException;
import com.gamevault.excepcion.ValidacionException;
import com.gamevault.servicio.DesarrolladoraServicio;
import com.gamevault.servicio.GeneroServicio;
import com.gamevault.servicio.VideojuegoServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServletApiTest {
    @Test
    void videojuegoGetListaYPostDeleganAlServicioYDevuelvenEstadosHttp() throws IOException {
        VideojuegoServicio servicio = mock(VideojuegoServicio.class);
        VideojuegoDTO videojuego = new VideojuegoDTO(1, "Zelda", 2023, new BigDecimal("59.99"), 1, List.of());
        when(servicio.listar()).thenReturn(List.of(videojuego));
        when(servicio.crear(org.mockito.ArgumentMatchers.any())).thenReturn(videojuego);
        VideojuegoServlet servlet = new VideojuegoServlet(servicio);

        HttpServletRequest peticionLista = mock(HttpServletRequest.class);
        Respuesta respuestaLista = new Respuesta();
        servlet.doGet(peticionLista, respuestaLista.respuesta());

        assertEquals(HttpServletResponse.SC_OK, respuestaLista.estado);
        assertTrue(respuestaLista.cuerpo.toString().contains("Zelda"));

        HttpServletRequest peticionCrear = mock(HttpServletRequest.class);
        when(peticionCrear.getReader()).thenReturn(new BufferedReader(new StringReader(
                "{\"titulo\":\"Zelda\",\"anio\":2023,\"precio\":59.99,\"desarrolladoraId\":1}")));
        Respuesta respuestaCrear = new Respuesta();
        servlet.doPost(peticionCrear, respuestaCrear.respuesta());

        assertEquals(HttpServletResponse.SC_CREATED, respuestaCrear.estado);
        verify(servicio).crear(org.mockito.ArgumentMatchers.any(VideojuegoDTO.class));
    }

    @Test
    void videojuegoTraduceExcepcionesDeServicioAJsonYEstadosSemanticos() throws IOException {
        VideojuegoServicio servicio = mock(VideojuegoServicio.class);
        when(servicio.buscarPorId(4)).thenThrow(new NoEncontradoException("No existe el videojuego"));
        VideojuegoServlet servlet = new VideojuegoServlet(servicio);
        HttpServletRequest peticion = mock(HttpServletRequest.class);
        when(peticion.getPathInfo()).thenReturn("/4");
        Respuesta respuesta = new Respuesta();

        servlet.doGet(peticion, respuesta.respuesta());

        assertEquals(HttpServletResponse.SC_NOT_FOUND, respuesta.estado);
        assertTrue(respuesta.cuerpo.toString().contains("\"error\":\"No existe el videojuego\""));

        when(servicio.buscarPorId(5)).thenThrow(new DaoException("Error de base de datos", new RuntimeException()));
        when(peticion.getPathInfo()).thenReturn("/5");
        Respuesta respuestaDao = new Respuesta();
        servlet.doGet(peticion, respuestaDao.respuesta());

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, respuestaDao.estado);
        assertTrue(respuestaDao.cuerpo.toString().contains("\"error\":\"Error de base de datos\""));
    }

    @Test
    void desarrolladorasYGenerosExponenSusOperacionesSolicitadas() throws IOException {
        DesarrolladoraServicio servicioDesarrolladoras = mock(DesarrolladoraServicio.class);
        DesarrolladoraDTO desarrolladora = new DesarrolladoraDTO(1, "Nintendo", "Japón");
        when(servicioDesarrolladoras.listar()).thenReturn(List.of(desarrolladora));
        when(servicioDesarrolladoras.crear(org.mockito.ArgumentMatchers.any())).thenReturn(desarrolladora);
        DesarrolladoraServlet servletDesarrolladoras = new DesarrolladoraServlet(servicioDesarrolladoras);

        Respuesta respuestaLista = new Respuesta();
        servletDesarrolladoras.doGet(mock(HttpServletRequest.class), respuestaLista.respuesta());
        assertEquals(HttpServletResponse.SC_OK, respuestaLista.estado);

        HttpServletRequest peticionCrear = mock(HttpServletRequest.class);
        when(peticionCrear.getReader()).thenReturn(new BufferedReader(new StringReader(
                "{\"nombre\":\"Nintendo\",\"pais\":\"Japón\"}")));
        Respuesta respuestaCrear = new Respuesta();
        servletDesarrolladoras.doPost(peticionCrear, respuestaCrear.respuesta());
        assertEquals(HttpServletResponse.SC_CREATED, respuestaCrear.estado);

        GeneroServicio servicioGeneros = mock(GeneroServicio.class);
        when(servicioGeneros.listar()).thenReturn(List.of(new GeneroDTO(1, "Aventura")));
        Respuesta respuestaGeneros = new Respuesta();
        new GeneroServlet(servicioGeneros).doGet(mock(HttpServletRequest.class), respuestaGeneros.respuesta());
        assertEquals(HttpServletResponse.SC_OK, respuestaGeneros.estado);
    }

    @Test
    void videojuegoDevuelveBadRequestParaDatosInvalidos() throws IOException {
        VideojuegoServicio servicio = mock(VideojuegoServicio.class);
        when(servicio.crear(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ValidacionException("El título es obligatorio"));
        VideojuegoServlet servlet = new VideojuegoServlet(servicio);
        HttpServletRequest peticion = mock(HttpServletRequest.class);
        when(peticion.getReader()).thenReturn(new BufferedReader(new StringReader("{\"titulo\":\"\"}")));
        Respuesta respuesta = new Respuesta();

        servlet.doPost(peticion, respuesta.respuesta());

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, respuesta.estado);
        assertTrue(respuesta.cuerpo.toString().contains("\"error\":\"El título es obligatorio\""));
    }

    @Test
    void videojuegoActualizaYEliminaUsandoElIdDeLaRuta() throws IOException {
        VideojuegoServicio servicio = mock(VideojuegoServicio.class);
        VideojuegoDTO videojuego = new VideojuegoDTO(7, "Zelda", 2023, new BigDecimal("59.99"), 1, List.of());
        when(servicio.actualizar(org.mockito.ArgumentMatchers.any(VideojuegoDTO.class))).thenReturn(videojuego);
        VideojuegoServlet servlet = new VideojuegoServlet(servicio);

        HttpServletRequest peticionActualizar = mock(HttpServletRequest.class);
        when(peticionActualizar.getPathInfo()).thenReturn("/7");
        when(peticionActualizar.getReader()).thenReturn(new BufferedReader(new StringReader(
                "{\"titulo\":\"Zelda\",\"anio\":2023,\"precio\":59.99,\"desarrolladoraId\":1}")));
        Respuesta respuestaActualizar = new Respuesta();
        servlet.doPut(peticionActualizar, respuestaActualizar.respuesta());
        assertEquals(HttpServletResponse.SC_OK, respuestaActualizar.estado);
        verify(servicio).actualizar(org.mockito.ArgumentMatchers.argThat(dto -> dto.getId() == 7));

        HttpServletRequest peticionEliminar = mock(HttpServletRequest.class);
        when(peticionEliminar.getPathInfo()).thenReturn("/7");
        Respuesta respuestaEliminar = new Respuesta();
        servlet.doDelete(peticionEliminar, respuestaEliminar.respuesta());
        assertEquals(HttpServletResponse.SC_OK, respuestaEliminar.estado);
        verify(servicio).eliminar(7);
    }

    private static class Respuesta {
        private final StringWriter cuerpo = new StringWriter();
        private int estado;

        private HttpServletResponse respuesta() throws IOException {
            HttpServletResponse respuesta = mock(HttpServletResponse.class);
            when(respuesta.getWriter()).thenReturn(new PrintWriter(cuerpo));
            org.mockito.Mockito.doAnswer(invocacion -> {
                estado = invocacion.getArgument(0);
                return null;
            }).when(respuesta).setStatus(org.mockito.ArgumentMatchers.anyInt());
            return respuesta;
        }
    }
}