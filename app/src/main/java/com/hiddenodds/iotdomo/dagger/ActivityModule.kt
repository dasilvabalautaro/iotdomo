package com.hiddenodds.iotdomo.dagger

import android.support.v7.app.AppCompatActivity
import com.hiddenodds.iotdomo.model.executor.PatternLockAccess
import com.hiddenodds.iotdomo.model.interfaces.IBoard
import com.hiddenodds.iotdomo.tool.ManageImages
import com.hiddenodds.iotdomo.tool.OneSheeld
import com.hiddenodds.iotdomo.tool.PermissionUtils
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private val activity:
                     AppCompatActivity) {

    @Provides
    fun providePermissionUtils(): PermissionUtils {
        return PermissionUtils()
    }

    @Provides
    fun provideOneSheeld(permissionUtils:
                         PermissionUtils): IBoard {
        return OneSheeld(activity, permissionUtils)
    }

    @Provides
    fun provideManageImages(): ManageImages {
        return ManageImages(activity)
    }

    @Provides
    fun providePatternLockAccess(): PatternLockAccess {
        return PatternLockAccess()
    }
}