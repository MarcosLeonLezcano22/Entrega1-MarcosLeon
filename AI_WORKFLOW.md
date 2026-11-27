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

## Errores corregidos a mano
Junie no pudo ejecutar Maven (no está en el PATH); validé el pom.xml desde IntelliJ.

## Auditoría final
(Se rellena al terminar con el resultado del prompt de auditoría y lo que corregí)