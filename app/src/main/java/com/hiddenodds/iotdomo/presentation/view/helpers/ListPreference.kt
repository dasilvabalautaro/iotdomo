package com.hiddenodds.iotdomo.presentation.view.helpers

import android.content.Context
import android.util.AttributeSet

class ListPreference: android.preference.ListPreference {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet):
            super(context, attributeSet)

    override fun getSummary(): CharSequence {
        return this.value
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)

        summary = summary
    }
}