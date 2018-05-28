package com.hiddenodds.iotdomo.presentation.view.activity


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper
import ch.zhaw.facerecognitionlibrary.Helpers.MatName
import ch.zhaw.facerecognitionlibrary.Helpers.PreferencesHelper
import ch.zhaw.facerecognitionlibrary.PreProcessor.PreProcessorFactory
import ch.zhaw.facerecognitionlibrary.Recognition.Recognition
import ch.zhaw.facerecognitionlibrary.Recognition.RecognitionFactory
import com.hiddenodds.iotdomo.R
import com.hiddenodds.iotdomo.tool.Constants
import com.hiddenodds.iotdomo.tool.PreferenceHelperApp
import com.hiddenodds.iotdomo.tool.PreferenceHelperApp.set
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File

class TrainingActivity: AppCompatActivity() {
    companion object {
        init {
            OpenCVLoader.initDebug()
        }
    }

    @BindView(R.id.tv_progress)
    @JvmField var tvProgress: TextView? = null
    @OnClick(R.id.bt_training)
    fun executeTraining(){
        async(CommonPool) {
            training()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_training)
        ButterKnife.bind(this)
        tvProgress!!.movementMethod = ScrollingMovementMethod()
        PreferenceManager.setDefaultValues(this,
                ch.zhaw.facerecognitionlibrary.R.xml.preferences, false)

    }

    private inline fun <reified T : Activity> Activity.navigate() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

    private fun Activity.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun training(){
        var flag = false
        val preProcessor = PreProcessorFactory(applicationContext)
        val preferenceHelper = PreferencesHelper(applicationContext)
        val algorithm = preferenceHelper.classificationMethod
        val fileHelper = FileHelper()
        var recognition: Recognition? = null
        fileHelper.createDataFolderIfNotExsiting()
        val fs = preferenceHelper.faceSize
        val persons = fileHelper.trainingList
        if (persons.isNotEmpty()){

            try {
                recognition = RecognitionFactory
                        .getRecognitionAlgorithm(applicationContext,
                                Recognition.TRAINING, algorithm)
            }catch (ex: Exception){
                println(ex.message)
            }


            for (person: File in persons){
                if (person.isDirectory){
                    val files = person.listFiles()
                    var counter = 1
                    for (file: File in files){
                        if (FileHelper.isFileAnImage(file)){
                            val imgRgb = Imgcodecs.imread(file.absolutePath)
                            println(file.absolutePath)
                            Imgproc.cvtColor(imgRgb, imgRgb, Imgproc.COLOR_BGRA2RGBA)
                            var processedImage = Mat()
                            imgRgb.copyTo(processedImage)
                            val images: List<Mat>? = preProcessor
                                    .getProcessedImage(processedImage,
                                            PreProcessorFactory
                                                    .PreprocessingMode.RECOGNITION)

                            if (images == null || images.size > 1) {
                                continue
                            }else{
                                val size = Size(fs.toDouble(), fs.toDouble())
                                Imgproc.resize(images[0], images[0], size)
                                processedImage = images[0]

                            }
                            if (processedImage.empty()){
                                continue
                            }
                            val tokens = file.parent.split("/")
                            val name = tokens[tokens.size - 1]
                            val matName = MatName("processedImage",
                                    processedImage)
                            fileHelper.saveMatToImage(matName, FileHelper.DATA_PATH)
                            try {
                                recognition!!.addImage(processedImage,
                                        name, false)
                                val counterPost = counter
                                val filesLength = files.size
                                runOnUiThread({
                                    tvProgress!!.append("Image $counterPost of " +
                                            " $filesLength from $name imported.\n")
                                })
                                counter++
                            }catch (ex: Exception){
                                println(ex.message)
                            }
                        }
                    }
                }
            }
            flag = true
            if (recognition!!.train()){
                runOnUiThread({
                    val prefs = PreferenceHelperApp.customPrefs(this,
                            Constants.PREFERENCE_IOTDOMO)
                    prefs[Constants.REGISTER_FACE] = true
                    toast("Training successful")
                })
            }else{
                runOnUiThread({
                    toast("Training failed")
                })

            }


        }

        runOnUiThread({
            if (!flag){
                toast("Image not found")
            }
            this.navigate<InitRecognitionActivity>()
            this.finish()
        })

    }
}