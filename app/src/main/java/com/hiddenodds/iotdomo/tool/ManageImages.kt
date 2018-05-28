package com.hiddenodds.iotdomo.tool

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Toast
import com.hiddenodds.iotdomo.App
import com.hiddenodds.iotdomo.R
import java.io.File
import java.io.IOException
import javax.inject.Inject



class ManageImages @Inject constructor(private val activity:
                                       AppCompatActivity):
        ActivityCompat.OnRequestPermissionsResultCallback  {

    val GALLERY_PERMISSIONS_REQUEST = 0
    val GALLERY_IMAGE_REQUEST = 1
    val CAMERA_PERMISSIONS_REQUEST = 2
    val CAMERA_IMAGE_REQUEST = 3
    private val MAX_DIMENSION_IMAGE = 1200
    private val FILE_NAME = "temp.jpg"
    private val permissionUtils: PermissionUtils = PermissionUtils()
    private val context = App.appComponent.context()


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
                //startCamera()
                println("Permission Ok")
            }
            GALLERY_PERMISSIONS_REQUEST -> if (permissionUtils
                            .permissionGranted(requestCode,
                                    GALLERY_PERMISSIONS_REQUEST,
                                    grantResults)) {
                println("Permission Ok")
            }
        }
    }

    fun permissionCamera(): Boolean{
        return permissionUtils.requestPermission(
                activity,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)
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

