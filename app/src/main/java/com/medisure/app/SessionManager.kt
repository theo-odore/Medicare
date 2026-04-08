package com.medisure.app

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME = "MediSureSession"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, token: String, userId: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_ACCESS_TOKEN, token)
        editor.putString(KEY_USER_ID, userId)
        editor.apply()
    }

    fun getToken(context: Context): String? {
        return getPreferences(context).getString(KEY_ACCESS_TOKEN, null)
    }

    fun getUserId(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_ID, null)
    }
    
    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}
