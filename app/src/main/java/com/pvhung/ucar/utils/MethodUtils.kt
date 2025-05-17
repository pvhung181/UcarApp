package com.pvhung.ucar.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.widget.GridLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.graphics.toColorInt

object MethodUtils {
    fun saveBitmapToLocal(bm: Bitmap, file: File): String {
        val path: String
        try {
            val fos = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            path = file.absolutePath
        } catch (e: IOException) {
            return ""
        }
        return path
    }

    private fun isSpaceAvailable(
        gridState: List<List<Boolean>>,
        startRow: Int,
        startColumn: Int,
        rowSpan: Int,
        columnSpan: Int,
        columnCount: Int
    ): Boolean {
        if (startColumn + columnSpan > columnCount) return false
        for (row in startRow until startRow + rowSpan) {
            if (row >= gridState.size) return false
            for (col in startColumn until startColumn + columnSpan) {
                if (gridState[row][col]) return false
            }
        }
        return true
    }

    fun findDropPosition(
        gridState: List<MutableList<Boolean>>,
        itemRowSpan: Int,
        itemColumnSpan: Int,
        columnCount: Int
    ): Pair<Int, Int>? {
        for (row in gridState.indices) {
            for (col in 0 until columnCount) {
                if (isSpaceAvailable(
                        gridState,
                        row,
                        col,
                        itemRowSpan,
                        itemColumnSpan,
                        columnCount
                    )
                ) {
                    return Pair(row, col)
                }
            }
        }
        return null
    }

    fun sendEmail(context: Context, supportEmail: String) {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.setType("text/email")
        emailIntent.setPackage("com.google.android.gm")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
        var versionCode = "1.0"
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionCode = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val androidVersion = Build.VERSION.SDK_INT
        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            "App Report (" + context.packageName + ") - version [" + versionCode + "] - [" + getDeviceName() + "] - Android[" + androidVersion + "]"
        )
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")
        context.startActivity(Intent.createChooser(emailIntent, "Send mail Report App !"))
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        if (model.startsWith(manufacturer)) {
            return capitalize(model)
        }
        return capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true

        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(c.uppercaseChar())
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }

        return phrase.toString()
    }

    fun shareText(context: Context, text: String?, subject: String?) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        context.startActivity(Intent.createChooser(shareIntent, "Share..."))
    }



    private fun goToAppSettings(activity: Activity, permission: String) {
        val b = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        if (!b) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.setData(uri)
            activity.startActivity(intent)
        }
    }

    fun adjustLightness(color: Int, lightnessFactor: Float): Int {
        val clampedBlendFactor = 1 - lightnessFactor.coerceIn(0f, 1f)

        return ColorUtils.blendARGB(color, 0xFFFFFFFF.toInt(), clampedBlendFactor)
    }

    fun safeParseColor(hex: String?, default: Int = Color.TRANSPARENT): Int {
        return try {
            if (!hex.isNullOrBlank()) hex.toColorInt()
            else default
        } catch (_: Exception) {
            default
        }
    }

    fun isColorAvailable(color: String?): Boolean {
        return !(color.isNullOrBlank() || !color.startsWith("#"))
    }



}