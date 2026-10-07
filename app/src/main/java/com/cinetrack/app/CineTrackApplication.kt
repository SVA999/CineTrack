package com.cinetrack.app

import android.app.Application
import com.cinetrack.app.di.AppContainer

class CineTrackApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
