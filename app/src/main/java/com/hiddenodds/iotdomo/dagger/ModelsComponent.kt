package com.hiddenodds.iotdomo.dagger

import com.hiddenodds.iotdomo.presentation.view.fragment.AuthenticateFragment
import dagger.Subcomponent

@Subcomponent(modules = [ModelsModule::class])
interface ModelsComponent {
/*
    fun inject(faceFragment: FaceFragment)
    fun inject(patternLockFragment: PatternLockFragment)
*/
    fun inject(authenticateFragment: AuthenticateFragment)
}