package pe.edu.upeu.pharmamobil.domain

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.port.EventosSesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pruebas de las filas 7 y 8 de la ficha de integración, cerradas en la Sesión 14.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CierreDeSesionTest {

    private val almacenConToken = object : AlmacenSeguro {
        private var token: String? = "token-de-prueba"
        override fun guardarToken(token: String) { this.token = token }
        override fun leerToken(): String? = token
        override fun borrarToken() { token = null }
    }

    /** Cliente real de la app con un servidor simulado que siempre responde [estado]. */
    private fun clienteQueResponde(estado: HttpStatusCode, eventos: EventosSesion) =
        crearHttpClient(
            motor = MockEngine { respond("", estado) },
            almacen = almacenConToken,
            esDepuracion = false,
            eventosSesion = eventos
        )

    @Test
    fun un401FueraDelLoginAvisaQueLaSesionExpiro() = runTest {
        val eventos = EventosSesion()
        val recibidos = mutableListOf<ErrorSesion>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            eventos.eventos.collect { recibidos += it }
        }
        val cliente = clienteQueResponde(HttpStatusCode.Unauthorized, eventos)

        runCatching { cliente.get("productos") }

        assertEquals(listOf<ErrorSesion>(ErrorSesion.SesionExpirada), recibidos)
    }

    @Test
    fun un401EnElLoginNoEsSesionExpiradaSinoCredencialInvalida() = runTest {
        val eventos = EventosSesion()
        val recibidos = mutableListOf<ErrorSesion>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            eventos.eventos.collect { recibidos += it }
        }
        val cliente = clienteQueResponde(HttpStatusCode.Unauthorized, eventos)

        runCatching { cliente.post("auth/login") }

        assertTrue(recibidos.isEmpty())
    }

    @Test
    fun un403NoCierraLaSesion() = runTest {
        val eventos = EventosSesion()
        val recibidos = mutableListOf<ErrorSesion>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            eventos.eventos.collect { recibidos += it }
        }
        val cliente = clienteQueResponde(HttpStatusCode.Forbidden, eventos)

        runCatching { cliente.get("productos") }

        assertTrue(recibidos.isEmpty())
    }

    @Test
    fun cerrarSesionBorraElTokenYLoSincronizadoPeroConservaLoPendiente() = runTest {
        val local = ProductoLocal(PharmaDb(crearDriverEnMemoria()))
        local.reemplazarSincronizados(listOf(Producto(id = 10L, nombre = "Paracetamol", precio = 3.5, stock = 20)))
        val pendiente = local.guardarPendiente(Producto(id = 0L, nombre = "Ibuprofeno", precio = 15.5, stock = 10))

        val auth = object : AuthRepository {
            override suspend fun iniciarSesion(credenciales: Credenciales): Sesion = error("no se usa")
            override suspend fun sesionGuardada(): Sesion? = null
            override suspend fun cerrarSesion() = almacenConToken.borrarToken()
        }
        val remoto = ProductoApi(clienteQueResponde(HttpStatusCode.OK, EventosSesion()))
        val cerrarSesion = CerrarSesionUseCase(auth, ProductoRepositorioOffline(local, remoto))

        cerrarSesion()

        assertNull(almacenConToken.leerToken())
        assertNull(local.porId(10L), "El catálogo del usuario anterior debe borrarse")
        assertNotNull(local.porId(pendiente.id), "Lo creado sin conexión no se pierde")
    }
}
