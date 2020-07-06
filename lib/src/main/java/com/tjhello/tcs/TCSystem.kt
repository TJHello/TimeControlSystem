package com.tjhello.tcs

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.google.gson.Gson
import com.tjhello.tcs.info.SuningDateInfo
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 作者:天镜baobao
 * 时间:2020/7/3  18:20
 * 说明:允许使用，但请遵循Apache License 2.0
 * 使用：
 * Copyright 2020/7/3 天镜baobao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object TCSystem {

    private lateinit var listener : Listener
    private lateinit var tools : Tools
    private lateinit var handler : Handler

    private var isAdult : Boolean = false
    private var gameTime = 0L
    private var calendar = Calendar.getInstance()
    private var nowDate = getNowDate()
    private var lastTime = SystemClock.elapsedRealtime()
    private var timerTask = TimerTask()
    private var isPause = true
    private var isInitTime = false
    private var isExit = false
    private var isOnly30Min = false
    private var isRealName = false


    private const val MAX_MS_WEEK = 3*60*60*1000
    private const val MAX_MS = MAX_MS_WEEK/2
    private const val MAX_MS_NOT_REAL_NAME = 60*60*1000

    private const val SHARE_IS_ADULT = "is_adult"
    private const val SHARE_GAME_TIME = "game_time"
    private const val SHARE_GAME_DATE = "game_date"


    fun init(context:Context,listener:Listener){
        handler = Handler(Looper.getMainLooper())
        this.listener = listener
        tools = Tools(context)
        isAdult = tools.getSharedPreferencesValue(SHARE_IS_ADULT,false)?:false
        gameTime = tools.getSharedPreferencesValue(SHARE_GAME_TIME,0L)?:0L
        isRealName = tools.containsSharedPreferencesKey(SHARE_IS_ADULT)
        object : Thread(){
            override fun run() {
                initDate()
                nowDate = getNowDate()
                val gameDate = tools.getSharedPreferencesValue(SHARE_GAME_DATE, nowDate)?:nowDate
                if(nowDate != gameDate){
                    gameTime = 0L
                    tools.setSharedPreferencesValue(SHARE_GAME_TIME,gameTime)
                    tools.setSharedPreferencesValue(SHARE_GAME_DATE, nowDate)
                }
                log("[init]gameTime:${gameTime/1000/60}min,nowDate:$nowDate,gameDate:$gameDate,isAdult:$isAdult,isRealName:$isRealName")
                if(!check()){
                    isExit= true
                    handler.post {
                        listener.onExit()
                    }
                }
            }
        }.start()
        //初始化时间


    }

    /**
     * @param isAdult true:成年人,false:未成年人
     */
    fun setUserInfo(isAdult:Boolean){
        TCSystem.isAdult = isAdult
        tools.setSharedPreferencesValue(SHARE_IS_ADULT,isAdult)
        isRealName = true
        log("[setUserInfo]isAdult:$isAdult,isRealName:$isRealName")
        if(!isAdult){
            refreshTime()
            if(!check()){
                isExit= true
                handler.post {
                    listener.onExit()
                }
            }
        }
    }

    fun onPause(){
        if(isExit) return
        refreshTime()
        isPause = true
        timerTask.stopTimer()
    }

    fun onResume(){
        if(isExit|| isAdult) return
        isPause = false
        timerTask.startTimer(60*1000,60*1000)
    }

    fun onExit(){
        refreshTime()
    }

    fun checkRealName(context: Context):Boolean{
        return tools.containsSharedPreferencesKey(SHARE_IS_ADULT)
    }

    private fun refreshTime(){
        if(isInitTime){
            gameTime+= max((SystemClock.elapsedRealtime()-lastTime),0)
            lastTime = SystemClock.elapsedRealtime()
            tools.setSharedPreferencesValue(SHARE_GAME_TIME,gameTime)
            log("[refreshTime]${gameTime/1000/60}min")
        }
    }

    private fun check():Boolean{
        if(!isInitTime){
            return true
        }
        if(isRealName){
            if(!isAdult){
                val nowWeek = getNowWeek()
                val nowHour = getNowHour()
                if(nowHour>=22 || nowHour<=8){
                    log("time ban:$nowHour")
                    return false
                }
                if(nowWeek==1||nowWeek==7){
                    if(gameTime>= MAX_MS_WEEK){
                        log("time out:${gameTime/1000/60}min")
                        return false
                    }
                    val time = MAX_MS_WEEK-gameTime
                    if(time<=30*60*1000){
                        isOnly30Min = true
                        listener.onRefreshTime(time)
                    }
                }else{
                    if(gameTime>= MAX_MS){
                        log("time out:${gameTime/1000/60}min")
                        return false
                    }
                    val time = MAX_MS_WEEK-gameTime
                    if(time<=30*60*1000){
                        isOnly30Min = true
                        listener.onRefreshTime(time)
                    }
                }
            }
        }else{
            if(gameTime>= MAX_MS_NOT_REAL_NAME){
                log("time out:${gameTime/1000/60}min")
                return false
            }
            val time = MAX_MS_NOT_REAL_NAME-gameTime
            if(time<=30*60*1000){
                isOnly30Min = true
                listener.onRefreshTime(time)
            }
        }

        return true
    }

    interface Listener{
        fun onExit()//需要强制退出游戏

        fun onRefreshTime(time:Long)//剩余时间刷新
    }

    private class TimerTask : BaseTimerTask(){
        override fun run() {
            if(isExit){
                stopTimer()
                return
            }
            refreshTime()
            if(!check()){
                isExit= true
                handler.post {
                    listener.onExit()
                }
            }
        }
    }

    private fun initDate(){
        val dateStr = getOnLineDate()?: getOnLocalDate()
        try {
            val sdf = DateFormat.getInstance() as SimpleDateFormat
            sdf.applyPattern("yyyy-MM-dd HH:mm:ss")
            val date = sdf.parse(dateStr)?:Date()
            calendar.time = date
        }catch (e:Exception){
            e.printStackTrace()
        }
        isInitTime = true
    }

    private fun getNowDate():String{
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)+1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val date = format2Num(year)+"-"+ format2Num(month)+"-"+ format2Num(day)
        log(date)
        return date
    }

    private fun getNowHour():Int{
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    private fun getNowWeek():Int{
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    private fun format2Num(num:Int):String{
        return String.format(Locale.getDefault(),"%02d",num)
    }

    private fun getOnLocalDate():String{
        val sdf = DateFormat.getInstance() as SimpleDateFormat
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
    }

    private fun getOnLineDate():String?{
        try {
            val dateText = OKHttpUtil.doGet("http://quan.suning.com/getSysTime.do")
            if(!dateText.isNullOrEmpty()){
                val suningDateInfo = Gson().fromJson(dateText,SuningDateInfo::class.java)
                if(suningDateInfo!=null){
                    return suningDateInfo.sysTime2
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return null
    }

    private fun log(msg:String){
        Log.i("TCSystem",msg)
    }


}