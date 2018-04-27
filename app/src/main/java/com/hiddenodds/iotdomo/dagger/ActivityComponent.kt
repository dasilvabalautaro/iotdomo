package com.hiddenodds.iotdomo.dagger

import com.hiddenodds.iotdomo.presentation.view.activity.MainActivity
import dagger.Subcomponent

@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {
    fun inject(mainActivity: MainActivity)

}