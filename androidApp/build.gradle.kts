import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

// Sesión 14 · Paso 3: las credenciales de firma viven en keystore.properties
// (raíz del proyecto, ignorado por Git). Se lee solo si existe: quien clona el
// repositorio sin ese archivo puede abrir el proyecto y compilar debug.
val firma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
val hayFirmaDeEntrega = firma.getProperty("storeFile") != null

// Dirección de PharmaSoft. El emulador llega al PC por 10.0.2.2; un teléfono real
// necesita la IP del PC en la red local. Se cambia en gradle.properties
// (pharmasoft.url) sin tocar el código Kotlin.
val urlPharmaSoft = (findProperty("pharmasoft.url") as String?) ?: "http://10.0.2.2:8080/api/v1/"

android {
    namespace = "pe.edu.upeu.pharmamobil"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    signingConfigs {
        if (hayFirmaDeEntrega) {
            create("release") {
                storeFile = file(firma.getProperty("storeFile"))
                storePassword = firma.getProperty("storePassword")
                keyAlias = firma.getProperty("keyAlias")
                keyPassword = firma.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        // Se conserva pe.edu.upeu.pharmamobil: es el paquete registrado en
        // google-services.json (Firebase). Cambiarlo rompe la compilación.
        applicationId = "pe.edu.upeu.pharmamobil"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "PHARMASOFT_URL", "\"$urlPharmaSoft\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            // Sin keystore.properties el release se genera sin firmar en vez de
            // fallar; apksigner lo delata y no sirve como entregable.
            if (hayFirmaDeEntrega) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
