package com.hiddenodds.iotdomo.presentation.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.hiddenodds.iotdomo.R

class PatternLockFragment: AuthenticateFragment() {
    @BindView(R.id.pl_access)
    @JvmField var plAccess: PatternLockView? = null
    @OnClick(R.id.bt_clear)
    fun clear(){
        patternLockAccess.clearPatternKey()
        this.patter = ""
        tvMessage!!.text = getString(R.string.lbl_pattern_draw)
        plAccess!!.clearPattern()
    }
    @BindView(R.id.tv_message)
    @JvmField var tvMessage: TextView? = null

    var patter: String = ""


    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(com.hiddenodds.iotdomo.R.layout.view_patter_lock,
                container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)
        plAccess!!.addPatternLockListener(mPatternLockViewListener)
        if (patternLockAccess.isExistPattern()){
            tvMessage!!.text = getString(R.string.lbl_pattern_exist)
        }else{
            tvMessage!!.text = getString(R.string.lbl_pattern_not_exist)
        }
    }


    private val mPatternLockViewListener = object : PatternLockViewListener {
        override fun onStarted() {
            println("Pattern drawing started")

        }

        override fun onProgress(progressPattern: List<PatternLockView.Dot>) {
            println("Pattern progress: " +
                    PatternLockUtils.patternToString(plAccess, progressPattern))

        }

        override fun onComplete(pattern: List<PatternLockView.Dot>) {

            if (patternLockAccess.isExistPattern()){
                val getPattern = PatternLockUtils.patternToString(plAccess, pattern)
                if (patternLockAccess.compare(getPattern)){
                    activity.runOnUiThread({
                        flagAccess = true
                        goLock(this@PatternLockFragment)
                    })

                    //digitalWrite()
                }else{
                    plAccess!!.clearPattern()
                    context.toast("Wrong Pattern.")
                }
            }else if (patter.isEmpty()){
                patter = PatternLockUtils.patternToString(plAccess, pattern)
                tvMessage!!.text = getString(R.string.lbl_pattern_confirm)
                plAccess!!.clearPattern()
            }else{
                val p = PatternLockUtils.patternToString(plAccess, pattern)
                if (p == patter){
                    patternLockAccess.setPatternKey(patter)
                    plAccess!!.clearPattern()
                    tvMessage!!.text = getString(R.string.lbl_pattern_valid)
                    activity.runOnUiThread({
                        flagAccess = true
                        goLock(this@PatternLockFragment)
                    })
                    //digitalWrite()
                }
            }

            println("Pattern complete: " +
                    PatternLockUtils.patternToString(plAccess, pattern))

        }
        override fun onCleared() {
            println("Pattern has been cleared")
        }
    }
    private fun Context.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}