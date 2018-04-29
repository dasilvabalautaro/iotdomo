package com.hiddenodds.iotdomo.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceView
import butterknife.BindView
import butterknife.ButterKnife
import ch.zhaw.facerecognitionlibrary.Helpers.CustomCameraView
import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper
import ch.zhaw.facerecognitionlibrary.Helpers.MatName
import ch.zhaw.facerecognitionlibrary.Helpers.MatOperation
import ch.zhaw.facerecognitionlibrary.PreProcessor.PreProcessorFactory
import com.hiddenodds.iotdomo.R
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import java.io.File
import java.util.*

class EnrollActivity: AppCompatActivity(),
        CameraBridgeViewBase.CvCameraViewListener2 {
    companion object {
        init {
            OpenCVLoader.initDebug()
        }
    }

    @BindView(R.id.cv_enroll)
    @JvmField var enrollView: CustomCameraView? = null
    private var preProcessor: PreProcessorFactory? = null
    private var fileHelper: FileHelper? = null
    private var numberOfPictures = 10
    private var exposure_compensation = 50
    private var front_camera = true
    private val MANUALLY = 1
    private val TIME = 0
    private var timerDiff: Long = 500
    private var lastTime: Long = 0
    private var method = 0
    private var nameImage = ""
    private var total = 0
    private var night_portrait: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_enroll)
        ButterKnife.bind(this)
        val intentData = intent
        nameImage = intentData.getStringExtra("Name")
        lastTime = Date().time
        fileHelper = FileHelper()

        PreferenceManager.setDefaultValues(this,
                ch.zhaw.facerecognitionlibrary.R.xml.preferences, false)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        front_camera = sharedPref.getBoolean("key_front_camera", true)
        night_portrait = sharedPref.getBoolean("key_night_portrait", false)
        exposure_compensation = Integer.valueOf(sharedPref
                .getString("key_exposure_compensation", "20")!!)

        timerDiff = Integer.valueOf(sharedPref.getString("key_timerDiff",
                "500")!!).toLong()
        numberOfPictures = Integer.valueOf(sharedPref
                .getString("key_numberOfPictures", "100")!!)

        if (front_camera){
            enrollView!!.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT)
        }else{
            enrollView!!.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK)
        }

        enrollView!!.visibility = SurfaceView.VISIBLE
        enrollView!!.setCvCameraViewListener(this)
        val maxCameraViewWidth = Integer.parseInt(sharedPref
                .getString("key_maximum_camera_view_width", "640")!!)
        val maxCameraViewHeight = Integer.parseInt(sharedPref
                .getString("key_maximum_camera_view_height", "480")!!)
        enrollView!!.setMaxFrameSize(maxCameraViewWidth, maxCameraViewHeight)

    }

    override fun onResume() {
        super.onResume()
        preProcessor = PreProcessorFactory(this)
        enrollView!!.enableView()

    }

    override fun onPause() {
        super.onPause()
        if (enrollView != null){
            enrollView!!.disableView()
        }
    }

    private inline fun <reified T : Activity> Activity.navigate() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }


    override fun onCameraViewStarted(width: Int, height: Int) {
        if (night_portrait){
            enrollView!!.setNightPortrait()
        }
        if (exposure_compensation != 50 &&
                0 <= exposure_compensation && exposure_compensation <= 100)
            enrollView!!.setExposure(exposure_compensation)
    }

    override fun onCameraViewStopped() {

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        val imgRgba: Mat = inputFrame!!.rgba()
        val imgCopy = Mat()
        val time = Date().time

        imgRgba.copyTo(imgCopy)
        if (front_camera){
            Core.flip(imgRgba, imgRgba, 1)
        }

        if((method == MANUALLY) || (method == TIME) &&
                (lastTime + timerDiff < time)){
            lastTime = time
            val images: List<Mat>? = preProcessor!!.getCroppedImage(imgCopy)
            if (images != null && images.size == 1) {
                val img: Mat = images[0]
                var faces: Array<Rect>? = preProcessor!!.facesForRecognition
                if (faces != null && faces.size == 1) {
                    faces = MatOperation.rotateFaces(imgRgba, faces,
                            preProcessor!!.angleForRecognition)
                    val matName = MatName(nameImage + "_" + total, img)
                    val wholeFolderPath = FileHelper.TRAINING_PATH + nameImage
                    println("Path Images : $wholeFolderPath")
                    File(wholeFolderPath).mkdirs()
                    fileHelper!!.saveMatToImage(matName, "$wholeFolderPath/")
                    for (i in 0 until faces!!.size){
                        MatOperation.drawRectangleAndLabelOnPreview(imgRgba,
                                faces[i], total.toString(), front_camera)
                    }
                    total++
                    if (total == numberOfPictures){
                        this.navigate<InitRecognitionActivity>()
                        this.finish()
                    }
                }
            }
        }

        return imgRgba
    }

}