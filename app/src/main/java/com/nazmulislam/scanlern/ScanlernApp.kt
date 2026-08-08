package com.nazmulislam.scanlern

import android.app.Application

class ScanlernApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeHelper.applySavedTheme(this)
    }
}