package com.hiddenodds.iotdomo.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import butterknife.ButterKnife
import com.hiddenodds.iotdomo.App
import com.hiddenodds.iotdomo.R
import com.hiddenodds.iotdomo.dagger.ActivityModule
import com.hiddenodds.iotdomo.model.interfaces.IBoard
import com.hiddenodds.iotdomo.presentation.component.AuthenticateAdapter
import com.hiddenodds.iotdomo.tool.ManageImages
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.yesButton
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private var disposable: CompositeDisposable = CompositeDisposable()
    var image: ImageView? = null
    var observableImage: Subject<ImageView> = PublishSubject.create()
    var itemMenuImage: MenuItem? = null

    val Activity.app: App
        get() = application as App

    private val component by lazy { app.getAppComponent()
            .plus(ActivityModule(this))}

    @Inject
    lateinit var board: IBoard
    @Inject
    lateinit var manageImages: ManageImages

    init {
        observableImage
                .subscribe { image }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        component.inject(this)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        board.initSDK()
        image = ImageView(this)
        val pager: ViewPager = ButterKnife.findById(this, R.id.vp_access)
        //pager.offscreenPageLimit = 1
        pager.adapter = AuthenticateAdapter(supportFragmentManager, pager)
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (position == 3){
                    pager.adapter.notifyDataSetChanged()
                }
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        itemMenuImage = menu!!.getItem(1)
        enabledMenuItemImage(false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if (id == R.id.action_phone_link){
            indeterminateProgressDialog("Process connect, please wait...").show()
            launch{
                board.scanDevices()
            }

        }
        if (id == R.id.action_face){
            dialogImage()
        }


        return super.onOptionsItemSelected(item)
    }
    override fun onStart() {
        super.onStart()
        val message = board.observableMessage.map { m -> m }
        disposable.add(message.observeOn(AndroidSchedulers.mainThread())
                .subscribe { m ->
                    kotlin.run {
                        toast(m)
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
                                yesButton { toast("Yess!!!") }
                            }.show()

                        }

                    }
                })



    }

    override fun onBackPressed() {
        super.onBackPressed()
        board.disconnect()
        finish()
        android.os.Process.killProcess(android.os.Process.myPid())

    }

    fun digitalWrite(led: Int){
        board.digitalWrite(led)
        launch {
            delay(2000)
            board.digitalWrite(led)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == manageImages.GALLERY_IMAGE_REQUEST &&
                resultCode == Activity.RESULT_OK && data != null) {
            manageImages.setControlImage(data.data, image!!)
            this.observableImage.onNext(this.image!!)
            //manageImages.uploadImage(data.data)

        } else if (requestCode == manageImages.CAMERA_IMAGE_REQUEST &&
                resultCode == Activity.RESULT_OK) {
            val photoUri = FileProvider.getUriForFile(this,
                    applicationContext.packageName + ".provider",
                    manageImages.getCameraFile())
            manageImages.setControlImage(photoUri, image!!)
            this.observableImage.onNext(this.image!!)
            //manageImages.uploadImage(photoUri)
        }

    }

    fun getResultTensor(): Observable<String> {
        return this.manageImages
                .observableResult.map { s -> s }
    }

    fun enabledMenuItemImage(enabled: Boolean){
        itemMenuImage!!.isVisible = enabled
    }

    fun dialogImage(){
        alert(R.string.dialog_select_prompt) {
            title = "Alerta"
            positiveButton(R.string.dialog_select_gallery) {
                manageImages.startGalleryChooser() }
            negativeButton(R.string.dialog_select_camera) {
                manageImages.startCamera() }
            neutralPressed("Cancelar"){}
        }.show()

    }

    inline fun <reified T : Activity> Activity.navigate() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

    private fun Activity.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
