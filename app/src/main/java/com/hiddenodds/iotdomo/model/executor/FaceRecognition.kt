package com.hiddenodds.iotdomo.model.executor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.hiddenodds.iotdomo.App
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceRecognition @Inject constructor() {
    //private var faceDetector: FaceDetector? = null
    private val context = App.appComponent.context()
    var smile: Float = 0f

    init {
        /*faceDetector = FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build()*/
    }

    fun detectFace(imageBitmap: Bitmap): Bitmap?{
        val bmp = Bitmap.createBitmap(imageBitmap.width,
                imageBitmap.height, imageBitmap.config)
        val canvas = Canvas(bmp)
        canvas.drawBitmap(imageBitmap, 0F, 0F, null)
        val paint = Paint()
        paint.color = Color.GREEN
        paint.style =  Paint.Style.STROKE
        paint.strokeWidth = 5F
        val landmarkPaint = Paint()
        landmarkPaint.color = Color.RED
        landmarkPaint.style = Paint.Style.STROKE
        landmarkPaint.strokeWidth = 5F
        /*val frame = Frame.Builder().setBitmap(imageBitmap).build()
        val faces = faceDetector!!.detect(frame)
        if (faces.size() > 0) {
            for (i in 0 until faces.size()) {
                val face = faces.valueAt(i)
                canvas.drawRect(
                        face.position.x,
                        face.position.y,
                        face.position.x + face.width,
                        face.position.y + face.height, paint)
                for (landmark in face.landmarks) {
                    val cx = landmark.position.x
                    val cy = landmark.position.y
                    canvas.drawCircle(cx, cy, 5F, landmarkPaint)
                }
                this.smile = face.isSmilingProbability
            }
            println("Faces: " + faces.size())
        } else {
            this.smile = 0f
            println("Faces: nulo ")
            return null

        }
*/
        return bmp
    }
}