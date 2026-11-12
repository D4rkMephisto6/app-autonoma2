package pe.edu.upeu.pharmamobil

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import pe.edu.upeu.pharmamobil.di.PROP_ES_DEPURACION
import pe.edu.upeu.pharmamobil.di.PROP_URL_PHARMASOFT
import pe.edu.upeu.pharmamobil.di.initKoin

/**
 * Arranca Koin una sola vez, antes que cualquier Activity. Se declara en el
 * manifiesto con android:name=".MainApplication".
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            // En la versión de entrega Koin solo registra errores.
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.ERROR)
            androidContext(this@MainApplication)
            // Sesión 14: la URL y el modo depuración salen de BuildConfig, que
            // Gradle genera por tipo de compilación; el módulo shared los lee
            // como propiedades de Koin sin depender del módulo Android.
            properties(
                mapOf(
                    PROP_URL_PHARMASOFT to BuildConfig.PHARMASOFT_URL,
                    PROP_ES_DEPURACION to BuildConfig.DEBUG
                )
            )
        }
    }
}
