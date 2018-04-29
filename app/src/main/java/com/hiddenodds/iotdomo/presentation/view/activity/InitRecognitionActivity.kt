package com.hiddenodds.iotdomo.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.hiddenodds.iotdomo.R
import org.jetbrains.anko.toast


class InitRecognitionActivity: AppCompatActivity() {
    var name = ""
    @BindView(R.id.et_input)
    @JvmField var etInput: EditText? = null
    @BindView(R.id.bt_detect)
    @JvmField var btDetect: Button? = null
    @BindView(R.id.bt_training)
    @JvmField var btTraining: Button? = null
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
    @OnClick(R.id.bt_training)
    fun training(){
        navigate<TrainingActivity>()
        finish()
    }
    @OnClick(R.id.bt_test)
    fun test(){
        val intent = Intent(this, RecognitionActivity::class.java)
        intent.putExtra("guide", "1")
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_face)
        ButterKnife.bind(this)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.face, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if (id == R.id.action_config_face){

        }

        return super.onOptionsItemSelected(item)
    }

    private inline fun <reified T : Activity> Activity.navigate() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }
}