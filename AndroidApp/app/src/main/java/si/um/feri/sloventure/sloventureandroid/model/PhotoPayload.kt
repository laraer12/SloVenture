package si.um.feri.sloventure.sloventureandroid.model

import android.net.Uri

import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

data class PhotoPayload(
    val imageUri: Uri,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val orientation: OrientationData?,
    val temperature: Double?,
    val weatherDescription: String?
) {
    // da lahko shranim tudi datum + čas ob zajemu slike
    fun getFormattedTimestamp(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(this.timestamp))
    }
}

// to je za orientacijo telefona
data class OrientationData(
    val azimuth: Float, // vrtenje levo/desno okoli NAVPIČNE osi
    val pitch: Float, // nagib naprej/nazaj
    val roll: Float // nagib levo/desno
)