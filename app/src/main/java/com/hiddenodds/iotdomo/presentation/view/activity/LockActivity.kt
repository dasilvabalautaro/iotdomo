package com.hiddenodds.iotdomo.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.hiddenodds.iotdomo.App
import com.hiddenodds.iotdomo.R
import com.hiddenodds.iotdomo.dagger.ActivityModule
import com.hiddenodds.iotdomo.model.interfaces.IBoard
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import java.lang.IllegalStateException
import javax.inject.Inject


class LockActivity: AppCompatActivity() {
    @BindView(R.id.pb_connect)
    @JvmField var pbConnect: ProgressBar? = null
    @BindView(R.id.ib_unlock)
    @JvmField var ibUnlock: ImageButton? = null
    @BindView(R.id.ib_lock)
    @JvmField var ibLock: ImageButton? = null
    @OnClick(R.id.ib_unlock)
    fun unLock(){
        digitalWrite(13)
        ibUnlock!!.isEnabled = false
        ibLock!!.isEnabled = true
        ibUnlock!!.setImageDrawable(getDrawable(R.drawable.n_unlock_android))
        ibLock!!.setImageDrawable(getDrawable(R.drawable.lock_android))
    }
    @OnClick(R.id.ib_lock)
    fun lock(){
        digitalWrite(12)
        ibUnlock!!.isEnabled = true
        ibLock!!.isEnabled = false
        ibUnlock!!.setImageDrawable(getDrawable(R.drawable.unlock_android))
        ibLock!!.setImageDrawable(getDrawable(R.drawable.n_lock_android))

    }

    val Activity.app: App
        get() = application as App

    private val component by lazy { app.getAppComponent()
            .plus(ActivityModule(this))}

    @Inject
    lateinit var board: IBoard

    private var disposable: CompositeDisposable = CompositeDisposable()
    private var itemMenuConnect: MenuItem? = null
    private var flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pattern_lock)
        component.inject(this)
        ButterKnife.bind(this)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        board.initSDK()

    }

    override fun onStart() {
        super.onStart()
        val message = board.observableMessage.map { m -> m }
        disposable.add(message.observeOn(AndroidSchedulers.mainThread())
                .subscribe { m ->
                    kotlin.run {
                        toast(m)
                        pbConnect!!.visibility = View.INVISIBLE

                    }
                })
        val list = board.observableDevicesNames.map { l -> l }
        disposable.add(list.observeOn(AndroidSchedulers.mainThread())
                .subscribe { l ->
                    kotlin.run {
                        if (l.isNotEmpty()){
                            board.selectDevice(0)
                            board.connect()
                            alert(l[0]) {
                                title = "Alert"
                                yesButton {
                                    toast("Yess!!!")
                                }
                            }.show()

                        }

                    }
                })

    }

    override fun onResume() {
        super.onResume()
        ibUnlock!!.visibility = View.VISIBLE
        ibLock!!.visibility = View.VISIBLE
        try {
            ibUnlock!!.setImageDrawable(getDrawable(R.drawable.n_lock_android))

        }catch (ie: IllegalStateException){
            println("Error: set image")
        }
        ibUnlock!!.isEnabled = false
        scanDevices()
        flag = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.lock, menu)
        itemMenuConnect = menu!!.getItem(0)
        return true
    }

    private fun scanDevices(){
        pbConnect!!.visibility = View.VISIBLE

        launch(CommonPool){
            board.scanDevices()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if (id == R.id.action_phone_link){
            scanDevices()
        }

        if (id == R.id.action_add_face){
            flag = true
            this.navigate<InitRecognitionActivity>()
            this.finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        board.disconnect()
        if (!flag){
            this.navigate<MainActivity>()
            this.finish()
        }

    }

    private inline fun <reified T : Activity> Activity.navigate() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

    private fun Activity.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun digitalWrite(led: Int){
        board.digitalWrite(led)
        launch {
            delay(2000)
            board.digitalWrite(led)
        }
    }

}