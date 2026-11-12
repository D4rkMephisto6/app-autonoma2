# Matriz de pruebas — Sesión 14

## Ficha de versión

| Dato | Valor |
| --- | --- |
| versionCode / versionName | 1 / 1.0.0 |
| applicationId | pe.edu.upeu.pharmamobil |
| Titular del certificado | _(copiar de `apksigner verify --print-certs`: línea "Signer #1 certificate DN")_ |
| Huella SHA-256 | _(copiar de la misma salida; nunca la contraseña)_ |

## Recorrido en emulador (versión de entrega minificada)

Ejecutado el 21/11/2026 en el emulador *Medium Phone API 37.0*, con el APK de `assembleRelease`
(R8 activo, firmado con una clave de prueba) contra PharmaSoft local. Capturas en
[`evidencias-s14/`](evidencias-s14/).

| N.º | Paso del recorrido | Resultado | Evidencia |
| --- | --- | --- | --- |
| 1 | Instalación desde el archivo firmado | OK. Para instalar hubo que desinstalar la versión de depuración, porque su firma no coincide (comportamiento esperado de Android). | `01-release-arranca-login.png` |
| 2 | Inicio de sesión incorrecto / correcto | OK. 401 → "Usuario o contraseña incorrectos". 200 → pantalla Inicio. | `02-…`, `03-…` |
| 3 | CRUD: registrar producto | OK. `POST /api/v1/productos` → 201, el producto quedó en Oracle con id 1. | `04-producto-registrado.png` |
| 4 | Operación sin privilegio | No recorrido en emulador. `vendedor` no tiene `CATALOGO_ESCRIBIR` y la app oculta el formulario. | — |
| 5 | Servidor caído: leer | OK. "Sin conexión: mostrando datos guardados" y la lista sigue visible. | `05-backend-caido-datos-locales.png` |
| 6 | Recuperar conexión y sincronizar | Cubierto por `SincronizacionTest` (commonTest). | — |
| 7 | Permiso: conceder / denegar / denegar siempre | Pendiente en dispositivo real. | — |
| 8 | Token rechazado por el servidor (fila 7) | OK. Se reinició el backend con otra clave JWT; "Sincronizar" recibió 401 y la app volvió al login con "Tu sesión ha expirado". | `06-token-rechazado-vuelve-login.png` |

## Dispositivos reales (completar en clase)

| N.º | Paso | Dispositivo A: marca, modelo, Android | Dispositivo B: marca, modelo, Android |
| --- | --- | --- | --- |
| 1 | Instalación desde el archivo, sin cable | | |
| 2 | Login incorrecto y correcto | | |
| 3 | CRUD con un rechazo de validación | | |
| 4 | Operación sin privilegio (usuario `vendedor`) | | |
| 5 | Modo avión: abrir, leer, crear | | |
| 6 | Recuperar conexión y sincronizar | | |
| 7 | Permiso de notificaciones: tres estados | | |
| 8 | Cerrar sesión y reabrir | | |

## Registro de defectos

| Id | Dispositivo y versión | Pasos para reproducir | Esperado | Obtenido | Severidad | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| D-02 | PC Windows 11, Gradle 9.1 | `gradlew clean :androidApp:assembleDebug :androidApp:assembleRelease` en **un solo** comando. | Compilan ambos. | Falla `uploadCrashlyticsMappingFileRelease`: usa `gmpAppId.txt` de `processDebugGoogleServices` sin declarar la dependencia (plugins Firebase 4.4.1 / 3.0.1 con AGP 9). | Baja | Declarado: compilar debug y release en comandos separados (así sí funciona). Se resolvería actualizando los plugins de Firebase, lo cual queda fuera del alcance congelado. |
| D-01 | Emulador API 37 | 1. Instalar la app de depuración. 2. Instalar el APK de entrega encima. | Se actualiza. | `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Baja | Descartado: es la protección de firma de Android. Se desinstala la de depuración antes de la demo. |
