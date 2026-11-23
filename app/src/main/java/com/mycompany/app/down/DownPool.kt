package com.mycompany.app.down

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * The download connections' own thread pool (2026-11-20). Range parts, the audio stream, HLS segment workers and
 * their retries run here, at most [SIZE] at a time for all downloads together; more wait in line. They used to run
 * on the app's shared unbounded pool: downloads at a time × parts threads (20 by default, no ceiling with
 * "unlimited"). Idle threads end after 30 s.
 */
object DownPool {
    const val SIZE = 8

    private val count = AtomicInteger()
    private val executor = ThreadPoolExecutor(SIZE, SIZE, 30, TimeUnit.SECONDS, LinkedBlockingQueue()) { r ->
        Thread(r, "opum-down-" + count.incrementAndGet()).apply { priority = Thread.NORM_PRIORITY - 1 }
    }.apply { allowCoreThreadTimeOut(true) }

    @JvmStatic
    fun execute(runnable: Runnable?) {
        if (runnable == null) return
        try { executor.execute(runnable) } catch (_: Exception) { }
    }
}
