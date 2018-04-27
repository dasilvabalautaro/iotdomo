package com.hiddenodds.iotdomo.tool

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.Signature
import com.google.common.io.BaseEncoding
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class PackageManagerUtils {
    @SuppressLint("PackageManagerGetSignatures")
    fun getSignature(pm: PackageManager, packageName: String): String? {
        return try {
            val packageInfo = pm.getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES)
            if (packageInfo?.signatures == null
                    || packageInfo.signatures.isEmpty()
                    || packageInfo.signatures[0] == null) {
                null
            } else signatureDigest(packageInfo.signatures[0])
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

    }

    private fun signatureDigest(sig: Signature): String? {
        val signature = sig.toByteArray()
        return try {
            val md = MessageDigest.getInstance("SHA1")
            val digest = md.digest(signature)
            BaseEncoding.base16().lowerCase().encode(digest)
        } catch (e: NoSuchAlgorithmException) {
            null
        }

    }
}