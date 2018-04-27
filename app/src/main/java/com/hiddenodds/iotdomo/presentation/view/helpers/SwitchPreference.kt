package com.hiddenodds.iotdomo.presentation.view.helpers

import android.content.Context
import android.util.AttributeSet


class SwitchPreference: android.preference.SwitchPreference {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet):
            super(context, attributeSet)

    override fun getSummary(): CharSequence {
        return if (this.isChecked) {
            this.switchTextOn
        } else {
            this.switchTextOff
        }
    }
}