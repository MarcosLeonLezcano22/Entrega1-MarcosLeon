package com.gamevault.dao;

import com.gamevault.excepcion.DaoException;
import com.gamevault.modelo.Genero;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GeneroDaoJdbc implements GeneroDao {
    private final DataSource dataSource;

    public GeneroDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Genero crear(Genero genero) {
        String sql = "INSERT INTO genero (nombre) VALUES (?)";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, genero.getNombre());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se pudo obtener el id del género creado");
                }
                genero.setId(claves.getInt(1));
                return genero;
            }
        } catch (SQLException error) {
            throw new DaoException("Error al crear el género", error);
        }
    }

    @Override
    public Optional<Genero> buscarPorId(int id) {
        String sql = "SELECT id, nombre FROM genero WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException error) {
            throw new DaoException("Error al buscar el género", error);
        }
    }

    @Override
    public List<Genero> listar() {
        String sql = "SELECT id, nombre FROM genero ORDER BY id";
        List<Genero> generos = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                generos.add(mapear(resultado));
            }
            return generos;
        } catch (SQLException error) {
            throw new DaoException("Error al listar los géneros", error);
        }
    }

    @Override
    public boolean actualizar(Genero genero) {
        String sql = "UPDATE genero SET nombre = ? WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, genero.getNombre());
            sentencia.setInt(2, genero.getId());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException error) {
            throw new DaoException("Error al actualizar el género", error);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM genero WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException error) {
            throw new DaoException("Error al eliminar el género", error);
        }
    }

    private Genero mapear(ResultSet resultado) throws SQLException {
        return new Genero(resultado.getInt("id"), resultado.getString("nombre"));
    }
}