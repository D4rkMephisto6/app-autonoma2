This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
---

## Sesión 14 · Builds firmados (Guía Práctica N.º 14)

### Datos de la entrega

| Dato | Valor |
| --- | --- |
| `applicationId` | `pe.edu.upeu.pharmamobil` (se conserva: es el paquete registrado en `google-services.json`; cambiarlo rompe Firebase) |
| `versionCode` | `1` |
| `versionName` | `1.0.0` |
| Etiqueta de Git | `v1.0-final` (se pone sobre el commit que se compiló) |
| Archivos | `Equipo<N>_ProyectoFinal_Android.aab` y `Equipo<N>_ProyectoFinal_Android.apk` |

### Firma sin exponer secretos

1. Crea el almacén **fuera** de esta carpeta (una sola vez por equipo):
   ```
   keytool -genkeypair -v -keystore equipo<N>.jks -storetype JKS -keyalg RSA -keysize 2048 -validity 10000 -alias equipo<N>
   ```
2. Copia `keystore.properties.example` como `keystore.properties` (raíz del proyecto) y completa
   `storeFile`, `storePassword`, `keyAlias`, `keyPassword`. En Windows usa `/` en la ruta.
3. `keystore.properties`, `*.jks` y `*.keystore` están en `.gitignore`: nunca se suben.

`androidApp/build.gradle.kts` lee el archivo solo si existe. Sin él, `assembleDebug` funciona para
cualquiera que clone el repositorio y `assembleRelease` genera un APK **sin firmar** (apksigner lo rechaza).

### Generar y verificar

```
gradlew.bat :androidApp:bundleRelease     -> androidApp/build/outputs/bundle/release/androidApp-release.aab
gradlew.bat :androidApp:assembleRelease   -> androidApp/build/outputs/apk/release/androidApp-release.apk
apksigner verify --print-certs androidApp/build/outputs/apk/release/androidApp-release.apk
```

### Dirección de PharmaSoft

`gradle.properties` → `pharmasoft.url`. El emulador usa `http://10.0.2.2:8080/api/v1/`; un teléfono real
necesita la IP del PC (por ejemplo `http://192.168.1.40:8080/api/v1/`) **y** esa misma IP en
`androidApp/src/main/res/xml/network_security_config.xml`, porque Android bloquea HTTP sin cifrar.
También se puede pasar sin editar archivos: `gradlew.bat :androidApp:assembleRelease -Ppharmasoft.url=...`.

### Minificación (R8)

`isMinifyEnabled = true`. Las reglas de `androidApp/proguard-rules.pro` conservan los DTO de
`kotlinx.serialization` y silencian clases de la JVM de escritorio que Ktor referencia. Koin 4 no usa
reflexión (`viewModelOf(::Clase)` es una referencia a constructor), por eso no necesita reglas propias.

Documentos de la sesión: [`docs/PASO-01-FICHA-CIERRE-S14.md`](docs/PASO-01-FICHA-CIERRE-S14.md) y
[`docs/MATRIZ-PRUEBAS-S14.md`](docs/MATRIZ-PRUEBAS-S14.md).
