package com.hiddenodds.iotdomo.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import com.hiddenodds.iotdomo.App
import com.hiddenodds.iotdomo.R
import com.hiddenodds.iotdomo.dagger.ActivityModule
import com.hiddenodds.iotdomo.model.interfaces.IBoard
import com.hiddenodds.iotdomo.tool.Constants
import com.hiddenodds.iotdomo.tool.ManageImages
import com.hiddenodds.iotdomo.tool.PreferenceHelperApp
import com.hiddenodds.iotdomo.tool.PreferenceHelperApp.get
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.jetbrains.anko.alert
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    var image: ImageView? = null
    var observableImage: Subject<ImageView> = PublishSubject.create()
    var itemMenuFace: MenuItem? = null
    var itemMenuPattern: MenuItem? = null
    var itemMenuRegister: MenuItem? = null

    val Activity.app: App
        get() = application as App

    private val component by lazy { app.getAppComponent()
            .plus(ActivityModule(this))}

    @Inject
    lateinit var manageImages: ManageImages
    @Inject
    lateinit var board: IBoard

    init {
        observableImage
                .subscribe { image }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        component.inject(this)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        image = ImageView(this)

    }

    private fun defineMenu(): Boolean{
        try {
            val prefs = PreferenceHelperApp.customPrefs(this,
                    Constants.PREFERENCE_IOTDOMO)
            val registerFace: Boolean? = prefs[Constants.REGISTER_FACE, false]
            val registerPattern: Boolean? = prefs[Constants.REGISTER_PATTERN, false]

            return ((registerFace != null && registerFace) ||
                    (registerPattern != null && registerPattern))

        }catch (ie: IllegalStateException){
            println(ie.message)
        }

        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        itemMenuFace = menu!!.getItem(1)
        itemMenuPattern = menu.getItem(2)
        itemMenuRegister = menu.getItem(0)

        if (defineMenu()){
            itemMenuFace!!.isVisible = true
            itemMenuPattern!!.isVisible = true
            itemMenuRegister!!.isVisible = false
        }else{
            itemMenuFace!!.isVisible = false
            itemMenuPattern!!.isVisible = false
            itemMenuRegister!!.isVisible = true
        }

        return true
    }

    private fun launchOptions(){
            this.alert(R.string.lbl_options_register) {
            title = "Bienvenido"
            positiveButton(R.string.lbl_patter_lock) {
                navigate<PatternLockActivity>()
                finish()
            }
            negativeButton(R.string.lbl_detect_face) {
                val intent = Intent(applicationContext, InitRecognitionActivity::class.java)
                intent.putExtra("training", "0")
                startActivity(intent)
                finish()
            }
            neutralPressed(R.string.lbl_clear){}

        }.show()
    }

    override fun onResume() {
        super.onResume()

        if (!manageImages.permissionCamera()){
            println("Solicitude")
        }
        if (!board.permissionLocation()){
            println("Solicitude")
        }
    }

    override fun onDestroy() {
        board.disconnect()
        super.onDestroy()

    }

    private fun optionAccessFace(){
        try {
            val prefs = PreferenceHelperApp.customPrefs(this,
                    Constants.PREFERENCE_IOTDOMO)
            val registerFace: Boolean? = prefs[Constants.REGISTER_FACE, false]
            if (registerFace != null && registerFace){
                val intent = Intent(this, RecognitionActivity::class.java)
                intent.putExtra("before", "1")
                startActivity(intent)
                this.finish()
            }else{
                val intent = Intent(applicationContext, InitRecognitionActivity::class.java)
                intent.putExtra("training", "0")
                startActivity(intent)
                finish()
            }

        }catch (ie: IllegalStateException){
            println(ie.message)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if (id == R.id.action_register){
            launchOptions()
        }

        if (id == R.id.action_access_face){
            optionAccessFace()
        }

        if (id == R.id.action_access_pattern){
            this.navigate<PatternLockActivity>()
            this.finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        android.os.Process.killProcess(android.os.Process.myPid())

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == manageImages.GALLERY_IMAGE_REQUEST &&
                resultCode == Activity.RESULT_OK && data != null) {
            manageImages.setControlImage(data.data, image!!)
            this.observableImage.onNext(this.image!!)


        } else if (requestCode == manageImages.CAMERA_IMAGE_REQUEST &&
                resultCode == Activity.RESULT_OK) {
            val photoUri = FileProvider.getUriForFile(this,
                    applicationContext.packageName + ".provider",
                    manageImages.getCameraFile())
            manageImages.setControlImage(photoUri, image!!)
            this.observableImage.onNext(this.image!!)

        }
    }

    private fun dialogImage(){
        alert(R.string.dialog_select_prompt) {
            title = "Alerta"
            positiveButton(R.string.dialog_select_gallery) {
                manageImages.startGalleryChooser() }
            negativeButton(R.string.dialog_select_camera) {
                manageImages.startCamera() }
            neutralPressed("Cancelar"){}
        }.show()

    }

    private inline fun <reified T : Activity> Activity.navigate() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

    private fun Activity.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
