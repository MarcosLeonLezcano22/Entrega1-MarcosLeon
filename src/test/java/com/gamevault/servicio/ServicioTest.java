package com.gamevault.servicio;

import com.gamevault.dao.DesarrolladoraDao;
import com.gamevault.dao.VideojuegoDao;
import com.gamevault.dto.GeneroDTO;
import com.gamevault.dto.VideojuegoDTO;
import com.gamevault.excepcion.NoEncontradoException;
import com.gamevault.excepcion.ValidacionException;
import com.gamevault.modelo.Desarrolladora;
import com.gamevault.modelo.Videojuego;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioTest {
    private VideojuegoDao videojuegoDao;
    private DesarrolladoraDao desarrolladoraDao;
    private VideojuegoServicio videojuegoServicio;

    @BeforeEach
    void prepararServicio() {
        videojuegoDao = mock(VideojuegoDao.class);
        desarrolladoraDao = mock(DesarrolladoraDao.class);
        videojuegoServicio = new VideojuegoServicioImpl(videojuegoDao, desarrolladoraDao);
    }

    @Test
    void rechazaTituloVacioYNoLlamaAlDao() {
        VideojuegoDTO videojuego = crearVideojuego(" ", Year.now().getValue(), BigDecimal.ZERO);

        assertThrows(ValidacionException.class, () -> videojuegoServicio.crear(videojuego));

        verify(videojuegoDao, never()).crear(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazaTituloMayorA100Caracteres() {
        VideojuegoDTO videojuego = crearVideojuego("a".repeat(101), Year.now().getValue(), BigDecimal.ZERO);

        assertThrows(ValidacionException.class, () -> videojuegoServicio.crear(videojuego));
    }

    @Test
    void rechazaAnioAnteriorAlMinimo() {
        assertThrows(ValidacionException.class,
                () -> videojuegoServicio.crear(crearVideojuego("Juego", 1969, BigDecimal.ZERO)));
    }

    @Test
    void rechazaAnioPosteriorAlActual() {
        assertThrows(ValidacionException.class,
                () -> videojuegoServicio.crear(crearVideojuego("Juego", Year.now().getValue() + 1, BigDecimal.ZERO)));
    }

    @Test
    void rechazaPrecioNegativo() {
        assertThrows(ValidacionException.class,
                () -> videojuegoServicio.crear(crearVideojuego("Juego", 1970, new BigDecimal("-0.01"))));
    }

    @Test
    void exigeDesarrolladoraExistenteYTransformaDtosAlGuardar() {
        VideojuegoDTO videojuego = crearVideojuego("a".repeat(100), 1970, BigDecimal.ZERO);
        videojuego.setGeneros(List.of(new GeneroDTO(2, "Aventura")));
        when(desarrolladoraDao.buscarPorId(3)).thenReturn(Optional.of(new Desarrolladora(3, "Estudio", "País")));
        when(videojuegoDao.crear(org.mockito.ArgumentMatchers.any(Videojuego.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        VideojuegoDTO resultado = videojuegoServicio.crear(videojuego);

        assertEquals(100, resultado.getTitulo().length());
        assertEquals(3, resultado.getDesarrolladoraId());
        assertEquals("Aventura", resultado.getGeneros().get(0).getNombre());
        verify(videojuegoDao).crear(org.mockito.ArgumentMatchers.argThat(entidad ->
                entidad.getGeneros().get(0).getNombre().equals("Aventura")));
        verify(videojuegoDao).crear(org.mockito.ArgumentMatchers.argThat(entidad ->
                entidad.getPrecio().compareTo(BigDecimal.ZERO) == 0));
    }

    @Test
    void fallaSiLaDesarrolladoraNoExiste() {
        when(desarrolladoraDao.buscarPorId(3)).thenReturn(Optional.empty());

        assertThrows(NoEncontradoException.class,
                () -> videojuegoServicio.crear(crearVideojuego("Juego", 1970, BigDecimal.ZERO)));
        verify(videojuegoDao, never()).crear(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void crearConGenerosNulosLosTrataComoListaVacia() {
        String json = "{\"titulo\":\"Juego\",\"anio\":" + Year.now().getValue()
                + ",\"precio\":19.99,\"desarrolladoraId\":3,\"generos\":null}";
        VideojuegoDTO videojuego = new Gson().fromJson(json, VideojuegoDTO.class);
        when(desarrolladoraDao.buscarPorId(3)).thenReturn(Optional.of(new Desarrolladora(3, "Estudio", "País")));
        when(videojuegoDao.crear(org.mockito.ArgumentMatchers.any(Videojuego.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        VideojuegoDTO resultado = videojuegoServicio.crear(videojuego);

        assertEquals(List.of(), resultado.getGeneros());
        verify(videojuegoDao).crear(org.mockito.ArgumentMatchers.argThat(entidad -> entidad.getGeneros().isEmpty()));
    }

    @Test
    void buscarVideojuegoInexistenteLanzaNoEncontrado() {
        when(videojuegoDao.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(NoEncontradoException.class, () -> videojuegoServicio.buscarPorId(99));
    }

    @Test
    void actualizarVideojuegoInexistenteLanzaNoEncontrado() {
        when(videojuegoDao.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(NoEncontradoException.class,
                () -> videojuegoServicio.actualizar(new VideojuegoDTO(99, "Juego", 1970, BigDecimal.ZERO, 3,
                        List.of())));
    }

    @Test
    void eliminarVideojuegoInexistenteLanzaNoEncontrado() {
        when(videojuegoDao.eliminar(99)).thenReturn(false);

        assertThrows(NoEncontradoException.class, () -> videojuegoServicio.eliminar(99));
    }

    @Test
    void noBorraDesarrolladoraConVideojuegosAsociados() {
        DesarrolladoraServicio servicio = new DesarrolladoraServicioImpl(desarrolladoraDao, videojuegoDao);
        when(desarrolladoraDao.buscarPorId(3)).thenReturn(Optional.of(new Desarrolladora(3, "Estudio", "País")));
        when(videojuegoDao.listar()).thenReturn(List.of(
                new Videojuego(8, "Juego", 2020, BigDecimal.TEN, 3, List.of())));

        assertThrows(ValidacionException.class, () -> servicio.eliminar(3));

        verify(desarrolladoraDao, never()).eliminar(3);
    }

    private VideojuegoDTO crearVideojuego(String titulo, int anio, BigDecimal precio) {
        return new VideojuegoDTO(0, titulo, anio, precio, 3, List.of());
    }
}