package com.gamevault.config;

import com.gamevault.dao.DesarrolladoraDao;
import com.gamevault.dao.DesarrolladoraDaoJdbc;
import com.gamevault.dao.GeneroDao;
import com.gamevault.dao.GeneroDaoJdbc;
import com.gamevault.dao.VideojuegoDao;
import com.gamevault.dao.VideojuegoDaoJdbc;
import com.gamevault.servicio.DesarrolladoraServicio;
import com.gamevault.servicio.DesarrolladoraServicioImpl;
import com.gamevault.servicio.GeneroServicio;
import com.gamevault.servicio.GeneroServicioImpl;
import com.gamevault.servicio.VideojuegoServicio;
import com.gamevault.servicio.VideojuegoServicioImpl;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AplicacionListener implements ServletContextListener {
    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        dataSource = ConexionBD.crearDataSource();
        ServletContext contexto = evento.getServletContext();

        VideojuegoDao videojuegoDao = new VideojuegoDaoJdbc(dataSource);
        DesarrolladoraDao desarrolladoraDao = new DesarrolladoraDaoJdbc(dataSource);
        GeneroDao generoDao = new GeneroDaoJdbc(dataSource);

        contexto.setAttribute(VideojuegoServicio.class.getName(),
                new VideojuegoServicioImpl(videojuegoDao, desarrolladoraDao));
        contexto.setAttribute(DesarrolladoraServicio.class.getName(),
                new DesarrolladoraServicioImpl(desarrolladoraDao, videojuegoDao));
        contexto.setAttribute(GeneroServicio.class.getName(), new GeneroServicioImpl(generoDao));
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}