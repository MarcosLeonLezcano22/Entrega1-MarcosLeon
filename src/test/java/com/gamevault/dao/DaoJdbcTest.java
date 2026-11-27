package com.gamevault.dao;

import com.gamevault.excepcion.DaoException;
import com.gamevault.modelo.Desarrolladora;
import com.gamevault.modelo.Genero;
import com.gamevault.modelo.Videojuego;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DaoJdbcTest {
    private JdbcDataSource dataSource;
    private DesarrolladoraDaoJdbc desarrolladoraDao;
    private GeneroDaoJdbc generoDao;
    private VideojuegoDaoJdbc videojuegoDao;

    @BeforeEach
    void prepararBaseDeDatos() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:gamevault" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        crearEsquema();
        desarrolladoraDao = new DesarrolladoraDaoJdbc(dataSource);
        generoDao = new GeneroDaoJdbc(dataSource);
        videojuegoDao = new VideojuegoDaoJdbc(dataSource);
        desarrolladoraDao.crear(new Desarrolladora(0, "Nintendo", "Japón"));
        generoDao.crear(new Genero(0, "Aventura"));
        generoDao.crear(new Genero(0, "Rol"));
    }

    @Test
    void videojuegoPersisteYRecuperaSusGenerosYPermiteActualizarYEliminar() {
        Videojuego videojuego = videojuegoDao.crear(crearVideojuego(List.of(new Genero(1, ""), new Genero(2, ""))));

        Videojuego encontrado = videojuegoDao.buscarPorId(videojuego.getId()).orElseThrow();
        assertEquals("Juego de prueba", encontrado.getTitulo());
        assertEquals(2024, encontrado.getAnio());
        assertEquals(new BigDecimal("19.99"), encontrado.getPrecio());
        assertEquals(1, encontrado.getDesarrolladoraId());
        assertEquals(List.of(1, 2), encontrado.getGeneros().stream().map(Genero::getId).toList());
        assertEquals(1, videojuegoDao.listar().size());

        videojuego.setGeneros(List.of(new Genero(2, "")));
        assertTrue(videojuegoDao.actualizar(videojuego));
        assertEquals("Juego de prueba", videojuegoDao.buscarPorId(videojuego.getId()).orElseThrow().getTitulo());
        assertEquals(List.of(2), videojuegoDao.buscarPorId(videojuego.getId()).orElseThrow()
                .getGeneros().stream().map(Genero::getId).toList());

        videojuego.setTitulo("Aventura actualizada");
        assertTrue(videojuegoDao.actualizar(videojuego));
        assertEquals("Aventura actualizada", videojuegoDao.buscarPorId(videojuego.getId()).orElseThrow().getTitulo());

        assertTrue(videojuegoDao.eliminar(videojuego.getId()));
        assertTrue(videojuegoDao.buscarPorId(videojuego.getId()).isEmpty());
    }

    @Test
    void buscarIdInexistenteDevuelveResultadoVacio() {
        assertTrue(videojuegoDao.buscarPorId(999).isEmpty());
    }

    @Test
    void crearConDesarrolladoraInexistenteLanzaErrorDeIntegridadYNoPersiste() {
        Videojuego videojuego = new Videojuego(0, "Juego sin desarrolladora", 2024,
                new BigDecimal("19.99"), 999, List.of());

        assertThrows(DaoException.class, () -> videojuegoDao.crear(videojuego));

        assertTrue(videojuegoDao.listar().isEmpty());
    }

    @Test
    void errorAlGuardarGeneroRevierteLaCreacionDelVideojuego() {
        assertThrows(DaoException.class,
                () -> videojuegoDao.crear(crearVideojuego(List.of(new Genero(999, "")))));

        assertTrue(videojuegoDao.listar().isEmpty());
    }

    @Test
    void errorAlGuardarGenerosEnActualizacionRevierteLosCambios() {
        Videojuego original = videojuegoDao.crear(crearVideojuego(List.of(new Genero(1, ""))));
        Videojuego actualizado = new Videojuego(original.getId(), "Juego actualizado", 2025,
                new BigDecimal("29.99"), original.getDesarrolladoraId(), List.of(new Genero(999, "")));

        assertThrows(DaoException.class, () -> videojuegoDao.actualizar(actualizado));

        Videojuego persistido = videojuegoDao.buscarPorId(original.getId()).orElseThrow();
        assertEquals("Juego de prueba", persistido.getTitulo());
        assertEquals(2024, persistido.getAnio());
        assertEquals(new BigDecimal("19.99"), persistido.getPrecio());
        assertEquals(original.getDesarrolladoraId(), persistido.getDesarrolladoraId());
        assertEquals(List.of(1), persistido.getGeneros().stream().map(Genero::getId).toList());
    }

    @Test
    void daoDeDesarrolladoraYGeneroCompletaOperacionesCrud() {
        Desarrolladora desarrolladora = desarrolladoraDao.listar().get(0);
        desarrolladora.setPais("Japón actualizado");
        assertTrue(desarrolladoraDao.actualizar(desarrolladora));
        assertEquals("Japón actualizado", desarrolladoraDao.buscarPorId(desarrolladora.getId()).orElseThrow().getPais());
        assertTrue(desarrolladoraDao.eliminar(desarrolladora.getId()));
        assertFalse(desarrolladoraDao.eliminar(desarrolladora.getId()));

        Genero genero = generoDao.buscarPorId(1).orElseThrow();
        genero.setNombre("Exploración");
        assertTrue(generoDao.actualizar(genero));
        assertEquals("Exploración", generoDao.buscarPorId(genero.getId()).orElseThrow().getNombre());
        assertTrue(generoDao.eliminar(genero.getId()));
        assertFalse(generoDao.buscarPorId(genero.getId()).isPresent());
    }

    private Videojuego crearVideojuego(List<Genero> generos) {
        return new Videojuego(0, "Juego de prueba", 2024, new BigDecimal("19.99"), 1, generos);
    }

    private void crearEsquema() throws SQLException {
        try (Connection conexion = dataSource.getConnection(); Statement sentencia = conexion.createStatement()) {
            sentencia.execute("CREATE TABLE desarrolladora (id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "nombre VARCHAR(100) NOT NULL, pais VARCHAR(100) NOT NULL)");
            sentencia.execute("CREATE TABLE videojuego (id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "titulo VARCHAR(150) NOT NULL, anio SMALLINT NOT NULL, precio DECIMAL(10, 2) NOT NULL, "
                    + "desarrolladora_id INT NOT NULL, FOREIGN KEY (desarrolladora_id) REFERENCES desarrolladora(id) "
                    + "ON DELETE RESTRICT ON UPDATE CASCADE)");
            sentencia.execute("CREATE TABLE genero (id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "nombre VARCHAR(50) NOT NULL UNIQUE)");
            sentencia.execute("CREATE TABLE videojuego_genero (videojuego_id INT NOT NULL, genero_id INT NOT NULL, "
                    + "PRIMARY KEY (videojuego_id, genero_id), FOREIGN KEY (videojuego_id) REFERENCES videojuego(id) "
                    + "ON DELETE CASCADE, FOREIGN KEY (genero_id) REFERENCES genero(id) ON DELETE CASCADE)");
        }
    }
}