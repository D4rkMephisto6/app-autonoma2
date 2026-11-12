package pe.edu.upeu.pharmamobil.domain.port

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion

/**
 * Canal único para avisar que el servidor rechazó la sesión (HTTP 401).
 *
 * Sesión 14, fila 7 de la ficha: el cliente HTTP publica aquí el evento desde
 * cualquier petición y SesionViewModel lo escucha para volver al login. Así
 * ninguna pantalla necesita revisar el 401 por su cuenta.
 */
class EventosSesion {

    // extraBufferCapacity = 1 permite emitir con tryEmit desde código que no
    // es suspend (el validador de Ktor) sin bloquear ni perder el aviso.
    private val _eventos = MutableSharedFlow<ErrorSesion>(extraBufferCapacity = 1)
    val eventos: SharedFlow<ErrorSesion> = _eventos.asSharedFlow()

    fun notificar(error: ErrorSesion) {
        _eventos.tryEmit(error)
    }
}
