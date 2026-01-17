package si.um.feri.sloventure.sloventureandroid.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
@Serializable
data class CrowdSimulationPayload(
    val attractionId: String,
    val timestamp: Long,
    val numOfPeople: Int,
    val latitude: Double, //lokacija atrakcije
    val longitude: Double,
) : Parcelable {
    fun getFormattedTimestamp(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(this.timestamp))
    }
}