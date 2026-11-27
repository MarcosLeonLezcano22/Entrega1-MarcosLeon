DROP TABLE IF EXISTS videojuego_genero;
DROP TABLE IF EXISTS videojuego;
DROP TABLE IF EXISTS genero;
DROP TABLE IF EXISTS desarrolladora;

CREATE TABLE IF NOT EXISTS desarrolladora (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    pais VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS videojuego (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(150) NOT NULL,
    anio SMALLINT UNSIGNED NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    desarrolladora_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_videojuego_desarrolladora
        FOREIGN KEY (desarrolladora_id)
        REFERENCES desarrolladora (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS genero (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_genero_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS videojuego_genero (
    videojuego_id INT UNSIGNED NOT NULL,
    genero_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (videojuego_id, genero_id),
    CONSTRAINT fk_videojuego_genero_videojuego
        FOREIGN KEY (videojuego_id)
        REFERENCES videojuego (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_videojuego_genero_genero
        FOREIGN KEY (genero_id)
        REFERENCES genero (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO desarrolladora (id, nombre, pais) VALUES
    (1, 'Nintendo', 'Japón'),
    (2, 'CD Projekt Red', 'Polonia'),
    (3, 'Naughty Dog', 'Estados Unidos'),
    (4, 'Mojang Studios', 'Suecia');

INSERT INTO genero (id, nombre) VALUES
    (1, 'Acción'),
    (2, 'Aventura'),
    (3, 'Rol'),
    (4, 'Plataformas'),
    (5, 'Sandbox');

INSERT INTO videojuego (id, titulo, anio, precio, desarrolladora_id) VALUES
    (1, 'The Legend of Zelda: Tears of the Kingdom', 2023, 69.99, 1),
    (2, 'Super Mario Odyssey', 2017, 59.99, 1),
    (3, 'The Witcher 3: Wild Hunt', 2015, 39.99, 2),
    (4, 'Cyberpunk 2077', 2020, 59.99, 2),
    (5, 'The Last of Us Part II', 2020, 49.99, 3),
    (6, 'Uncharted 4: A Thief’s End', 2016, 39.99, 3),
    (7, 'Minecraft', 2011, 29.99, 4),
    (8, 'Minecraft Dungeons', 2020, 19.99, 4);

INSERT INTO videojuego_genero (videojuego_id, genero_id) VALUES
    (1, 2),
    (1, 3),
    (2, 2),
    (2, 4),
    (3, 1),
    (3, 2),
    (3, 3),
    (4, 1),
    (4, 3),
    (5, 1),
    (5, 2),
    (6, 1),
    (6, 2),
    (7, 5),
    (8, 1),
    (8, 3);