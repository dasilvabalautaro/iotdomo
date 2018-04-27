package com.hiddenodds.iotdomo.presentation.view.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.hiddenodds.iotdomo.R

class ConfigurationFragment : Fragment() {
    @BindView(R.id.tv_message)
    @JvmField var tvMessage: TextView? = null

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(com.hiddenodds.iotdomo.R.layout.view_help,
                container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)

        tvMessage!!.text = "1.- Please, enable bluetooth. \n2.- Push phone link button, on menu bar and wait. \n3.- On message of alert push OK."
    }

}