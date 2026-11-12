# Paso 1 — Cierre de la ficha de integración y alcance congelado (Sesión 14)

Revisión de la tabla de huecos de la Sesión 13 ([PASO-01-TABLA-HUECOS-S13.md](PASO-01-TABLA-HUECOS-S13.md)).
Cada fila queda en una de tres columnas: **cerrado**, **se cerró hoy** o **declarado como limitación**.

| N.º | Momento | Estado al cierre | Dónde está en el código |
| --- | --- | --- | --- |
| 1 | Primera apertura, sin sesión | Cerrado en S13 | `InicioScreen` + `GestorPermisos` |
| 2 | Credenciales incorrectas | Cerrado en S10 | `SesionViewModel.iniciarSesion`, `mapearErrorSesion` |
| 3 | Listado con base vacía | Cerrado en S13 | `EstadoVacio`, `BarraEstadoSincronizacion` |
| 4 | Creación en modo avión | Cerrado en S13 | `ProductoLocal.guardarPendiente`, columnas `intentos` / `ultimoError` |
| 5 | Sincronización al recuperar señal | Cerrado en S13 | `ProductoRepositorioOffline.sincronizar` |
| 6 | Error de validación del servidor | Cerrado en S13 | `ProductoApi.enviar` → `ResultadoEnvio.Permanente` |
| 7 | Token vencido a mitad de un formulario | **Se cerró hoy (S14)** | `crearHttpClient` (interceptor de `HttpSend`) → `EventosSesion` → `SesionViewModel.escucharRechazosDelServidor`. Lo escrito ya estaba a salvo en SQLite. |
| 8 | Cierre de sesión | **Se cerró hoy (S14)** | `CerrarSesionUseCase` borra el token **y** la caché sincronizada (`ProductoLocal.borrarSincronizados`); conserva lo pendiente. |

## Alcance congelado

A partir de este punto no entra funcionalidad nueva, ni cambios de librerías ni de esquema de base.
Los cambios de la Sesión 14 son de empaquetado (firma, versión, R8, red) y el cierre de las filas 7 y 8,
que estaban comprometidas para esta sesión.

## Declarado como limitación

| Limitación | Motivo | Qué haría falta para cerrarla |
| --- | --- | --- |
| Sin archivo de iOS ni `.ipa` | El equipo de desarrollo es Windows; Xcode solo corre en macOS. | Un Mac con Xcode y una cuenta de Apple. Con cuenta gratuita: archivo en Xcode + captura del organizador + video en un iPhone (sin `.ipa`). |
| Aviso de R8: *"An error occurred when parsing kotlin metadata"* | AGP 9.0.1 trae un R8 anterior a Kotlin 2.4.10. Es un aviso, no un error. | Subir AGP cuando publique un R8 compatible. Mientras tanto se verificó en el emulador que el APK minificado inicia sesión, lista y registra (ver matriz). |
| Debug y release no compilan en un mismo comando de Gradle (D-02) | Conflicto entre tareas de los plugins de Firebase con AGP 9 | Actualizar los plugins `google-services` y `crashlytics`; mientras tanto, compilar en comandos separados. |
| La IP del PC se escribe en dos lugares | `network_security_config.xml` no acepta variables de Gradle. | Generar el XML desde Gradle; se descartó para no agregar complejidad en la última sesión. |

## Descartado (trabajo futuro, nunca estuvo en el alcance)

- Módulo de pedidos (la pantalla muestra "en construcción").
- Limpieza de los pendientes de otro usuario en un equipo compartido: hoy se conservan para no perder datos.
