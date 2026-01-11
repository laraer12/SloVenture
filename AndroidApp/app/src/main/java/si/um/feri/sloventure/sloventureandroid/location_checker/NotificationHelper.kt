package si.um.feri.sloventure.sloventureandroid.location_checker

import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

import timber.log.Timber

import si.um.feri.sloventure.sloventureandroid.R
import si.um.feri.sloventure.sloventureandroid.MainActivity
import si.um.feri.sloventure.sloventureandroid.model.Attraction

class NotificationHelper(private val context: Context) {
    private val channelId = "EVENTS"

    fun showAttractionNotification(attraction: Attraction) {
        Timber.i("Trying to show notification for attraction: ${attraction.name} (id: ${attraction.id})")

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("OPEN_CAMERA", true)
            putExtra("ATTRACTION_ID", attraction.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            attraction.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        sendNotification(
            context.getString(R.string.nearby_attraction),
            context.getString(R.string.take_pic, attraction.name),
            pendingIntent
        )
    }

    private fun sendNotification(
        title: String,
        message: String,
        pendingIntent: PendingIntent
    ) {
        Timber.i("Sending notification: $title - $message")

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(title.hashCode(), notification)
    }
}