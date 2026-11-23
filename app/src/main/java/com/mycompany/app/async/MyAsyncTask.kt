package com.mycompany.app.async

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import java.util.concurrent.ExecutorService

@Suppress("deprecation")
abstract class MyAsyncTask {
    @JvmField
    var taskContext: Context? = null
    @JvmField
    var taskHandler: Handler? = null
    @JvmField
    var cancelled: Boolean = false
    @JvmField
    var taskExecutor: ExecutorService? = null

    open fun doInBackground() {
    }

    fun start(context: Context?) {
        var context = context
        if (context != null) {
            try {
                val applicationContext = context.getApplicationContext()
                if (applicationContext != null) {
                    context = applicationContext
                }
            } catch (unused: Exception) {
            }
        }
        this.taskContext = context
        var handler = this.taskHandler
        if (handler == null) {
            handler = Handler(Looper.getMainLooper())
            this.taskHandler = handler
        }
        handler.post(Runnable {
            if (!this.cancelled) {
                this.onPreExecute()
            }
            this.execute(this.taskContext)
        })
    }

    fun execute(context: Context?) {
        var context = context
        if (context != null) {
            try {
                val applicationContext = context.getApplicationContext()
                if (applicationContext != null) {
                    context = applicationContext
                }
            } catch (unused: Exception) {
            }
        }
        this.taskContext = context
        var handler = this.taskHandler
        if (handler == null) {
            handler = Handler(Looper.getMainLooper())
            this.taskHandler = handler
        }
        val task = Runnable {
            try {
                if (!this.cancelled) {
                    this.doInBackground()
                }
            } catch (unused: Throwable) {
                this.cancelled = true
            }
            val taskHandler = this.taskHandler
            if (taskHandler != null) {
                taskHandler.post(Runnable {
                    MainUtil.clearHandler(this.taskHandler)
                    this.taskHandler = null
                    if (this.cancelled) {
                        this.onCancelled()
                    } else {
                        this.onPostExecute()
                    }
                })
            }
        }
        var executorServiceH = this.taskExecutor
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.executor(this.taskContext)
            if (executorServiceH == null) {
                return
            } else {
                this.taskExecutor = executorServiceH
            }
        }
        try {
            executorServiceH.execute(task)
        } catch (unused2: Exception) {
        }
    }

    open fun onCancelled() {
    }

    open fun onPostExecute() {
    }

    open fun onPreExecute() {
    }

    open fun onProgressUpdate() {
    }

    fun publishProgress() {
        val handler = this.taskHandler
        if (this.cancelled || handler == null) {
            return
        }
        handler.post(Runnable {
            if (this.cancelled || this.taskHandler == null) {
                return@Runnable
            }
            this.onProgressUpdate()
        })
    }
}
