package com.hiddenodds.iotdomo

import android.app.Application
import android.content.res.Configuration
import com.hiddenodds.iotdomo.dagger.AppComponent
import com.hiddenodds.iotdomo.dagger.AppModule
import com.hiddenodds.iotdomo.dagger.DaggerAppComponent
import com.hiddenodds.iotdomo.tool.LocaleUtils
import java.util.*
import javax.inject.Inject

class App: Application() {

    @Inject
    lateinit var localeUtils: LocaleUtils

    companion object{
        lateinit var appComponent: AppComponent
    }

    private val component: AppComponent by lazy {
        DaggerAppComponent
                .builder()
                .appModule(AppModule(this))
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
        localeUtils.setLocale(Locale("es"))
        localeUtils.updateConfiguration(this,
                baseContext.resources.configuration)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        localeUtils.updateConfiguration(this, newConfig!!)
    }

    fun getAppComponent(): AppComponent{
        appComponent = component
        return appComponent
    }

}