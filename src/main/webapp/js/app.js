const formulario = document.querySelector("#formulario-videojuego");
const listaVideojuegos = document.querySelector("#lista-videojuegos");
const listaGeneros = document.querySelector("#lista-generos");
const selectorDesarrolladora = document.querySelector("#desarrolladora");
const estadoCarga = document.querySelector("#estado-carga");
const mensajeCatalogo = document.querySelector("#mensaje-catalogo");
const mensajeFormulario = document.querySelector("#mensaje-formulario");
const contadorVideojuegos = document.querySelector("#contador-videojuegos");
const botonRecargar = document.querySelector("#btn-recargar");
const botonGuardar = document.querySelector("#btn-guardar");
const botonCancelar = document.querySelector("#btn-cancelar");
const campoTitulo = document.querySelector("#titulo");
const campoAnio = document.querySelector("#anio");
const campoPrecio = document.querySelector("#precio");

const URL_VIDEOJUEGOS = "api/videojuegos";
const URL_DESARROLLADORAS = "api/desarrolladoras";
const URL_GENEROS = "api/generos";
const anioActual = new Date().getFullYear();

let videojuegos = [];
let desarrolladoras = [];
let generos = [];
let videojuegoEnEdicion = null;

campoAnio.max = String(anioActual);

class ErrorApi extends Error {
    constructor(mensaje, estado) {
        super(mensaje);
        this.estado = estado;
    }
}

async function solicitar(url, opciones = {}) {
    let respuesta;

    try {
        respuesta = await fetch(url, opciones);
    } catch (error) {
        throw new Error("No se pudo conectar con el servidor. Comprueba que la aplicación esté disponible e inténtalo de nuevo.");
    }

    const tipoContenido = respuesta.headers.get("content-type") || "";
    let contenido = null;

    if (respuesta.status !== 204) {
        if (tipoContenido.includes("application/json")) {
            try {
                contenido = await respuesta.json();
            } catch (error) {
                throw new ErrorApi("El servidor devolvió una respuesta JSON no válida.", respuesta.status);
            }
        } else {
            contenido = await respuesta.text();
        }
    }

    if (!respuesta.ok) {
        const mensaje = contenido && typeof contenido === "object" && contenido.error
            ? contenido.error
            : `La solicitud no se pudo completar (HTTP ${respuesta.status}).`;
        throw new ErrorApi(mensaje, respuesta.status);
    }

    return contenido;
}

function mensajeDeError(error, operacion) {
    if (error instanceof ErrorApi) {
        if (error.estado === 400) {
            return `Revisa los datos: ${error.message}`;
        }
        if (error.estado === 404) {
            return `No se encontró el elemento solicitado: ${error.message}`;
        }
        if (error.estado >= 500) {
            return `Error del servidor al ${operacion} (HTTP ${error.estado}): ${error.message}`;
        }
        return `${error.message}`;
    }

    return error.message || `Se produjo un error inesperado al ${operacion}.`;
}

function mostrarError(elemento, mensaje) {
    elemento.textContent = mensaje;
    elemento.hidden = false;
}

function ocultarError(elemento) {
    elemento.textContent = "";
    elemento.hidden = true;
}

function crearElemento(etiqueta, clase, texto) {
    const elemento = document.createElement(etiqueta);
    if (clase) {
        elemento.className = clase;
    }
    if (texto !== undefined) {
        elemento.textContent = texto;
    }
    return elemento;
}

function mostrarCarga(visible) {
    estadoCarga.hidden = !visible;
    estadoCarga.setAttribute("aria-busy", String(visible));
    botonRecargar.disabled = visible;
}

function renderizarDesarrolladoras() {
    selectorDesarrolladora.replaceChildren();
    const opcionInicial = document.createElement("option");
    opcionInicial.value = "";
    opcionInicial.textContent = desarrolladoras.length
        ? "Selecciona una desarrolladora"
        : "No hay desarrolladoras disponibles";
    selectorDesarrolladora.append(opcionInicial);

    desarrolladoras.forEach((desarrolladora) => {
        const opcion = document.createElement("option");
        opcion.value = String(desarrolladora.id);
        opcion.textContent = desarrolladora.nombre;
        selectorDesarrolladora.append(opcion);
    });
}

function renderizarGeneros() {
    listaGeneros.replaceChildren();

    if (!generos.length) {
        listaGeneros.append(crearElemento("p", "texto-secundario", "No hay géneros disponibles."));
        return;
    }

    generos.forEach((genero) => {
        const etiqueta = crearElemento("label", "opcion-genero");
        const casilla = document.createElement("input");
        casilla.type = "checkbox";
        casilla.name = "generos";
        casilla.value = String(genero.id);
        casilla.dataset.generoId = String(genero.id);
        const nombre = crearElemento("span", "", genero.nombre);
        etiqueta.append(casilla, nombre);
        listaGeneros.append(etiqueta);
    });
}

function renderizarVideojuegos() {
    listaVideojuegos.replaceChildren();
    contadorVideojuegos.textContent = videojuegos.length === 1
        ? "1 videojuego en el catálogo"
        : `${videojuegos.length} videojuegos en el catálogo`;

    if (!videojuegos.length) {
        listaVideojuegos.append(crearElemento(
            "p",
            "vacio",
            "Todavía no hay videojuegos en el catálogo. Añade el primero con el formulario."
        ));
        return;
    }

    const desarrolladorasPorId = new Map(desarrolladoras.map((desarrolladora) => [
        Number(desarrolladora.id), desarrolladora.nombre
    ]));
    const formatoPrecio = new Intl.NumberFormat("es-ES", {
        style: "currency",
        currency: "EUR"
    });

    videojuegos.forEach((videojuego) => {
        const tarjeta = crearElemento("article", "tarjeta-videojuego");
        const cabecera = crearElemento("div", "tarjeta-cabecera");
        const titulo = crearElemento("h3", "", videojuego.titulo);
        const precio = crearElemento("p", "precio-videojuego", formatoPrecio.format(Number(videojuego.precio)));
        cabecera.append(titulo, precio);

        const nombreDesarrolladora = desarrolladorasPorId.get(Number(videojuego.desarrolladoraId))
            || "Desarrolladora no disponible";
        const metadatos = crearElemento(
            "p",
            "metadatos-videojuego",
            `${videojuego.anio} · ${nombreDesarrolladora}`
        );

        const contenedorGeneros = crearElemento("div", "generos-videojuego");
        (Array.isArray(videojuego.generos) ? videojuego.generos : []).forEach((genero) => {
            contenedorGeneros.append(crearElemento("span", "genero-etiqueta", genero.nombre));
        });

        const acciones = crearElemento("div", "acciones-tarjeta");
        const botonEditar = crearElemento("button", "boton boton-secundario boton-tarjeta", "Editar");
        botonEditar.type = "button";
        botonEditar.dataset.accion = "editar";
        botonEditar.dataset.id = String(videojuego.id);

        const botonEliminar = crearElemento("button", "boton boton-secundario boton-tarjeta boton-eliminar", "Eliminar");
        botonEliminar.type = "button";
        botonEliminar.dataset.accion = "eliminar";
        botonEliminar.dataset.id = String(videojuego.id);

        acciones.append(botonEditar, botonEliminar);
        tarjeta.append(cabecera, metadatos, contenedorGeneros, acciones);
        listaVideojuegos.append(tarjeta);
    });
}

async function cargarCatalogo() {
    mostrarCarga(true);
    ocultarError(mensajeCatalogo);

    try {
        const [listaJuegos, listaDesarrolladoras, listaGenerosApi] = await Promise.all([
            solicitar(URL_VIDEOJUEGOS),
            solicitar(URL_DESARROLLADORAS),
            solicitar(URL_GENEROS)
        ]);

        if (!Array.isArray(listaJuegos) || !Array.isArray(listaDesarrolladoras) || !Array.isArray(listaGenerosApi)) {
            throw new Error("La API devolvió datos con un formato inesperado.");
        }

        videojuegos = listaJuegos;
        desarrolladoras = listaDesarrolladoras;
        generos = listaGenerosApi;
        renderizarDesarrolladoras();
        renderizarGeneros();
        renderizarVideojuegos();
    } catch (error) {
        mostrarError(mensajeCatalogo, mensajeDeError(error, "cargar el catálogo"));
    } finally {
        mostrarCarga(false);
    }
}

function validarFormulario() {
    const titulo = campoTitulo.value.trim();
    const anio = Number(campoAnio.value);
    const precio = Number(campoPrecio.value);

    if (!titulo) {
        return "El título del videojuego es obligatorio.";
    }
    if (titulo.length > 100) {
        return "El título no puede superar los 100 caracteres.";
    }
    if (!campoAnio.value || !Number.isInteger(anio) || anio < 1970 || anio > anioActual) {
        return `El año debe ser un número entre 1970 y ${anioActual}.`;
    }
    if (!campoPrecio.value || !Number.isFinite(precio) || precio < 0) {
        return "El precio debe ser un número mayor o igual a cero.";
    }
    if (!selectorDesarrolladora.value) {
        return "Selecciona una desarrolladora.";
    }
    if (!desarrolladoras.length) {
        return "No hay desarrolladoras disponibles para asignar al videojuego.";
    }

    return null;
}

function obtenerGenerosSeleccionados() {
    const idsSeleccionados = new Set(
        Array.from(formulario.querySelectorAll('input[name="generos"]:checked'))
            .map((casilla) => Number(casilla.value))
    );
    return generos
        .filter((genero) => idsSeleccionados.has(Number(genero.id)))
        .map((genero) => ({ id: Number(genero.id), nombre: genero.nombre }));
}

function limpiarFormulario() {
    formulario.reset();
    videojuegoEnEdicion = null;
    botonGuardar.textContent = "Guardar videojuego";
    botonCancelar.hidden = true;
    document.querySelector("#titulo-formulario").textContent = "Añadir videojuego";
    ocultarError(mensajeFormulario);
}

function prepararEdicion(videojuego) {
    videojuegoEnEdicion = Number(videojuego.id);
    campoTitulo.value = videojuego.titulo || "";
    campoAnio.value = String(videojuego.anio ?? "");
    campoPrecio.value = String(videojuego.precio ?? "");
    selectorDesarrolladora.value = String(videojuego.desarrolladoraId ?? "");

    const idsGeneros = new Set((videojuego.generos || []).map((genero) => Number(genero.id)));
    formulario.querySelectorAll('input[name="generos"]').forEach((casilla) => {
        casilla.checked = idsGeneros.has(Number(casilla.value));
    });

    botonGuardar.textContent = "Guardar cambios";
    botonCancelar.hidden = false;
    document.querySelector("#titulo-formulario").textContent = "Editar videojuego";
    ocultarError(mensajeFormulario);
    campoTitulo.focus();
}

async function guardarVideojuego(evento) {
    evento.preventDefault();
    ocultarError(mensajeFormulario);

    const errorValidacion = validarFormulario();
    if (errorValidacion) {
        mostrarError(mensajeFormulario, errorValidacion);
        return;
    }

    const videojuego = {
        titulo: campoTitulo.value.trim(),
        anio: Number(campoAnio.value),
        precio: Number(campoPrecio.value),
        desarrolladoraId: Number(selectorDesarrolladora.value),
        generos: obtenerGenerosSeleccionados()
    };
    const estaEditando = videojuegoEnEdicion !== null;
    const url = estaEditando ? `${URL_VIDEOJUEGOS}/${videojuegoEnEdicion}` : URL_VIDEOJUEGOS;
    const opciones = {
        method: estaEditando ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(videojuego)
    };

    botonGuardar.disabled = true;
    botonGuardar.textContent = estaEditando ? "Guardando cambios..." : "Guardando...";

    try {
        await solicitar(url, opciones);
        limpiarFormulario();
        await cargarCatalogo();
    } catch (error) {
        mostrarError(mensajeFormulario, mensajeDeError(error, estaEditando ? "actualizar el videojuego" : "guardar el videojuego"));
    } finally {
        botonGuardar.disabled = false;
        if (videojuegoEnEdicion !== null) {
            botonGuardar.textContent = "Guardar cambios";
        } else {
            botonGuardar.textContent = "Guardar videojuego";
        }
    }
}

async function eliminarVideojuego(id) {
    const videojuego = videojuegos.find((elemento) => Number(elemento.id) === id);
    if (!videojuego || !window.confirm(`¿Seguro que quieres borrar «${videojuego.titulo}»? Esta acción no se puede deshacer.`)) {
        return;
    }

    ocultarError(mensajeCatalogo);
    try {
        await solicitar(`${URL_VIDEOJUEGOS}/${id}`, { method: "DELETE" });
        await cargarCatalogo();
    } catch (error) {
        mostrarError(mensajeCatalogo, mensajeDeError(error, "borrar el videojuego"));
    }
}

listaVideojuegos.addEventListener("click", (evento) => {
    const boton = evento.target.closest("button[data-accion]");
    if (!boton) {
        return;
    }

    const id = Number(boton.dataset.id);
    const videojuego = videojuegos.find((elemento) => Number(elemento.id) === id);
    if (!videojuego) {
        return;
    }

    if (boton.dataset.accion === "editar") {
        prepararEdicion(videojuego);
    } else if (boton.dataset.accion === "eliminar") {
        eliminarVideojuego(id);
    }
});

formulario.addEventListener("submit", guardarVideojuego);
botonCancelar.addEventListener("click", limpiarFormulario);
botonRecargar.addEventListener("click", cargarCatalogo);

cargarCatalogo();