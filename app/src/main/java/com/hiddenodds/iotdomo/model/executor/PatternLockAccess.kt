package com.hiddenodds.iotdomo.model.executor

import android.content.SharedPreferences
import com.hiddenodds.iotdomo.App
import com.hiddenodds.iotdomo.tool.Constants
import com.hiddenodds.iotdomo.tool.PreferenceHelperApp
import com.hiddenodds.iotdomo.tool.PreferenceHelperApp.get
import com.hiddenodds.iotdomo.tool.PreferenceHelperApp.set
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternLockAccess @Inject constructor() {
    private val context = App.appComponent.context()
    private var prefs: SharedPreferences? = null

    init {
        try {
            prefs = PreferenceHelperApp.customPrefs(context,
                    Constants.PREFERENCE_IOTDOMO)

        }catch (ie: IllegalStateException){
            val patternKey: String? = prefs?.get(Constants.PATTERN_KEY, "")
        }
    }

    fun clearPatternKey(){
        prefs?.set(Constants.PATTERN_KEY, "")
        prefs?.set(Constants.REGISTER_PATTERN, false)
    }

    fun setPatternKey(pattern: String){
        prefs?.set(Constants.PATTERN_KEY, pattern)
        prefs?.set(Constants.REGISTER_PATTERN, true)
    }

    fun isExistPattern(): Boolean{
        return !prefs?.get(Constants.PATTERN_KEY, "").isNullOrEmpty()
    }

    fun compare(pattern: String): Boolean{
        val patterKey = prefs?.get(Constants.PATTERN_KEY, "")
        return !patterKey.isNullOrEmpty() && patterKey == pattern
    }

    fun setFaceSize(size: Int){
        prefs?.set("key_faceSize", size)
    }

    fun getFaceSize(): Int?{
        return prefs?.get("key_faceSize", 140)
    }

}