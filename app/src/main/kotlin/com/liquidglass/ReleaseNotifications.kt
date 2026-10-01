package com.liquidglass

import com.glaze.*

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.glaze.shared.ReleaseClient
import com.glaze.shared.ServerCredentials
import com.glaze.shared.UpcomingAlbum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest
import javax.inject.Inject

internal fun releaseNotificationEvent(album: UpcomingAlbum, now: Long): String? = when {
    album.saved && album.status == "rescanned" -> "${album.id}:added"
    album.followed && album.releaseAt > now -> "${album.id}:announced"
    else -> null
}

internal object ReleaseNotifications {
    const val JOB_ID = 4201
    const val RELEASE_ID = "release_id"
    private val lock = Mutex()
    fun schedule(context: Context, enabled: Boolean) {
        val scheduler = context.getSystemService(JobScheduler::class.java)
        if (!enabled) { scheduler.cancel(JOB_ID); return }
        if (scheduler.getPendingJob(JOB_ID) != null) return
        scheduler.schedule(JobInfo.Builder(JOB_ID, ComponentName(context, ReleaseNotificationService::class.java))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(15 * 60_000L).setPersisted(true).build())
    }
    suspend fun check(context: Context, account: ServerCredentials, address: String) = lock.withLock {
        if (address.isBlank()) return@withLock
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@withLock
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("releases", "Artist releases", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Upcoming albums from favorite artists and completed Pre-Saves" })
        if (!manager.areNotificationsEnabled() || manager.getNotificationChannel("releases").importance == NotificationManager.IMPORTANCE_NONE) return@withLock
        val api = ReleaseClient(address, account)
        val albums = try { api.albums() } finally { api.close() }
        val key = MessageDigest.getInstance("SHA-256").digest("${account.serverUrl}\n${account.username}".toByteArray())
            .joinToString("") { "%02x".format(it) }
        val prefs = context.getSharedPreferences("release_notifications", Context.MODE_PRIVATE)
        val seen = prefs.getStringSet(key, emptySet()).orEmpty().toMutableSet()
        albums.forEach { album ->
            val completed = album.saved && album.status == "rescanned"
            val event = releaseNotificationEvent(album, System.currentTimeMillis()) ?: return@forEach
            if (event in seen) return@forEach
            val intent = Intent(context, MainActivity::class.java).putExtra(RELEASE_ID, album.id)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pending = PendingIntent.getActivity(context, event.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            manager.notify(event.hashCode(), NotificationCompat.Builder(context, "releases")
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(if (completed) "${album.title} is in your library" else "${album.artist} has an album on the way")
                .setContentText(if (completed) "Your Pre-Save is ready to listen." else "${album.title} · Releases ${album.releaseDate}")
                .setContentIntent(pending).setAutoCancel(true).build())
            seen += event
        }
        prefs.edit().putStringSet(key, seen).commit()
    }
}

@AndroidEntryPoint
class ReleaseNotificationService : JobService() {
    @Inject internal lateinit var credentials: CredentialStore
    @Inject internal lateinit var settings: SettingsStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var work: Job? = null
    override fun onStartJob(params: JobParameters): Boolean {
        work = scope.launch {
            var retry = false
            try {
                withContext(Dispatchers.IO) {
                    credentials.load()?.let { ReleaseNotifications.check(this@ReleaseNotificationService, it, settings.load().companionUrl) }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { retry = true }
            jobFinished(params, retry)
        }
        return true
    }
    override fun onStopJob(params: JobParameters): Boolean { work?.cancel(); return true }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
