package com.gamevault.dao;

import com.gamevault.excepcion.DaoException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

final class TransaccionesJdbc {
    private TransaccionesJdbc() {
    }

    static <T> T ejecutar(DataSource dataSource, Operacion<T> operacion, String mensajeError) {
        try (Connection conexion = dataSource.getConnection()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conexion);
                conexion.commit();
                return resultado;
            } catch (SQLException | RuntimeException error) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    error.addSuppressed(errorRollback);
                }
                throw error;
            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }
        } catch (SQLException error) {
            throw new DaoException(mensajeError, error);
        }
    }

    @FunctionalInterface
    interface Operacion<T> {
        T ejecutar(Connection conexion) throws SQLException;
    }
}