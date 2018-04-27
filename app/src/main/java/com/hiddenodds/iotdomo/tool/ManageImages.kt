package com.hiddenodds.iotdomo.tool

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Toast
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequest
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import com.hiddenodds.iotdomo.App
import com.hiddenodds.iotdomo.R
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject



class ManageImages @Inject constructor(private val activity:
                                       AppCompatActivity):
        ActivityCompat.OnRequestPermissionsResultCallback  {

    val GALLERY_PERMISSIONS_REQUEST = 0
    val GALLERY_IMAGE_REQUEST = 1
    val CAMERA_PERMISSIONS_REQUEST = 2
    val CAMERA_IMAGE_REQUEST = 3
    val ANDROID_PACKAGE_HEADER = "X-Android-Package"
    val ANDROID_CERT_HEADER = "X-Android-Cert"
    val ALBUM_APP = "img_iotdomo"
    private val MAX_DIMENSION_IMAGE = 1200
    private val FILE_NAME = "temp.jpg"
    private val permissionUtils: PermissionUtils = PermissionUtils()
    private val packageManagerUtils: PackageManagerUtils = PackageManagerUtils()
    private val context = App.appComponent.context()

    private var resultTensor: String = ""

    var observableResult: Subject<String> = PublishSubject.create()

    init {

        this.observableResult
                .subscribe{this.resultTensor}

    }

    fun startGalleryChooser() {
        if (permissionUtils.requestPermission(activity,
                        GALLERY_PERMISSIONS_REQUEST,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activity.startActivityForResult(Intent.createChooser(intent,
                    activity.getString(R.string.select_photo)),
                    GALLERY_IMAGE_REQUEST)
        }
    }

    fun startCamera() {
        if (permissionUtils.requestPermission(
                        activity,
                        CAMERA_PERMISSIONS_REQUEST,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoUri = FileProvider.getUriForFile(context,
                    context.applicationContext.packageName
                            + ".provider", getCameraFile())
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            activity.startActivityForResult(intent, CAMERA_IMAGE_REQUEST)

        }
    }

    fun getCameraFile(): File {
        /*return File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), ALBUM_APP)*/
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dir, FILE_NAME)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {

        when (requestCode) {
            CAMERA_PERMISSIONS_REQUEST -> if (permissionUtils
                            .permissionGranted(requestCode,
                                    CAMERA_PERMISSIONS_REQUEST,
                                    grantResults)) {
                startCamera()
            }
            GALLERY_PERMISSIONS_REQUEST -> if (permissionUtils
                            .permissionGranted(requestCode,
                                    GALLERY_PERMISSIONS_REQUEST,
                                    grantResults)) {
                startGalleryChooser()
            }
        }
    }

    fun uploadImage(uri: Uri?) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                val bitmap = scaleBitmapDown(
                        MediaStore.Images.Media
                                .getBitmap(context
                                        .applicationContext
                                        .contentResolver, uri),
                        MAX_DIMENSION_IMAGE)

                callCloudVision(bitmap)
                //mMainImage.setImageBitmap(bitmap)

            } catch (e: IOException) {
                activity.toast("Image picking failed because " + e.message)
            }

        } else {
            activity.toast("Image picker gave us a null image.")
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Throws(IOException::class)
    private fun callCloudVision(bitmap: Bitmap) {
        // Switch text to loading
        //mImageDetails.setText(R.string.loading_message)

        // Do the real work in an async task, because we need to use the network anyway
        object : AsyncTask<Any, Void, String>() {
            override fun doInBackground(vararg params: Any): String {
                try {
                    val httpTransport = AndroidHttp.newCompatibleTransport()
                    val jsonFactory = GsonFactory.getDefaultInstance()

                    val requestInitializer = object :
                            VisionRequestInitializer(Constants.CLOUD_VISION_API_KEY) {
                        /**
                         * We override this so we can inject important identifying fields into the HTTP
                         * headers. This enables use of a restricted cloud platform API key.
                         */
                        @Throws(IOException::class)
                        override fun initializeVisionRequest(visionRequest: VisionRequest<*>?) {
                            super.initializeVisionRequest(visionRequest)

                            val packageName = context.applicationContext.packageName
                            visionRequest!!.requestHeaders
                                    .set(ANDROID_PACKAGE_HEADER, packageName)

                            val sig = packageManagerUtils
                                    .getSignature(context.applicationContext
                                            .packageManager,
                                            packageName)

                            visionRequest.requestHeaders.set(ANDROID_CERT_HEADER, sig)
                        }
                    }

                    val builder = Vision.Builder(httpTransport, jsonFactory, null)
                    builder.setVisionRequestInitializer(requestInitializer)

                    val vision = builder.build()

                    val batchAnnotateImagesRequest = BatchAnnotateImagesRequest()
                    batchAnnotateImagesRequest.requests = object : ArrayList<AnnotateImageRequest>() {
                        init {
                            val annotateImageRequest = AnnotateImageRequest()

                            // Add the image
                            val base64EncodedImage = Image()
                            // Convert the bitmap to a JPEG
                            // Just in case it's a format that Android understands but Cloud Vision
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
                            val imageBytes = byteArrayOutputStream.toByteArray()

                            // Base64 encode the JPEG
                            base64EncodedImage.encodeContent(imageBytes)
                            annotateImageRequest.image = base64EncodedImage

                            // add the features we want
                            annotateImageRequest.features = object : ArrayList<Feature>() {
                                init {
                                    val labelDetection = Feature()
                                    labelDetection.type = "LABEL_DETECTION"
                                    labelDetection.maxResults = 10
                                    add(labelDetection)
                                }
                            }

                            // Add the list of one thing to the request
                            add(annotateImageRequest)
                        }
                    }

                    val annotateRequest = vision.images().annotate(batchAnnotateImagesRequest)
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.disableGZipContent = true
                    println("created Cloud Vision request object, sending request")

                    val response = annotateRequest.execute()
                    return convertResponseToString(response)

                } catch (e: GoogleJsonResponseException) {
                    println("failed to make API request because " + e.content)

                } catch (e: IOException) {
                    println("failed to make API request because of other IOException " + e.message)
                }

                return "Cloud Vision API request failed. Check logs for details."
            }

            override fun onPostExecute(result: String) {
                //mImageDetails.setText(result)
                resultTensor = result
                observableResult.onNext(resultTensor)
            }
        }.execute()
    }

    private fun convertResponseToString(response:
                                        BatchAnnotateImagesResponse): String {
        var message = "I found these things:\n\n"

        val labels = response.responses[0].labelAnnotations
        if (labels != null) {
            for (label in labels) {
                message += String.format(Locale.US, "%.3f: %s", label.score, label.description)
                message += "\n"
            }
        } else {
            message += "nothing"
        }

        return message
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {

        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = maxDimension
        var resizedHeight = maxDimension

        when {
            originalHeight > originalWidth -> {
                resizedHeight = maxDimension
                resizedWidth = (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
            }
            originalWidth > originalHeight -> {
                resizedWidth = maxDimension
                resizedHeight = (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
            }
            originalHeight == originalWidth -> {
                resizedHeight = maxDimension
                resizedWidth = maxDimension
            }
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth,
                resizedHeight, false)
    }

    fun setControlImage(uri: Uri?, imageView: ImageView) {
        when {
            uri != null -> try {
                val bitmap = scaleBitmapDown(
                        MediaStore.Images.Media
                                .getBitmap(context.contentResolver, uri),
                        MAX_DIMENSION_IMAGE)
                imageView.setImageBitmap(bitmap)

            } catch (e: IOException) {
                println(e.message)

            }
            else -> println(R.string.value_null)
        }
    }

    private fun Activity.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

