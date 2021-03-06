package com.tjhello.tcs

import android.content.Context
import android.content.SharedPreferences.Editor

/**
 * 创建者：TJbaobao
 * 时间:2020/2/24 10:30
 * 使用:
 * 说明:
 **/
class Tools(context: Context) {

    private val pref = context.getSharedPreferences("tc-system", 0)

    fun <T>getSharedPreferencesValue(key: String?, defValue: T?): T? {
        if (defValue == null || defValue is String) {
            return pref.getString(key, defValue as? String) as? T?
        } else if (defValue is Int) {
            return pref.getInt(key, defValue) as? T?
        } else if (defValue is Float) {
            return pref.getFloat(key, defValue) as? T?
        } else if (defValue is Long) {
            return pref.getLong(key, defValue) as? T?
        } else if (defValue is Boolean) {
            return pref.getBoolean(key, defValue) as? T?
        }
        return null
    }

    fun containsSharedPreferencesKey(key:String):Boolean{
        return pref.contains(key)
    }

    fun setSharedPreferencesValue(key: String?, value: Any?) {
        val editor: Editor = pref.edit()
        if (value == null || value is String) {
            editor.putString(key, value as String?)
        } else if (value is Int) {
            editor.putInt(key, (value as Int?)!!)
        } else if (value is Float) {
            editor.putFloat(key, (value as Float?)!!)
        } else if (value is Long) {
            editor.putLong(key, (value as Long?)!!)
        } else if (value is Boolean) {
            editor.putBoolean(key, (value as Boolean?)!!)
        }
        editor.apply()
    }

}