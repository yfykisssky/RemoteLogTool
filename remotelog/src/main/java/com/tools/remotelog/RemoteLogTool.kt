package com.tools.remotelog

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.tools.remotelog.remotelogcat.LogcatRunner
import java.io.IOException
import java.util.*

class RemoteLogTool {

    companion object {
        val instance: RemoteLogTool by lazy {
            RemoteLogTool()
        }
        private const val TAG = "RemoteLogTool"

        private var isLogOut = false

        fun log(log: String?) {
            Log.w(TAG, log ?: "null")
        }
    }

    private var logRunning = false

    fun initTool(con: Context, isLog: Boolean = false) {
        isLogOut = isLog
        if (!isLogOut) {
            return
        }
        try {
            LogcatRunner.getInstance()
                .config(
                    LogcatRunner.LogConfig.builder()
                        .setWsCanReceiveMsg(false)
                        .write2File(true)
                ).with(con.applicationContext)
                .start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun destoryTool() {
        if (!isLogOut) {
            return
        }
        LogcatRunner.getInstance().stop()
    }

    fun startLog() {
        if (!isLogOut) {
            return
        }
        if (logRunning) {
            return
        }
        logRunning = true
        Thread {
            val random = Random()
            var i = 0
            while (logRunning) {
                if (random.nextBoolean()) {
                    log("run --> $i")
                } else {
                    log("run --> $i")
                }
                SystemClock.sleep((random.nextInt(5000) + 100).toLong())
                i++
            }
        }.start()
    }

    fun stopLog() {
        if (!isLogOut) {
            return
        }
        logRunning = false
    }


}