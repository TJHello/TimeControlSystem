package com.tjhello.app.tcs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.tjhello.tcs.TCSystem

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TCSystem.init(this,object : TCSystem.Listener{
            override fun onExit() {
                Log.i("TCSystem","onExit")
            }

            override fun onRefreshTime(time: Long) {
                Log.i("TCSystem","onRefreshTime:$time")
            }
        })
        TCSystem.setUserInfo(false)
    }

    override fun onPause() {
        super.onPause()
        TCSystem.onPause()
    }

    override fun onResume() {
        super.onResume()
        TCSystem.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        TCSystem.onExit()
    }
}