package si.um.feri.sloventure.sloventureandroid.model

data class PhotoPayload(
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val orientation: OrientationData?,
    val temperature: Double?,
    val weatherDescription: String?
)

// to je za orientacijo telefona
data class OrientationData(
    val azimuth: Float, // vrtenje levo/desno okoli NAVPIČNE osi
    val pitch: Float, // nagib naprej/nazaj
    val roll: Float // nagib levo/desno
)