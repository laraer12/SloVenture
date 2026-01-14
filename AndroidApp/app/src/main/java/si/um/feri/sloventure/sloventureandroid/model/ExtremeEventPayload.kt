package si.um.feri.sloventure.sloventureandroid.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ExtremeEventPayload(
    val eventType: String,           // "crowd_detected"
    val attractionId: String,
    val attractionName: String,
    val attractionLatitude: Double,
    val attractionLongitude: Double,

    val userLatitude: Double?,
    val userLongitude: Double?,

    val timestamp: Long,

    val imageBase64: String?,
    val temperature: Double?,
    val weatherDescription: String?
) : Parcelable