# AI_WORKFLOW.md – GameVault

**Autor:** Marcos León Lezcano
**Módulo:** Acceso a Datos 
**Herramienta de IA:** Junie (agente de codificación de IntelliJ IDEA)
## Planteamiento
Yo actúo como arquitecto de software, revisor de código y responsable del despliegue. Junie genera el código paso a paso con prompts acotados. Después reviso cada resultado, corrijo lo que haga falta y hago un commit por paso.

## Decisiones arquitectónicas
- **Temática:** catálogo de videojuegos (GameVault).
- **Stack:** Java 17, Maven, Servlets Jakarta en Tomcat 10.1.
- **Acceso a datos:** JDBC puro con PreparedStatement y pool HikariCP.
- **Arquitectura:** Controlador → Servicio → DAO → BD, con DTO entre capas.
- **Configuración:** credenciales por variables de entorno (.env), nada hardcodeado.
- **Tests:** JUnit 5 + Mockito para el servicio y H2 (modo MySQL) para el DAO.
- **Despliegue:** Docker Compose con app, MySQL y Adminer.

## Registro de prompts
### Paso 1 – Contexto y esqueleto
**Prompt:**
```
Vas a ayudarme a construir una aplicación web Java llamada GameVault (catálogo de videojuegos) para un módulo de Acceso a Datos. Reglas del proyecto:
- Java 17, Maven, empaquetado .war, Servlets con Jakarta EE 10 (Tomcat 10.1).
- Arquitectura por capas estricta: controlador (servlets) -> servicio -> dao -> BD. Uso obligatorio de DTOs entre capas, sin exponer entidades.
- Acceso a datos con JDBC puro, PreparedStatement siempre, try-with-resources y pool HikariCP.
- Prohibido hardcodear credenciales: la conexión se lee de variables de entorno (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD).
- Código simple, con nombres de variables y comentarios en español.
Ahora crea solo el pom.xml (dependencias: jakarta.servlet-api provided, mysql-connector-j, HikariCP, Gson, JUnit 5, Mockito, H2 para test) y la estructura de paquetes com.gamevault.{config,controlador,servicio,dao,dto,modelo,excepcion}. No generes más código todavía.
```
**Resultado de Junie: actualizó pom.xml (Java 17, WAR, Servlet API Jakarta 6 provided, MySQL Connector/J, HikariCP, Gson, JUnit 5, Mockito, H2 en test) y creó las 7 carpetas de com.gamevault. Borró código previo que existía en el proyecto.** 
**Cambios manuales míos:Ninguno ** 
**Commit:** (mensaje del commit)
**Observaciones:**Junie no pudo ejecutar Maven (no está en el PATH); validé el pom.xml desde IntelliJ.

### Paso 2 – Base de datos (init.sql)
**Prompt:**
```
Crea db/init.sql para MySQL 8 con estas tablas: desarrolladora(id, nombre, pais), videojuego(id, titulo, anio, precio, desarrolladora_id FK ON DELETE RESTRICT ON UPDATE CASCADE), genero(id, nombre UNIQUE) y videojuego_genero(videojuego_id, genero_id, PK compuesta, ambas FK ON DELETE CASCADE). Incluye DDL (con DROP/CREATE IF NOT EXISTS) y datos de prueba: 4 desarrolladoras, 8 videojuegos, 5 géneros y sus relaciones. Charset utf8mb4.
```
**Resultado de Junie:** creó `db/init.sql` para MySQL 8 con las 4 tablas solicitadas (`desarrolladora`, `videojuego`, `genero`, `videojuego_genero`), motor InnoDB y charset utf8mb4, con claves primarias y foráneas (RESTRICT en videojuego→desarrolladora y CASCADE en la tabla intermedia). Elimina las tablas en orden de dependencia y las vuelve a crear con IF NOT EXISTS. Incluye datos de prueba: 4 desarrolladoras, 8 videojuegos, 5 géneros y 16 relaciones videojuego-género.
**Observaciones:** Junie no pudo ejecutar el script porque no hay cliente MySQL en el entorno, solo lo revisó a mano. Lo validaré al levantar el contenedor con Docker Compose.
**Cambios manuales míos:** ninguno
