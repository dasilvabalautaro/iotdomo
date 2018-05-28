package com.hiddenodds.iotdomo.dagger

import android.content.Context
import com.hiddenodds.iotdomo.App
import com.hiddenodds.iotdomo.tool.LocaleUtils
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val app: App) {
    @Provides
    @Singleton
    fun provideApplicationContext(): Context {
        return this.app
    }
    @Provides
    fun provideLocaleConfiguration(): LocaleUtils{
        return LocaleUtils()
    }

}