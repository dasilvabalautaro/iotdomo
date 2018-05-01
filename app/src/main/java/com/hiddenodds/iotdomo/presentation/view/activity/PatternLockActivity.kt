package com.hiddenodds.iotdomo.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.hiddenodds.iotdomo.App
import com.hiddenodds.iotdomo.R
import com.hiddenodds.iotdomo.dagger.ActivityModule
import com.hiddenodds.iotdomo.model.executor.PatternLockAccess
import org.jetbrains.anko.alert
import javax.inject.Inject

class PatternLockActivity: AppCompatActivity(){
    val Activity.app: App
        get() = application as App
    private val component by lazy { app.
            getAppComponent().plus(ActivityModule(this))}

    @Inject
    lateinit var patternLockAccess: PatternLockAccess

    @BindView(R.id.pl_access)
    @JvmField var plAccess: PatternLockView? = null
    @BindView(R.id.bt_clear)
    @JvmField var btClear: Button? = null
    @OnClick(R.id.bt_clear)
    fun clear(){
        when(btClear!!.tag){
            0 -> {
                navigate<MainActivity>()
                finish()
            }
            1 ->{
                btClear!!.tag = 0
                btClear!!.text = getString(R.string.lbl_clear)
                btNext!!.visibility = View.INVISIBLE
                patternLockAccess.clearPatternKey()
                this.patter = ""
                tvTitle!!.text = getString(R.string.lbl_pattern_lock)
                plAccess!!.clearPattern()
            }
        }

    }
    @BindView(R.id.tv_title)
    @JvmField var tvTitle: TextView? = null
    @BindView(R.id.bt_next)
    @JvmField var btNext: Button? = null
    @OnClick(R.id.bt_next)
    fun next(){
        navigate<LockActivity>()
        finish()
    }

    var patter: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_pattern_lock)
        component.inject(this)
        ButterKnife.bind(this)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        plAccess!!.addPatternLockListener(mPatternLockViewListener)
        btClear!!.tag = 0

        if (!patternLockAccess.isExistPattern()){
            btClear!!.visibility = View.VISIBLE
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
                    runOnUiThread({
                        navigate<LockActivity>()
                        finish()
                    })

                }else{
                    plAccess!!.clearPattern()
                    toast(getString(R.string.lbl_wrong_pattern))
                }
            }else if (patter.isEmpty()){
                patter = PatternLockUtils.patternToString(plAccess, pattern)
                if (patter.length > 3){
                    btClear!!.tag = 1
                    btClear!!.text = getString(R.string.lbl_repeat)
                    tvTitle!!.text = getString(R.string.lbl_pattern_confirm)
                    plAccess!!.clearPattern()
                }else{
                    plAccess!!.clearPattern()
                    patter = ""
                    toast(getString(R.string.lbl_size_pattern))
                }
            }else{
                val p = PatternLockUtils.patternToString(plAccess, pattern)
                if (p == patter){
                    patternLockAccess.setPatternKey(patter)
                    plAccess!!.clearPattern()
                    runOnUiThread({
                        navigate<LockActivity>()
                        finish()
                    })

                }else{
                    plAccess!!.clearPattern()
                    toast(getString(R.string.lbl_wrong_pattern))
                }
            }

            println("Pattern complete: " +
                    PatternLockUtils.patternToString(plAccess, pattern))

        }
        override fun onCleared() {
            println("Pattern has been cleared")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.navigate<MainActivity>()
        this.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pattern, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if (id == R.id.action_clear){
            alert("El patrón de seguridad será eliminado.") {
                title = "Alerta"
                positiveButton("Confirmar") {
                    patternLockAccess.clearPatternKey()
                    plAccess!!.clearPattern()
                    btClear!!.visibility = View.VISIBLE
                    btClear!!.tag = 0
                    patter = ""
                }
                negativeButton("Cancelar") {}
            }.show()
        }

        return super.onOptionsItemSelected(item)
    }

    private inline fun <reified T : Activity> Activity.navigate() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

    private fun Activity.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

}