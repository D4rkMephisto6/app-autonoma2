package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.port.EventosSesion

/** Dirección por defecto: el PC visto desde el emulador de Android. */
const val URL_PHARMASOFT_EMULADOR = "http://10.0.2.2:8080/api/v1/"

fun crearHttpClient(
    motor: HttpClientEngine,
    almacen: AlmacenSeguro,
    urlBase: String = URL_PHARMASOFT_EMULADOR,
    esDepuracion: Boolean = true,
    eventosSesion: EventosSesion? = null
): HttpClient = HttpClient(motor) {

    // Mantenemos true como pide el profesor para que EjecutarLlamada / Repositorio atrape 401 y 403
    expectSuccess = true

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        })
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15000
        connectTimeoutMillis = 10000
    }

    defaultRequest {
        url(urlBase)
        contentType(ContentType.Application.Json)
    }

    install(Auth) {
        bearer {
            loadTokens {
                almacen.leerToken()?.let { BearerTokens(it, "") }
            }
            sendWithoutRequest { peticion ->
                !esLogin(peticion.url.encodedPath)
            }
        }
    }

    install(Logging) {
        // En la versión de entrega no se escribe nada de la red en el registro.
        level = if (esDepuracion) LogLevel.HEADERS else LogLevel.NONE
        sanitizeHeader { nombre -> nombre == HttpHeaders.Authorization }
    }
}.also { cliente ->
    // Sesión 14 (fila 7): un 401 fuera del login significa que el token venció o
    // fue rechazado. Se avisa una sola vez aquí y SesionViewModel vuelve al login.
    // Se usa HttpSend y no HttpResponseValidator porque, con expectSuccess = true,
    // el validador por defecto de Ktor lanza la excepción antes que el nuestro.
    // HttpSend ve la respuesta antes de cualquier validación, sea cual sea expectSuccess.
    // El 403 no se publica: la sesión sigue siendo válida, solo falta el privilegio.
    cliente.plugin(HttpSend).intercept { peticion ->
        val llamada = execute(peticion)
        val ruta = llamada.request.url.encodedPath
        if (llamada.response.status == HttpStatusCode.Unauthorized && !esLogin(ruta)) {
            eventosSesion?.notificar(ErrorSesion.SesionExpirada)
        }
        llamada
    }
}

private fun esLogin(ruta: String): Boolean = ruta.endsWith("/auth/login")
