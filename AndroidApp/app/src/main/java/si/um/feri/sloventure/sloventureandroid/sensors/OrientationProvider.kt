package si.um.feri.sloventure.sloventureandroid.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.hardware.SensorEventListener

import si.um.feri.sloventure.sloventureandroid.model.OrientationData

// SensorEventListener za prejemanje podatkov senzorjev
class OrientationProvider(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager // dostop do senzorjev pa registracija listener-jev
    private val rotationSensor : Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) // tu so notri žiroskop + pospeškometer + magnetometer
    private var currentOrientation: OrientationData? = null

    // zagon poslušanja senzorjev
    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    fun stop() {
       sensorManager.unregisterListener(this)
    }

    fun getCurrentOrientation(): OrientationData? {
        return currentOrientation
    }

    // to se sproži vsakič, ko senzor pošlje nove podatke
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9) // to je 3 x 3 rotacijska matrika, ki predstavlja orientacijo telefona v prostoru
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            val orientationAngles = FloatArray(3) // to pa je polje za azimuth + pitch + roll (v radianih)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // pretvorim iz radianov v stopinje
            currentOrientation = OrientationData(
                azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat(),
                pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat(),
                roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
            )
        }
    }

    // NI V UPORABI (potrebno imeti, ker je implementacija zahtevana, četudi prazna)
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}