package com.hiddenodds.iotdomo.model.executor

import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper
import ch.zhaw.facerecognitionlibrary.Helpers.MatName
import ch.zhaw.facerecognitionlibrary.Helpers.PreferencesHelper
import ch.zhaw.facerecognitionlibrary.PreProcessor.PreProcessorFactory
import ch.zhaw.facerecognitionlibrary.Recognition.Recognition
import ch.zhaw.facerecognitionlibrary.Recognition.RecognitionFactory
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import javax.inject.Inject


class Training @Inject constructor(private val activity: AppCompatActivity) {

    companion object {
        init {
            OpenCVLoader.initDebug()
        }
    }

    init {
        PreferenceManager.setDefaultValues(activity,
                ch.zhaw.facerecognitionlibrary.R.xml.preferences, false)
    }

    fun training(): Boolean{
        var flag = false
        val preProcessor = PreProcessorFactory(activity.applicationContext)
        val preferenceHelper = PreferencesHelper(activity.applicationContext)
        val algorithm = preferenceHelper.classificationMethod
        val fileHelper = FileHelper()
        var recognition: Recognition? = null
        fileHelper.createDataFolderIfNotExsiting()
        val fs = preferenceHelper.faceSize
        val persons = fileHelper.trainingList
        if (persons.isNotEmpty()){

            try {
                recognition = RecognitionFactory
                        .getRecognitionAlgorithm(activity.applicationContext,
                                Recognition.TRAINING, algorithm)
            }catch (ex: Exception){
                println(ex.message)
            }


            for (person: File in persons){
                if (person.isDirectory){
                    val files = person.listFiles()
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

                            }catch (ex: Exception){
                                println(ex.message)
                            }
                        }
                    }
                }
            }

            if (recognition!!.train()){
                flag = true
            }


        }

        return flag

    }
}