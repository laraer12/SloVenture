package si.um.feri.sloventure.sloventureandroid.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
@Serializable
data class SensorReading(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double?,
    val weather: String?
) : Parcelable{
    fun getFormattedTimestamp(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(this.timestamp))
    }
}
