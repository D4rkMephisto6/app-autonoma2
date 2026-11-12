package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class IniciarSesionUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(usuario: String, clave: String): Sesion {
        require(usuario.isNotBlank()) { "El usuario es obligatorio" }
        require(clave.isNotBlank()) { "La contraseña es obligatoria" }
        return repositorio.iniciarSesion(Credenciales(usuario.trim(), clave))
    }
}

class ObtenerSesionGuardadaUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(): Sesion? = repositorio.sesionGuardada()
}

/**
 * Sesión 14 (fila 8 de la ficha): además de borrar el token, borra la copia
 * local del catálogo descargado del servidor, para que el siguiente usuario del
 * equipo no vea datos de la cuenta anterior antes de su primera sincronización.
 * Los registros pendientes de subir se conservan: borrarlos perdería trabajo
 * que el usuario creó sin conexión.
 */
class CerrarSesionUseCase(
    private val repositorio: AuthRepository,
    private val productos: ProductoRepository
) {
    suspend operator fun invoke() {
        repositorio.cerrarSesion()
        productos.limpiarCacheSincronizada()
    }
}
