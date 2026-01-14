package si.um.feri.sloventure.sloventureandroid.location_checker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import si.um.feri.sloventure.sloventureandroid.MainActivity
import si.um.feri.sloventure.sloventureandroid.R
import si.um.feri.sloventure.sloventureandroid.model.Attraction
import timber.log.Timber

class NotificationHelper(private val context: Context) {
    private val channelId = "EVENTS"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showAttractionNotification(attraction: Attraction) {
        Timber.i("Showing extreme event notification for ${attraction.name}")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra("navigate_to", "extreme_event")

            putExtra("attraction_id", attraction.id)
            putExtra("attraction_name", attraction.name)
            putExtra("attraction_lat", attraction.location.lat)
            putExtra("attraction_lon", attraction.location.lon)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            attraction.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Extreme event nearby")
            .setContentText("Crowd detected near ${attraction.name}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context)
            .notify(attraction.id.hashCode(), notification)
    }
}