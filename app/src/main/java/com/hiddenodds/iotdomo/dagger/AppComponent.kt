package com.hiddenodds.iotdomo.dagger

import android.content.Context
import com.hiddenodds.iotdomo.App
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(app: App)
    fun context(): Context
    fun plus(activityModule: ActivityModule): ActivityComponent
    fun plus(modelsModule: ModelsModule): ModelsComponent
}