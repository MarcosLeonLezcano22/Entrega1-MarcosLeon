package com.gamevault.dao;

import com.gamevault.excepcion.DaoException;
import com.gamevault.modelo.Genero;
import com.gamevault.modelo.Videojuego;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VideojuegoDaoJdbc implements VideojuegoDao {
    private final DataSource dataSource;

    public VideojuegoDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Videojuego crear(Videojuego videojuego) {
        return TransaccionesJdbc.ejecutar(dataSource, conexion -> {
            String sql = "INSERT INTO videojuego (titulo, anio, precio, desarrolladora_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement sentencia = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                establecerDatos(sentencia, videojuego);
                sentencia.executeUpdate();
                try (ResultSet claves = sentencia.getGeneratedKeys()) {
                    if (!claves.next()) {
                        throw new SQLException("No se pudo obtener el id del videojuego creado");
                    }
                    videojuego.setId(claves.getInt(1));
                }
            }
            guardarGeneros(conexion, videojuego);
            return videojuego;
        }, "Error al crear el videojuego y sus géneros");
    }

    @Override
    public Optional<Videojuego> buscarPorId(int id) {
        return TransaccionesJdbc.ejecutar(dataSource, conexion -> {
            String sql = "SELECT id, titulo, anio, precio, desarrolladora_id FROM videojuego WHERE id = ?";
            Videojuego videojuego;
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setInt(1, id);
                try (ResultSet resultado = sentencia.executeQuery()) {
                    if (!resultado.next()) {
                        return Optional.empty();
                    }
                    videojuego = mapear(resultado);
                }
            }
            videojuego.setGeneros(buscarGeneros(conexion, id));
            return Optional.of(videojuego);
        }, "Error al buscar el videojuego y sus géneros");
    }

    @Override
    public List<Videojuego> listar() {
        return TransaccionesJdbc.ejecutar(dataSource, conexion -> {
            List<Videojuego> videojuegos = new ArrayList<>();
            String sql = "SELECT id, titulo, anio, precio, desarrolladora_id FROM videojuego ORDER BY id";
            try (PreparedStatement sentencia = conexion.prepareStatement(sql);
                 ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    videojuegos.add(mapear(resultado));
                }
            }
            for (Videojuego videojuego : videojuegos) {
                videojuego.setGeneros(buscarGeneros(conexion, videojuego.getId()));
            }
            return videojuegos;
        }, "Error al listar los videojuegos y sus géneros");
    }

    @Override
    public boolean actualizar(Videojuego videojuego) {
        return TransaccionesJdbc.ejecutar(dataSource, conexion -> {
            String sql = "UPDATE videojuego SET titulo = ?, anio = ?, precio = ?, desarrolladora_id = ? WHERE id = ?";
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                establecerDatos(sentencia, videojuego);
                sentencia.setInt(5, videojuego.getId());
                if (sentencia.executeUpdate() == 0 && !existe(conexion, videojuego.getId())) {
                    return false;
                }
            }
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    "DELETE FROM videojuego_genero WHERE videojuego_id = ?")) {
                sentencia.setInt(1, videojuego.getId());
                sentencia.executeUpdate();
            }
            guardarGeneros(conexion, videojuego);
            return true;
        }, "Error al actualizar el videojuego y sus géneros");
    }

    @Override
    public boolean eliminar(int id) {
        return TransaccionesJdbc.ejecutar(dataSource, conexion -> {
            try (PreparedStatement sentencia = conexion.prepareStatement("DELETE FROM videojuego WHERE id = ?")) {
                sentencia.setInt(1, id);
                return sentencia.executeUpdate() > 0;
            }
        }, "Error al eliminar el videojuego");
    }

    private void guardarGeneros(Connection conexion, Videojuego videojuego) throws SQLException {
        String sql = "INSERT INTO videojuego_genero (videojuego_id, genero_id) VALUES (?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (Genero genero : videojuego.getGeneros()) {
                sentencia.setInt(1, videojuego.getId());
                sentencia.setInt(2, genero.getId());
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }

    private List<Genero> buscarGeneros(Connection conexion, int videojuegoId) throws SQLException {
        String sql = "SELECT g.id, g.nombre FROM genero g "
                + "INNER JOIN videojuego_genero vg ON vg.genero_id = g.id "
                + "WHERE vg.videojuego_id = ? ORDER BY g.id";
        List<Genero> generos = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, videojuegoId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    generos.add(new Genero(resultado.getInt("id"), resultado.getString("nombre")));
                }
            }
        }
        return generos;
    }

    private boolean existe(Connection conexion, int videojuegoId) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "SELECT 1 FROM videojuego WHERE id = ?")) {
            sentencia.setInt(1, videojuegoId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        }
    }

    private Videojuego mapear(ResultSet resultado) throws SQLException {
        Videojuego videojuego = new Videojuego();
        videojuego.setId(resultado.getInt("id"));
        videojuego.setTitulo(resultado.getString("titulo"));
        videojuego.setAnio(resultado.getInt("anio"));
        videojuego.setPrecio(resultado.getBigDecimal("precio"));
        videojuego.setDesarrolladoraId(resultado.getInt("desarrolladora_id"));
        return videojuego;
    }

    private void establecerDatos(PreparedStatement sentencia, Videojuego videojuego) throws SQLException {
        sentencia.setString(1, videojuego.getTitulo());
        sentencia.setInt(2, videojuego.getAnio());
        sentencia.setBigDecimal(3, videojuego.getPrecio());
        sentencia.setInt(4, videojuego.getDesarrolladoraId());
    }
}