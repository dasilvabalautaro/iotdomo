package com.hiddenodds.iotdomo.tool

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import java.util.*

class PermissionUtils {

    fun requestPermission(
            activity: Activity, requestCode: Int,
            vararg permissions: String): Boolean {
        var granted = true
        val permissionsNeeded = ArrayList<String>()

        permissions.forEach { s ->
            val permissionCheck = ContextCompat.checkSelfPermission(
                    activity, s)
            val hasPermission = permissionCheck == PackageManager.PERMISSION_GRANTED
            granted = granted and hasPermission
            when {
                !hasPermission -> permissionsNeeded.add(s)
            }
        }

        return when {
            granted -> true
            else -> {
                ActivityCompat.requestPermissions(activity,
                        permissionsNeeded.toTypedArray(),
                        requestCode)
                false
            }
        }
    }

    fun isPermissionGranted(grantPermissions: Array<out String>, grantResults: IntArray,
                            permission: String): Boolean {
        return grantPermissions.indices
                .firstOrNull { permission == grantPermissions[it] }
                ?.let { grantResults[it] == PackageManager.PERMISSION_GRANTED }
                ?: false
    }

    fun permissionGranted(
            requestCode: Int, permissionCode: Int, grantResults: IntArray) =
            if (requestCode == permissionCode) {
                grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED
            } else false

}