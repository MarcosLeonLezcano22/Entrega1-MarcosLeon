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
### Paso 3 – Configuración, modelo y capa DAO
**Prompt:**
```
Crea: (1) la clase config.ConexionBD que cree un DataSource HikariCP leyendo las variables de entorno DB_HOST, DB_PORT, DB_NAME, DB_USER y DB_PASSWORD, fallando con un mensaje claro si falta alguna; (2) las entidades del paquete modelo; (3) las interfaces y las implementaciones JDBC del paquete dao para Videojuego, Desarrolladora y Genero con CRUD completo (el DAO de Videojuego debe guardar y leer también sus géneros en la tabla intermedia usando una transacción). Usa PreparedStatement, try-with-resources y lanza una excepción propia DaoException (paquete excepcion) en vez de devolver null o imprimir trazas. El DAO debe recibir el DataSource por constructor para poder testearlo.
```
**Resultado de Junie:** creó `ConexionBD` (pool HikariCP que lee DB_HOST, DB_PORT, DB_NAME, DB_USER y DB_PASSWORD de variables de entorno y falla indicando cuál falta), las entidades `Videojuego`, `Desarrolladora` y `Genero`, la excepción `DaoException`, las interfaces DAO y sus implementaciones JDBC con PreparedStatement y recursos cerrados. El DAO de videojuegos guarda, actualiza y lee los géneros dentro de transacciones (clase auxiliar `TransaccionesJdbc`).
**Observaciones:**
- Junie añadió por su cuenta un test (`DaoJdbcTest`, con H2) que yo no había pedido en este paso.
- No pudo ejecutar `mvn test`: en su entorno no hay Maven y solo hay Java 11, y el proyecto requiere Java 17. Los tests los ejecuto yo desde IntelliJ con JDK 17.
  **Cambios manuales míos:** He tenidoque cambiar el SDK del 25 al 17
  **Commit:** `feat: conexión HikariCP, modelo y capa DAO JDBC` 
### Paso 4 – DTOs y capa de servicio
**Prompt:**
```
Crea los DTOs (VideojuegoDTO, DesarrolladoraDTO, GeneroDTO) y la capa de servicio con sus interfaces. El servicio convierte entidad<->DTO y aplica reglas de negocio: título obligatorio y máximo 100 caracteres, año entre 1970 y el año actual, precio >= 0, la desarrolladora debe existir, no se puede borrar una desarrolladora con videojuegos. Lanza ValidacionException (datos inválidos), NoEncontradoException (id inexistente) y DaoException.
```
**Resultado de Junie:** creó `VideojuegoDTO`, `DesarrolladoraDTO` y `GeneroDTO`, las excepciones `ValidacionException` y `NoEncontradoException`, y las interfaces con sus implementaciones de servicio (`VideojuegoServicioImpl`, `DesarrolladoraServicioImpl`, `GeneroServicioImpl`). Los servicios convierten DTO↔entidad, aplican las reglas de validación y lanzan error si el registro no existe. El servicio de desarrolladoras impide borrar las que tengan videojuegos asociados.
**Observaciones:**
- Junie añadió por su cuenta tests unitarios (`ServicioTest`) que no había pedido en este paso.
- No pudo ejecutar `mvn test` (Maven no disponible en su entorno). Los ejecuto yo desde IntelliJ.
- Detectó cambios en `.idea/misc.xml` y `AI_WORKFLOW.md` y los dejó intactos.
  **Cambios manuales míos:** ninguno
  **Commit:** `feat: DTOs, excepciones y capa de servicio con validaciones` 
### Paso 5 – API REST con Servlets
**Prompt:**
```
Crea los servlets REST en el paquete controlador: /api/videojuegos (GET lista, POST crear), /api/videojuegos/{id} (GET, PUT, DELETE), /api/desarrolladoras (GET, POST) y /api/generos (GET). Solo deben recibir la petición, llamar al servicio y serializar JSON con Gson. Códigos HTTP semánticos: 200, 201, 400 (ValidacionException), 404 (NoEncontradoException), 500 (DaoException). Los errores devuelven JSON {"error": "mensaje"}. Nada de lógica de negocio ni SQL en los servlets.
```
**Resultado de Junie:** creó `VideojuegoServlet`, `DesarrolladoraServlet` y `GeneroServlet` con las rutas y operaciones indicadas, y una clase base `ApiServlet` que centraliza la serialización JSON con Gson y traduce las excepciones a códigos HTTP (400 validación, 404 no encontrado, 500 error de datos) con respuestas JSON de error. Añadió `AplicacionListener`, que crea el DataSource al arrancar la app y conecta DAO y servicios.
**Observaciones:**
- Junie añadió por su cuenta un test (`ServletApiTest`) que no había pedido en este paso.
- No pudo ejecutar `mvn test` (Maven no disponible en su entorno); solo pasó `git diff --check`. Compilo y ejecuto los tests desde IntelliJ.
- El listener es una pieza que yo no había especificado; decisión de diseño de Junie para compartir el pool entre capas.
  **Cambios manuales míos:** ninguno
  **Commit:** `feat: servlets REST con manejo centralizado de errores` 
### Paso 6 – Frontend
**Prompt:**
```
Crea el frontend en src/main/webapp: index.html semántico, css/estilos.css responsive (mobile first) y js/app.js que consuma la API con fetch. Debe permitir listar, crear, editar y borrar videojuegos (elegir desarrolladora en un <select> y géneros con checkboxes). Muestra un indicador de "Cargando...", mensajes de error claros si falla la validación o el servidor, y confirmación antes de borrar. Prohibido mezclar Java o SQL en las vistas.
```
**Resultado de Junie:** creó el frontend en `src/main/webapp`: `index.html` semántico con formulario (selector de desarrolladora y casillas de géneros), `css/estilos.css` con diseño mobile-first y accesible, y `js/app.js`, que consume la API con fetch para listar, crear, editar y borrar videojuegos, con indicador de carga, mensajes de error comprensibles y confirmación antes de borrar.
**Observaciones:**
- Junie solo pudo comprobar la sintaxis del JS (`node --check`). No se probó contra Tomcat ni la base de datos; la prueba real la haré con Docker Compose.
- Detectó una clase `src/main/java/org/example/Main.java` (plantilla de IntelliJ, no es de Junie) y la dejó intacta.
  **Cambios manuales míos:** ninguno
  **Commit:** `feat: frontend HTML/CSS/JS con CRUD de videojuegos` 
### Paso 7 – Batería de tests
**Prompt:**
```
Crea tests con JUnit 5: (1) tests de integración del VideojuegoDAO contra H2 en modo MySQL (crear el esquema en @BeforeEach) cubriendo crear, leer, actualizar, borrar, buscar un id inexistente, y violación de integridad (desarrolladora inexistente); (2) tests unitarios del VideojuegoServicio con Mockito cubriendo el camino feliz y los casos límite: título vacío, año fuera de rango, precio negativo, desarrolladora inexistente, id no encontrado.
```
**Resultado de Junie:** en vez de crear tests nuevos, amplió los existentes. En `DaoJdbcTest` (H2 en modo MySQL) añadió comprobaciones de persistencia de datos, búsqueda de un ID inexistente y rechazo de una desarrolladora inválida sin que se persista nada. En `ServicioTest` (Mockito) separó los casos de año fuera de rango y precio negativo, y mantiene los de camino feliz, título vacío, desarrolladora inexistente e ID no encontrado.
**Observaciones:**
- Los cambios son pequeños (+19 y +9 líneas) porque ya había una base de tests de los pasos anteriores.
- No pudo ejecutar `mvn test` (Maven no disponible en su entorno). Ejecuto los tests yo desde IntelliJ.
  **Cambios manuales míos:** ninguno
  **Commit:** `test: ampliar cobertura de DAO y servicio con casos límite` 
### Paso 8 – Docker y Docker Compose
**Prompt:**
```
Crea un Dockerfile multi-stage (stage 1: maven con eclipse-temurin 17 que compila el .war; stage 2: tomcat:10.1-jdk17 que lo copia como ROOT.war), un docker-compose.yml y un .env.example. Servicios: db (mysql:8 con volumen nombrado db_data, init.sql montado en /docker-entrypoint-initdb.d/, healthcheck con mysqladmin ping), app (depends_on db con condition: service_healthy, variables desde .env) y adminer. Los tres en una red interna propia. Solo se exponen al host los puertos de app y adminer. Ninguna credencial hardcodeada.
```
**Resultado de Junie:** creó un `Dockerfile` multi-stage (compila el WAR con Maven y Java 17 y lo instala como `ROOT.war` en Tomcat 10.1), un `docker-compose.yml` con tres servicios (MySQL, aplicación y Adminer) en una red aislada, con volumen persistente, `init.sql` montado para la inicialización, healthcheck en la base de datos y `depends_on` con condición saludable, y un `.env.example` con las variables requeridas y los puertos configurables. Solo se publican los puertos de la aplicación y de Adminer.
**Observaciones:**
- Junie no pudo validar con `docker compose config` ni construir las imágenes porque no hay Docker en su entorno. Lo único que contrastó fue el nombre del WAR y las variables de conexión con `pom.xml` y `ConexionBD.java`.
- La validación real la hago yo levantando el entorno en mi equipo.
  **Cambios manuales míos:** La app no arranca con Docke. He añadido allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC a la URL JDBC en ConexionBD
  **Commit:** `feat: Dockerfile multi-stage, docker-compose y .env.example` 
### Paso 9 – Documentación (README)
**Prompt:**
```
Genera el README.md profesional: descripción, diagrama entidad-relación (Mermaid), esquema de arquitectura de contenedores (Mermaid), explicación de variables de entorno, pasos para copiar .env.example a .env y ejecutar docker compose up --build -d, puertos, credenciales de prueba, cómo ejecutar los tests y un manual breve de uso.
```
**Resultado de Junie:** creó `README.md` en español con diagramas Mermaid (arquitectura por capas, modelo entidad-relación y servicios/redes de Docker), explicación de las variables del `.env` y de las credenciales de desarrollo, puertos expuestos, datos de prueba, comandos de Docker Compose, endpoints REST, instrucciones para ejecutar los tests y un manual de uso de la interfaz.
**Observaciones:**
- Junie contrastó el contenido con `docker-compose.yml`, `.env.example`, `db/init.sql`, los servlets, la interfaz y la estructura de tests.
- No ejecutó tests porque el cambio es solo documental.
  **Commit:** `docs: README con arquitectura, modelo de datos y guía de despliegue` 
### Paso 10 – Auditoría final
**Prompt:**
```
Audita el proyecto completo, SIN MODIFICAR NINGÚN ARCHIVO: busca SQL concatenado, recursos sin cerrar, credenciales en código, lógica de negocio en los servlets, entidades expuestas sin DTO y casos de test que falten. Devuélveme solo una lista de problemas ordenada por gravedad, indicando archivo y línea.
```
**Hallazgos de Junie:**
1. **Alta (solo si se despliega fuera de desarrollo local):** la API permite crear, actualizar y borrar videojuegos sin autenticación, y Compose publica la aplicación y Adminer en puertos del host. Los valores de ejemplo de `.env.example` tampoco deben usarse en un despliegue expuesto.
2. **Media:** un POST o PUT con `"generos": null` hace que `VideojuegoDTO.getGeneros()` lance `NullPointerException`, y `ApiServlet` no la captura, por lo que no se garantiza la respuesta JSON de error prevista.
3. **Baja (cobertura incompleta):** faltan pruebas del servicio para actualizar y eliminar con ID inexistente, y falta probar el rollback si la actualización de un videojuego falla al guardar sus géneros.
4. **Baja (escalabilidad):** para comprobar si una desarrolladora tiene videojuegos, el servicio carga todos los videojuegos y el DAO consulta los géneros por separado para cada uno.

**Decisiones mías:**
- **Corregido (2 y 3):** el hallazgo 2 es un fallo real que devolvía un error sin controlar, y el 3 mejora la cobertura de casos límite. Se corrigen en el Paso 11.
- **No corregido (1):** la autenticación queda fuera del alcance de la práctica, que se ejecuta en local. Se documenta como limitación conocida en el README.
- **No corregido (4):** es un problema de escalabilidad que no afecta al volumen de datos de la práctica. Se documenta como mejora futura en el README.

**Observaciones:** comprobé en GitHub Desktop que Junie no modificó ningún archivo durante la auditoría.
**Commit:** `docs: registro de la auditoría final con Junie`

### Paso 11 – Correcciones tras la auditoría
**Prompt:**
```
Corrige dos hallazgos de la auditoría sin tocar nada más. (1) Si VideojuegoDTO recibe "generos": null en un POST o PUT, no debe lanzar NullPointerException: trata null como lista vacía o lanza ValidacionException, y haz que ApiServlet capture cualquier excepción inesperada y responda 500 con JSON {"error": "mensaje"}. (2) Añade tests: en ServicioTest, actualizar y eliminar un videojuego con ID inexistente deben lanzar NoEncontradoException, y un POST con generos null debe dar el resultado definido; en DaoJdbcTest, un test que compruebe el rollback cuando la actualización de un videojuego falla al guardar sus géneros (por ejemplo, con un id de género inexistente) y que los datos originales no cambian.
```
**Resultado de Junie:**
- `VideojuegoDTO` devuelve una lista vacía si `generos` es `null`, incluso cuando Gson asigna el valor directamente.
- `ApiServlet` responde 500 con JSON de error para excepciones inesperadas, conservando el manejo específico de los errores conocidos (400 y 404).
- Añadió tests en `ServicioTest`, `DaoJdbcTest` y `ServletApiTest`: creación con géneros nulos, actualización y eliminación con ID inexistente, error inesperado en la API y rollback íntegro al fallar la actualización de géneros.

**Observaciones:**
- Entre las dos opciones que le di para el caso `null` (lista vacía o `ValidacionException`), Junie eligió tratarlo como lista vacía.
- No pudo ejecutar `mvn test` (Maven no disponible en su entorno); los ejecuté yo desde IntelliJ.

**Cambios manuales míos:** ninguno.
**Commit:** `fix: manejar generos null y ampliar tests de casos límite` 

ENLACE AL GITHUB:https://github.com/MarcosLeonLezcano22/Entrega1-MarcosLeon