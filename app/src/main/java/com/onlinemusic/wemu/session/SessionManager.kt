package com.onlinemusic.wemu.session

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.onlinemusic.wemu.Login

class SessionManager(  // Context
    var _context: Context
) {
    // Shared Preferences
    var pref: SharedPreferences

    // Editor for Shared preferences
    var editor: SharedPreferences.Editor

    // Shared pref mode
    var PRIVATE_MODE = 0

    /**
     * Create login session
     */
    fun createLoginSession(username: String?, password: String?) {
        editor.putBoolean(IS_LOGIN, true)
        editor.putString(KEY_username, username)
        editor.putString(KEY_password, password)
        editor.commit()
    }

    fun checkLogin() {
        // Check login status
        if (!isLoggedIn) {
            // user is not logged in redirect him to Login Activity
            val i = Intent(_context, Long::class.java)
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Add new Flag to start new Activity
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // Staring Login Activity
            _context.startActivity(i)
        }
    }

    fun setToken(token: String?) {
        pref.edit().putString("token", token).commit()
    }

    fun getToken(): String? {
        return pref.getString("token", "")
    }

    fun setUsername(username: String?) {
        pref.edit().putString("name", username).commit()
    }

    fun getUsername(): String? {
        return pref.getString("name", "")
    }


    fun setid(id: String?) {
        pref.edit().putString("id", id).commit()
    }

    fun getid(): String? {
        return pref.getString("id", "")
    }

    fun setSubscribed(subscribeStatus: Boolean?) {
        pref.edit().putBoolean("subscribeStatus", subscribeStatus!!).commit()
    }

    fun getSubscribed(): Boolean? {
        return pref.getBoolean("subscribeStatus", false)
    }

    fun setFCMToken(fcmToken: String?) {
        pref.edit().putString("fcmToken", fcmToken!!).commit()
    }

    fun getFCMToken(): String? {
        return pref.getString("fcmToken", "")
    }


    fun setTheme(theme: String?) {
        pref.edit().putString("theme", theme).commit()
    }

    fun getTheme(): String? {
        return pref.getString("theme", "")
    }


    /**
     * Clear session details
     */
    fun logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear()
        editor.commit()

        // After logout redirect user to Loing Activity
        val i = Intent(_context, Login::class.java)
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Add new Flag to start new Activity
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        _context.startActivity(i)
    }

    // Get Login State
    val isLoggedIn: Boolean
        get() = pref.getBoolean(IS_LOGIN, false)

    companion object {
        private const val PREF_NAME = "NWS_Pref"
        private const val IS_LOGIN = "IsLoggedIn"

        //username
        const val KEY_username = "username"

        //password
        const val KEY_password = "password"
        const val KEY_batchcount = "batchcount"
        const val KEY_VENDOR = "key_vandor"
        const val KEY_ADDRESS = "key_address"
    }

    // Constructor
    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}