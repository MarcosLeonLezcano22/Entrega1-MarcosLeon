package com.gamevault.dao;

import com.gamevault.excepcion.DaoException;
import com.gamevault.modelo.Desarrolladora;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DesarrolladoraDaoJdbc implements DesarrolladoraDao {
    private final DataSource dataSource;

    public DesarrolladoraDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Desarrolladora crear(Desarrolladora desarrolladora) {
        String sql = "INSERT INTO desarrolladora (nombre, pais) VALUES (?, ?)";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, desarrolladora.getNombre());
            sentencia.setString(2, desarrolladora.getPais());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se pudo obtener el id de la desarrolladora creada");
                }
                desarrolladora.setId(claves.getInt(1));
                return desarrolladora;
            }
        } catch (SQLException error) {
            throw new DaoException("Error al crear la desarrolladora", error);
        }
    }

    @Override
    public Optional<Desarrolladora> buscarPorId(int id) {
        String sql = "SELECT id, nombre, pais FROM desarrolladora WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException error) {
            throw new DaoException("Error al buscar la desarrolladora", error);
        }
    }

    @Override
    public List<Desarrolladora> listar() {
        String sql = "SELECT id, nombre, pais FROM desarrolladora ORDER BY id";
        List<Desarrolladora> desarrolladoras = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                desarrolladoras.add(mapear(resultado));
            }
            return desarrolladoras;
        } catch (SQLException error) {
            throw new DaoException("Error al listar las desarrolladoras", error);
        }
    }

    @Override
    public boolean actualizar(Desarrolladora desarrolladora) {
        String sql = "UPDATE desarrolladora SET nombre = ?, pais = ? WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, desarrolladora.getNombre());
            sentencia.setString(2, desarrolladora.getPais());
            sentencia.setInt(3, desarrolladora.getId());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException error) {
            throw new DaoException("Error al actualizar la desarrolladora", error);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM desarrolladora WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException error) {
            throw new DaoException("Error al eliminar la desarrolladora", error);
        }
    }

    private Desarrolladora mapear(ResultSet resultado) throws SQLException {
        return new Desarrolladora(resultado.getInt("id"), resultado.getString("nombre"),
                resultado.getString("pais"));
    }
}