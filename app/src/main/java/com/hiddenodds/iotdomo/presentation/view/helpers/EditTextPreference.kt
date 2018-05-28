package com.hiddenodds.iotdomo.presentation.view.helpers

import android.content.Context
import android.util.AttributeSet


class EditTextPreference: android.preference.EditTextPreference {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet):
            super(context, attributeSet)

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)
        summary = summary
    }

    override fun getSummary(): CharSequence {
        return this.text
    }
}