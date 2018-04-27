package com.hiddenodds.iotdomo.presentation.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.hiddenodds.iotdomo.R

class FibonacciFragment: AuthenticateFragment() {
    @BindView(R.id.tv_values)
    @JvmField var tvValues: TextView? = null
    @BindView(R.id.et_input)
    @JvmField var etInput: EditText? = null
    @OnClick(R.id.bt_save)
    fun saveValue(){
        if (!etInput!!.text.isEmpty()){
            try {
                val lastValue = fibonacci(getSizeSeries())
                val value = etInput!!.text.toString().toInt()
                if (lastValue == value){
                    val newSeries = tvValues!!.text.toString() + " " + value.toString()
                    tvValues!!.text = newSeries
                    patternLockAccess.setFibonacciKey(newSeries)
                    //digitalWrite()
                    flagAccess = true
                    goLock(this)
                }else{
                    context.toast("Wrong value, repeat.")
                }
                etInput!!.setText("")
            }catch (ex: Exception){
                println(ex.message)
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(com.hiddenodds.iotdomo.R.layout.view_fibonacci,
                container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)
    }

    override fun onStart() {
        super.onStart()
        if (patternLockAccess.isExistFibonacci()){
            val fibo = patternLockAccess.getFibonacciKey()
            if (fibo!!.length > 20){
                tvValues!!.text = "1"
            }else{
                tvValues!!.text = fibo
            }
        }else{
            tvValues!!.text = "1"
        }
    }

    private fun getSizeSeries(): Int{
        val array = tvValues!!.text.split(" ")
        return array.size
    }

    private fun fibonacci(n: Int): Int{
        val array: ArrayList<Int> = ArrayList()

        for (i in 0..n){
            if ((i - 1) < 0 || (i - 2) < 0){
                array.add(1)
            }else{
                val sum = array[i - 1] + array[i - 2]
                array.add(sum)
            }
        }

        return array[array.size - 1]
    }

    private fun Context.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}