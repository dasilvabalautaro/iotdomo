package com.hiddenodds.iotdomo.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceView
import android.view.WindowManager
import butterknife.BindView
import butterknife.ButterKnife
import ch.zhaw.facerecognitionlibrary.Helpers.CustomCameraView
import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper
import ch.zhaw.facerecognitionlibrary.Helpers.MatOperation
import ch.zhaw.facerecognitionlibrary.PreProcessor.PreProcessorFactory
import ch.zhaw.facerecognitionlibrary.Recognition.Recognition
import ch.zhaw.facerecognitionlibrary.Recognition.RecognitionFactory
import com.hiddenodds.iotdomo.R
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File


class RecognitionActivity: AppCompatActivity(),
        CameraBridgeViewBase.CvCameraViewListener2 {
    companion object {
        init {
            OpenCVLoader.initDebug()
        }
    }
    @BindView(R.id.cv_recognition)
    @JvmField var cvRecognition: CustomCameraView? = null
    private var preProcessor: PreProcessorFactory? = null
    private var fileHelper: FileHelper? = null
    private var recognition: Recognition? = null
    private var front_camera: Boolean = false
    private var night_portrait: Boolean = false
    private var exposure_compensation: Int = 50
    private var faceSize = 160
    private var flagOut = 0
    private var before = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.view_recognition)
        ButterKnife.bind(this)
        fileHelper = FileHelper()
        val intentData = intent
        before = intentData.getStringExtra("before")
        preProcessor = PreProcessorFactory(applicationContext)
        val folder = File(FileHelper.getFolderPath())
        if (folder.mkdir() || folder.isDirectory) {
            println("New directory for photos created")
        } else {
            println("Photos directory already existing")
        }
        PreferenceManager.setDefaultValues(this,
                ch.zhaw.facerecognitionlibrary.R.xml.preferences, false)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        front_camera = sharedPref.getBoolean("key_front_camera", true)
        night_portrait = sharedPref.getBoolean("key_night_portrait", false)
        faceSize = sharedPref.getString("key_faceSize", "160").toInt()

        exposure_compensation = Integer.valueOf(sharedPref
                .getString("key_exposure_compensation", "20")!!)
        if (front_camera){
            cvRecognition!!.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT)
        }else{
            cvRecognition!!.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK)
        }

        cvRecognition!!.visibility = SurfaceView.VISIBLE
        cvRecognition!!.setCvCameraViewListener(this)
        val maxCameraViewWidth = Integer.parseInt(sharedPref
                .getString("key_maximum_camera_view_width", "640")!!)
        val maxCameraViewHeight = Integer.parseInt(sharedPref
                .getString("key_maximum_camera_view_height", "480")!!)
        cvRecognition!!.setMaxFrameSize(maxCameraViewWidth, maxCameraViewHeight)

    }

    override fun onResume() {
        super.onResume()
        flagOut = 0
        executeDetect()

    }
   private fun executeDetect() =  runBlocking{

        val request = async(CommonPool) {
            val sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(applicationContext)
            val algorithm = sharedPref
                    .getString("key_classification_method",
                            resources.getString(R.string.eigenfaces))
            recognition = RecognitionFactory
                    .getRecognitionAlgorithm(applicationContext,
                            Recognition.RECOGNITION, algorithm)
        }

        request.join()

        cvRecognition!!.enableView()
   }

    override fun onPause() {
        super.onPause()
        if (cvRecognition != null){
            cvRecognition!!.disableView()
        }
    }


    private inline fun <reified T : Activity> Activity.navigate() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }


    override fun onCameraViewStarted(width: Int, height: Int) {
        if (night_portrait){
            cvRecognition!!.setNightPortrait()
        }
        if (exposure_compensation != 50 &&
                0 <= exposure_compensation && exposure_compensation <= 100)
            cvRecognition!!.setExposure(exposure_compensation)
    }

    override fun onCameraViewStopped() {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        when(before){
            "1" -> {
                this.navigate<MainActivity>()
                this.finish()
            }
            "2" -> {
                this.navigate<InitRecognitionActivity>()
                this.finish()
            }
            else -> {
                this.navigate<MainActivity>()
                this.finish()
            }
        }

    }

    override fun onCameraFrame(inputFrame:
                               CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        val imgRgba: Mat = inputFrame!!.rgba()
        val img = Mat()
        imgRgba.copyTo(img)

        val images: List<Mat>? = preProcessor!!
                .getProcessedImage(img,
                        PreProcessorFactory
                                .PreprocessingMode.RECOGNITION)
        var faces: Array<Rect>? = preProcessor!!.facesForRecognition
        if (front_camera) {
            Core.flip(imgRgba, imgRgba, 1)
        }

        return if (images == null || images.isEmpty() ||
                faces == null || faces.isEmpty() ||
                images.size != faces.size) {

            imgRgba
        }else{
            faces = MatOperation.rotateFaces(imgRgba, faces,
                    preProcessor!!.angleForRecognition)
            for (i in 0 until faces!!.size){
                try {
                    val size = Size(faceSize.toDouble(), faceSize.toDouble())
                    Imgproc.resize(images[i], images[i], size)

                    MatOperation.drawRectangleAndLabelOnPreview(imgRgba,
                            faces[i], recognition!!
                            .recognize(images[i], ""), front_camera)


                    flagOut++

                }catch (ex: Exception){
                    println(ex.message)
                }

            }
            if (flagOut >= 2){
                this.navigate<LockActivity>()
                this.finish()
            }

            imgRgba
        }
    }

}