package com.gamevault.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public final class ConexionBD {
    private ConexionBD() {
    }

    public static HikariDataSource crearDataSource() {
        return crearDataSource(System.getenv());
    }

    static HikariDataSource crearDataSource(Map<String, String> variablesEntorno) {
        String host = obtenerVariable(variablesEntorno, "DB_HOST");
        String puerto = obtenerVariable(variablesEntorno, "DB_PORT");
        String nombre = obtenerVariable(variablesEntorno, "DB_NAME");
        String usuario = obtenerVariable(variablesEntorno, "DB_USER");
        String contrasena = obtenerVariable(variablesEntorno, "DB_PASSWORD");

        HikariConfig configuracion = new HikariConfig();
        configuracion.setJdbcUrl("jdbc:mysql://" + host + ":" + puerto + "/" + nombre
                + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8");
        configuracion.setUsername(usuario);
        configuracion.setPassword(contrasena);
        configuracion.setDriverClassName("com.mysql.cj.jdbc.Driver");
        configuracion.setPoolName("GameVaultPool");

        return new HikariDataSource(configuracion);
    }

    private static String obtenerVariable(Map<String, String> variablesEntorno, String nombre) {
        String valor = variablesEntorno.get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno obligatoria " + nombre);
        }
        return valor.trim();
    }
}