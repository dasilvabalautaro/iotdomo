package com.hiddenodds.iotdomo.presentation.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.hiddenodds.iotdomo.R
import java.lang.IllegalStateException


class LockFragment: AuthenticateFragment() {
    @BindView(R.id.ib_unlock)
    @JvmField var ibUnlock: ImageButton? = null
    @BindView(R.id.ib_lock)
    @JvmField var ibLock: ImageButton? = null
    @OnClick(R.id.ib_unlock)
    fun unLock(){
        digitalWrite(13)
        ibUnlock!!.isEnabled = false
        ibLock!!.isEnabled = true
        ibUnlock!!.setImageDrawable(resources
                .getDrawable(R.drawable.n_unlock_android))
        ibLock!!.setImageDrawable(resources
                .getDrawable(R.drawable.lock_android))
    }
    @OnClick(R.id.ib_lock)
    fun lock(){
        digitalWrite(12)
        ibUnlock!!.isEnabled = true
        ibLock!!.isEnabled = false
        ibUnlock!!.setImageDrawable(resources
                .getDrawable(R.drawable.unlock_android))
        ibLock!!.setImageDrawable(resources
                .getDrawable(R.drawable.n_lock_android))

    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(com.hiddenodds.iotdomo.R.layout.view_lock,
                container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)
    }

    override fun onResume() {
        super.onResume()
        if (flagAccess){
            ibUnlock!!.visibility = View.VISIBLE
            ibLock!!.visibility = View.VISIBLE
            try {
                ibUnlock!!.setImageDrawable(resources
                        .getDrawable(R.drawable.n_lock_android))

            }catch (ie: IllegalStateException){
                println("Error: set image")
            }
            ibUnlock!!.isEnabled = false


        }else{
            ibUnlock!!.visibility = View.INVISIBLE
            ibLock!!.visibility = View.INVISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        flagAccess = false
    }
}