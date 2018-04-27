package com.hiddenodds.iotdomo.dagger

import com.hiddenodds.iotdomo.model.executor.FaceRecognition
import com.hiddenodds.iotdomo.model.executor.PatternLockAccess
import dagger.Module
import dagger.Provides

@Module
class ModelsModule {

    @Provides
    fun provideFaceRecognition(): FaceRecognition {
        return FaceRecognition()
    }

    @Provides
    fun providePatternLockAccess(): PatternLockAccess{
        return PatternLockAccess()
    }
}