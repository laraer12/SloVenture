package si.um.feri.sloventure.sloventureandroid.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UserEventPayload(
    val eventType: String, //info, warning
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val imageBase64: String?
): Parcelable
