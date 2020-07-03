package com.tjhello.tcs

import android.content.Context
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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
    private var nowDate = getNowDate()

    fun init(context:Context,listener:Listener){
        //初始化时间
        this.listener = listener

    }

    /**
     * @param isAdult true:成年人,false:未成年人
     */
    fun setUserInfo(isAdult:Boolean){

    }

    fun onPause(context:Context){

    }

    fun onResume(context:Context){

    }

    interface Listener{

        fun onTimeOut()//超出允许的游戏时长

        fun onTimeBan()//禁止游戏的时间段

    }

    private fun getNowDate():String{
        val sdf = DateFormat.getInstance() as SimpleDateFormat
        sdf.applyPattern("yyyy-MM-dd")
        return sdf.format(Date())
    }


}