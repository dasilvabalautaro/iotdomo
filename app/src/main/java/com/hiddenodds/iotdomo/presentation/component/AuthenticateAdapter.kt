package com.hiddenodds.iotdomo.presentation.component

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import com.hiddenodds.iotdomo.presentation.view.fragment.*


class AuthenticateAdapter(manager: FragmentManager,
                          pager: ViewPager):
        FragmentStatePagerAdapter(manager), AuthenticateFragment.Callback {

    var pager: ViewPager? = null
    var fibonacci: AuthenticateFragment? = null
    var patternLock: AuthenticateFragment? = null
    var face: AuthenticateFragment? = null
    var presentation: PresentationFragment? = null
    var lock: LockFragment? = null

    init {
        this.pager = pager
    }

    override fun getItem(position: Int): Fragment {
        when(position){
            0 ->{

                if (presentation == null) presentation = PresentationFragment()
                presentation!!.callback = this
                return presentation!!

            }

            1 ->{

                if (fibonacci == null) fibonacci = FibonacciFragment()
                fibonacci!!.callback = this
                return fibonacci!!

            }
            2 ->{
                if (patternLock == null) patternLock = PatternLockFragment()
                patternLock!!.callback = this
                return patternLock!!
            }
            3 ->{
                if (face == null) face = FaceFragment()
                face!!.callback = this
                return face!!
            }
            else ->{
                if (lock == null) lock = LockFragment()
                lock!!.callback = this

            }
        }
        return lock!!

    }

    override fun getCount(): Int {
        return 5
    }


    override fun remove(fragment: AuthenticateFragment) {
        if (lock != fragment){
            if (lock != null){
                lock!!.onResume()
            }

            pager!!.setCurrentItem(4, true)
        }
    }

   override fun getItemPosition(frg: Any?): Int {
        return POSITION_NONE
    }

}