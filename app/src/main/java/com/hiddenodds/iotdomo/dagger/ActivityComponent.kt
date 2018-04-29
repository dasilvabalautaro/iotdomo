package com.hiddenodds.iotdomo.dagger

import com.hiddenodds.iotdomo.presentation.view.activity.LockActivity
import com.hiddenodds.iotdomo.presentation.view.activity.MainActivity
import com.hiddenodds.iotdomo.presentation.view.activity.PatternLockActivity
import dagger.Subcomponent

@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(patternLockActivity: PatternLockActivity)
    fun inject(lockActivity: LockActivity)
}