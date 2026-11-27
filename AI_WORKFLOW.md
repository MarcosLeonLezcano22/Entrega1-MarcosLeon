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
(pegar aquí el Prompt 1 exacto antes de enviarlo)
```
**Resultado de Junie:** (qué generó)
**Cambios manuales míos:** (qué corregí y por qué)
**Commit:** (mensaje del commit)

## Errores corregidos a mano
| Paso | Error | Causa | Solución |
|------|-------|-------|----------|
|      |       |       |          |

## Auditoría final
(Se rellena al terminar con el resultado del prompt de auditoría y lo que corregí)