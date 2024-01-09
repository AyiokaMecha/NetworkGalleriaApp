package com.bignerdranch.photogallery

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class PollWorker(
    private val context: Context,
    workerParemeters: WorkerParameters
) : CoroutineWorker(context, workerParemeters) {



    override suspend fun doWork(): Result {
        val preferenceRepository = PreferenceRepository.get()
        val photoRepository = PhotoRepository()

        val query = preferenceRepository.storedQuery.first()
        val lastId = preferenceRepository.lastID.first()

        if (query.isEmpty()) {
            return Result.success()
        }

        return try {
            val items = photoRepository.searchPhotos(query)

            if (items.isNotEmpty()) {
                val newResultId = items.first().id
                if (newResultId == lastId) {
                    //same resutl
                } else {
                    preferenceRepository.setLastId(newResultId)

                        notifyUser(context)


                }
            }
            Result.success()
        } catch (ex: Exception) {
            //failure has occured
            Result.failure()
        }
    }

    private fun notifyUser(contextF: Context) {
        val intent = MainActivity.newIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val resources = context.resources

        val notification = NotificationCompat
            .Builder(context, NOTIFICATION_CHANNEL_ID)
            .setTicker(resources.getString(R.string.new_pictures_title))
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentTitle(resources.getString(R.string.new_pictures_title))
            .setContentText(resources.getString(R.string.new_pictures_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        if (ContextCompat.checkSelfPermission(contextF, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
        NotificationManagerCompat.from(context).notify(0, notification)}
    }
}