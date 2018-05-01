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
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import javax.inject.Inject


class LockActivity: AppCompatActivity() {
    var flagConnect = false
    @BindView(R.id.pb_connect)
    @JvmField var pbConnect: ProgressBar? = null
    @BindView(R.id.ib_lock)
    @JvmField var ibLock: ImageButton? = null

    @OnClick(R.id.ib_lock)
    fun lock(){

        if (board.ifSelectedScannedDevice()){
            if (!flagConnect){
                flagConnect = true
                board.connect()
            }

            buttonManagement()
            digitalWrite(12)
        }

    }

    val Activity.app: App
        get() = application as App

    private val component by lazy { app.getAppComponent()
            .plus(ActivityModule(this))}

    @Inject
    lateinit var board: IBoard

    private var disposable: CompositeDisposable = CompositeDisposable()
    private var itemMenuConnect: MenuItem? = null

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
                        if (m.length > 10){
                            toast(m)
                        }

                        pbConnect!!.visibility = View.INVISIBLE

                    }
                })
        val list = board.observableDevicesNames.map { l -> l }
        disposable.add(list.observeOn(AndroidSchedulers.mainThread())
                .subscribe { l ->
                    kotlin.run {
                        pbConnect!!.visibility = View.INVISIBLE
                        if (l.isNotEmpty()){
                            board.selectDevice(0)

                            alert(l[0]) {
                                title = "Alert"
                                yesButton {

                                    toast("Yess!!!")
                                }
                            }.show()

                        }else{
                            toast("Not devices.")
                        }

                    }
                })

    }

    override fun onResume() {
        super.onResume()

        flagConnect = false
        scanDevices()
        ibLock!!.visibility = View.VISIBLE
        ibLock!!.tag = 0
        ibLock!!.setImageDrawable(getDrawable(R.drawable.lock_android))
        ibLock!!.isEnabled = true

    }

    private fun buttonManagement(){
        when(ibLock!!.tag){
            0 -> {
                ibLock!!.tag = 1
                ibLock!!.isEnabled = false
                ibLock!!.setImageDrawable(getDrawable(R.drawable.unlock_android))
            }
            1 -> {
                ibLock!!.tag = 0
                ibLock!!.isEnabled = true
                ibLock!!.setImageDrawable(getDrawable(R.drawable.lock_android))

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.lock, menu)
        itemMenuConnect = menu!!.getItem(0)
        return true
    }

    private fun scanDevices(){
        pbConnect!!.visibility = View.VISIBLE

        async(CommonPool){
            board.scanDevices()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if (id == R.id.action_phone_link){
            if (board.ifSelectedScannedDevice()){
                toast(getString(R.string.lbl_board_initialized))
            }else{
                scanDevices()
            }

        }

        if (id == R.id.action_add_face){
            val intent = Intent(this, InitRecognitionActivity::class.java)
            intent.putExtra("training", "0")
            startActivity(intent)
            this.finish()

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.navigate<MainActivity>()
        this.finish()
    }

    override fun onDestroy() {
        board.disconnect()
        disposable.dispose()
        super.onDestroy()
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

            delay(5000)
            board.digitalWrite(led)
            runOnUiThread({
                buttonManagement()
            })
        }
    }

}