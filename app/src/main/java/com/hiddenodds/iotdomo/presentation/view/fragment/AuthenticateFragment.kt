package com.hiddenodds.iotdomo.presentation.view.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import com.hiddenodds.iotdomo.App
import com.hiddenodds.iotdomo.dagger.ModelsModule
import com.hiddenodds.iotdomo.model.executor.FaceRecognition
import com.hiddenodds.iotdomo.model.executor.PatternLockAccess
import com.hiddenodds.iotdomo.presentation.view.activity.MainActivity
import javax.inject.Inject

abstract class AuthenticateFragment: Fragment() {
    companion object Factory{
        var flagAccess = false
    }

    val Fragment.app: App
        get() = activity.application as App

    private val component by lazy { app.
            getAppComponent().plus(ModelsModule())}

    @Inject
    lateinit var faceRecognition: FaceRecognition
    @Inject
    lateinit var patternLockAccess: PatternLockAccess


    interface Callback{
        fun remove(fragment: AuthenticateFragment)
    }


    var callback: Callback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
    }

    fun digitalWrite(led: Int){
        (activity as MainActivity).digitalWrite(led)
    }

    fun goLock(fragment: AuthenticateFragment){
        if (callback != null){
            callback!!.remove(fragment)
        }
    }


}