package com.gamevault.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Videojuego {
    private int id;
    private String titulo;
    private int anio;
    private BigDecimal precio;
    private int desarrolladoraId;
    private List<Genero> generos = new ArrayList<>();

    public Videojuego() {
    }

    public Videojuego(int id, String titulo, int anio, BigDecimal precio,
                      int desarrolladoraId, List<Genero> generos) {
        this.id = id;
        this.titulo = titulo;
        this.anio = anio;
        this.precio = precio;
        this.desarrolladoraId = desarrolladoraId;
        setGeneros(generos);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getDesarrolladoraId() {
        return desarrolladoraId;
    }

    public void setDesarrolladoraId(int desarrolladoraId) {
        this.desarrolladoraId = desarrolladoraId;
    }

    public List<Genero> getGeneros() {
        return new ArrayList<>(generos);
    }

    public void setGeneros(List<Genero> generos) {
        this.generos = generos == null ? new ArrayList<>() : new ArrayList<>(generos);
    }
}