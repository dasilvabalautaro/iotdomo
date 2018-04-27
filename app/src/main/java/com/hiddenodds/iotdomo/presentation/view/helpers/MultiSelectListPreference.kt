package com.hiddenodds.iotdomo.presentation.view.helpers

import android.content.Context
import android.util.AttributeSet


class MultiSelectListPreference: android.preference.MultiSelectListPreference {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet):
            super(context, attributeSet)

    override fun getSummary(): CharSequence {
        var result = ""
        for (s in this.values) {
            result += "$s "
        }
        return result
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)

        summary = summary
    }
}