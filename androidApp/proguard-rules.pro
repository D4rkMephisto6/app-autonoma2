# Reglas de R8 para la versión de entrega (Sesión 14, Paso 5).
# Cada regla responde a un síntoma de la tabla de la guía: solo se conserva
# lo que se resuelve por nombre en tiempo de ejecución.

# Conserva las líneas de los errores para leer los reportes de Crashlytics,
# pero oculta el nombre del archivo fuente.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- kotlinx.serialization (DTO de transporte) ---------------------------
# Síntoma: lista vacía y error de serialización. El cliente HTTP necesita las
# anotaciones y la firma genérica para encontrar el serializador generado.
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-keepclassmembers @kotlinx.serialization.Serializable class pe.edu.upeu.pharmamobil.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class pe.edu.upeu.pharmamobil.data.remote.dto.** { *; }

# --- Ktor ------------------------------------------------------------------
# Ktor referencia clases opcionales de la JVM de escritorio que no existen en
# Android; sin esto R8 detiene la compilación con "Missing class".
-dontwarn java.lang.management.**
-dontwarn org.slf4j.**
-dontwarn io.ktor.util.debug.**
