package com.hiddenodds.iotdomo.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.hiddenodds.iotdomo.App
import com.hiddenodds.iotdomo.R
import com.hiddenodds.iotdomo.dagger.ActivityModule
import com.hiddenodds.iotdomo.model.executor.Training
import com.hiddenodds.iotdomo.tool.Constants
import com.hiddenodds.iotdomo.tool.PreferenceHelperApp
import com.hiddenodds.iotdomo.tool.PreferenceHelperApp.set
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.toast
import javax.inject.Inject


class InitRecognitionActivity: AppCompatActivity() {
    var name = ""
    @BindView(R.id.pb_training)
    @JvmField var pbTraining: ProgressBar? = null
    @BindView(R.id.et_input)
    @JvmField var etInput: EditText? = null
    @BindView(R.id.bt_detect)
    @JvmField var btDetect: Button? = null
    @BindView(R.id.bt_test)
    @JvmField var btTest: Button? = null
    @OnClick(R.id.bt_detect)
    fun detect(){
        if (etInput!!.text.isNotEmpty()){
            name = etInput!!.text.toString()
            val intent = Intent(this, EnrollActivity::class.java)
            intent.putExtra("Name", name)
            startActivity(intent)
            finish()
        }else{
            toast("Su nombre por favor.")
        }

    }

    @OnClick(R.id.bt_test)
    fun test(){
        val intent = Intent(this, RecognitionActivity::class.java)
        intent.putExtra("before", "2")
        startActivity(intent)
        this.finish()
    }

    val Activity.app: App
        get() = application as App

    private val component by lazy { app.getAppComponent()
            .plus(ActivityModule(this))}

    @Inject
    lateinit var training: Training

    private var isTraining = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_face)
        component.inject(this)
        ButterKnife.bind(this)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        val intentData = intent
        isTraining = intentData.getStringExtra("training")

    }

    override fun onResume() {
        super.onResume()
        btTest!!.isEnabled = false
        if (isTraining == "1"){
            executeTraining()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.navigate<MainActivity>()
        this.finish()
    }


    private fun executeTraining(){
        pbTraining!!.visibility = View.VISIBLE

        async {
            val result = training.training()
            if (result){
                runOnUiThread({
                    pbTraining!!.visibility = View.INVISIBLE
                    val prefs = PreferenceHelperApp.customPrefs(applicationContext,
                            Constants.PREFERENCE_IOTDOMO)
                    prefs[Constants.REGISTER_FACE] = true
                    toast("Clasificación exitosa.")
                    btTest!!.isEnabled = true
                })
            }else{
                runOnUiThread({
                    pbTraining!!.visibility = View.INVISIBLE
                    toast("Error en la clasificación.")
                })
            }
        }
        toast("Construyendo el clasificador de imágenes. \nUn momento por favor.")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.face, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if (id == R.id.action_config_face){
            this.navigate<SettingsActivity>()
            this.finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private inline fun <reified T : Activity> Activity.navigate() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }
}