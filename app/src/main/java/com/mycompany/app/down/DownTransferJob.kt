package com.mycompany.app.down

import android.app.Notification
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownSvc

/**
 * Downloads as a user-initiated data transfer job (Android 14+, 2026-11-20).
 *
 * The download engine stays [MainDownSvc]; what changes is what keeps it alive in the background. Before, it was a
 * `dataSync` foreground service, which Android 15 stops after 6 hours a day. Now a download the user starts schedules
 * this job (allowed only while the app is visible, which a tap always is); the job binds the engine (a bound service
 * needs no foreground start), shows the engine's "Download manager" notification as the job's notification, and ends
 * when the engine has nothing left to do. Android then has no daily limit for it, and the job waits for a network
 * connection: when the connection drops, the downloads are paused and the job is rescheduled, so they continue by
 * themselves when the network is back.
 *
 * Where the job cannot be scheduled (Android 13 or older, or started from the background, e.g. a notification
 * button), [MainApp] starts the engine as the foreground service as before.
 */
// Android 14+ only where it matters (the job is only scheduled there); the companion is called on every version
class DownTransferJob : JobService() {
    private var params: JobParameters? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {}
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onStartJob(params: JobParameters): Boolean {
        this.params = params
        current = this
        // the job has to show its notification right away; the engine replaces it with its own (same id)
        val placeholder = pendingNotification ?: MainDownSvc.placeholderNotification(applicationContext)
        if (placeholder != null) setJobNotification(placeholder)
        // the engine runs as long as it is bound (it registers itself with MainApp in onBind and takes the pending
        // downloads / reserved ones from there)
        bound = try {
            bindService(Intent(applicationContext, MainDownSvc::class.java), connection, Context.BIND_AUTO_CREATE)
        } catch (_: Exception) {
            false
        }
        if (!bound) {
            current = null
            return false
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        // Android stops the job: network gone, the user stopped it in the task manager, or the system needs the
        // resources. Pause the downloads as "reserved" (they continue when the job runs again or OPUM is opened).
        val reschedule = params.stopReason == JobParameters.STOP_REASON_CONSTRAINT_CONNECTIVITY
        try {
            MainApp.from(applicationContext)?.downService?.stopAll(false, true)
        } catch (_: Exception) {
        }
        release()
        return reschedule
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun setJobNotification(notification: Notification) {
        val p = params ?: return
        try {
            setNotification(p, NOTIFICATION_ID, notification, JOB_END_NOTIFICATION_POLICY_REMOVE)
        } catch (_: Exception) {
        }
    }

    private fun release() {
        if (bound) {
            try { unbindService(connection) } catch (_: Exception) { }
            bound = false
        }
        if (current === this) current = null
        params = null
    }

    /** The engine is idle: end the job. */
    private fun done() {
        val p = params
        release()
        if (p != null) {
            try { jobFinished(p, false) } catch (_: Exception) { }
        }
    }

    companion object {
        const val JOB_ID = 0x0D0E
        /** Same id as the engine's foreground notification. */
        const val NOTIFICATION_ID = 2147483646

        @Volatile
        private var current: DownTransferJob? = null
        @Volatile
        private var pendingNotification: Notification? = null

        /** True while the job keeps the engine alive (the engine then hands its notification here). */
        @JvmStatic
        fun isActive(): Boolean = Build.VERSION.SDK_INT >= 34 && current != null

        /**
         * Schedules the job for the downloads the user just started. True when it was scheduled (or already runs);
         * false on Android 13 or older or when Android refuses (app not visible): the caller starts the engine as a
         * foreground service instead.
         */
        @JvmStatic
        fun schedule(context: Context?): Boolean {
            if (context == null || Build.VERSION.SDK_INT < 34) return false
            if (current != null) return true
            return try {
                val scheduler = context.getSystemService(JobScheduler::class.java) ?: return false
                if (!scheduler.canRunUserInitiatedJobs()) return false
                val info = JobInfo.Builder(JOB_ID, ComponentName(context, DownTransferJob::class.java))
                    .setUserInitiated(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setEstimatedNetworkBytes(JobInfo.NETWORK_BYTES_UNKNOWN.toLong(), JobInfo.NETWORK_BYTES_UNKNOWN.toLong())
                    .build()
                scheduler.schedule(info) == JobScheduler.RESULT_SUCCESS
            } catch (_: Exception) {
                false
            }
        }

        /** The engine's notification (progress, "Download manager"): shown as the job's notification. */
        @JvmStatic
        fun setNotification(notification: Notification) {
            val job = current
            if (job == null) {
                pendingNotification = notification
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) job.setJobNotification(notification)
        }

        /** The engine has finished everything: end the job (it is scheduled again with the next download). */
        @JvmStatic
        fun finish() {
            pendingNotification = null
            current?.done()
        }
    }
}
