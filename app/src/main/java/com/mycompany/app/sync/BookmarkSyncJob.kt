package com.mycompany.app.sync

import android.app.job.JobParameters
import android.app.job.JobService

/** The background run of the bookmark sync (2026-11-28): every workspace that syncs in the background, one after another. */
class BookmarkSyncJob : JobService() {
    @Volatile private var stopped = false

    override fun onStartJob(params: JobParameters): Boolean {
        stopped = false
        val app = applicationContext
        Thread {
            var failed = false
            try {
                val soon = params.jobId == BookmarkSync.JOB_SOON
                for (number in BookmarkSync.activeNumbers(app)) {
                    if (stopped) break
                    if (!soon && !BookmarkSyncStore.config(app, number).background) continue
                    if (soon && !BookmarkSync.isDirty(app, number)) continue
                    val r = BookmarkSync.sync(app, number)
                    if (r == com.mycompany.app.data.sync.SyncError.NETWORK || r == com.mycompany.app.data.sync.SyncError.LOCKED) failed = true
                }
            } catch (_: Exception) {
            }
            // a change made before leaving is tried again later when the network or the lock was in the way
            jobFinished(params, failed && params.jobId == BookmarkSync.JOB_SOON)
        }.start()
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        stopped = true
        return params.jobId == BookmarkSync.JOB_SOON
    }
}
