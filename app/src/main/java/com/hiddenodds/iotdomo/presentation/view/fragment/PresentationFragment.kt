package com.hiddenodds.iotdomo.presentation.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class PresentationFragment: AuthenticateFragment() {

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(com.hiddenodds.iotdomo.R.layout.view_presentation,
                container, false)
    }
}