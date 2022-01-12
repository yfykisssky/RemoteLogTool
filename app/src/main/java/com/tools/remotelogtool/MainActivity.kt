package com.tools.remotelogtool

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tools.remotelog.RemoteLogTool

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RemoteLogTool.instance.initTool(this,true)
        RemoteLogTool.instance.startLog()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        RemoteLogTool.instance.stopLog()
    }


    override fun onDestroy() {
        super.onDestroy()
        RemoteLogTool.instance.destoryTool()
    }
}