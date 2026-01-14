package si.um.feri.sloventure.sloventureandroid.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import java.io.ByteArrayOutputStream


fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val ratioBitmap = width.toFloat() / height.toFloat()
    val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

    var finalWidth = maxWidth
    var finalHeight = maxHeight

    if (ratioMax > ratioBitmap) {
        finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
    } else {
        finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
    }
    return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

        bitmap = resizeBitmap(bitmap, 1024, 768)

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.NO_WRAP) // NO_WRAP, da ne bo '\n'
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

