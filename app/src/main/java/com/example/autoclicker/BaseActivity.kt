package com.example.autoclicker

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
//extend to this to get all function base activity
open class BaseActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "MyPrefs"
    private val THEME_KEY = "theme"
    private val THEME_LIGHT = "light"
    private val THEME_DARK = "dark"
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        applySavedTheme() // Apply the theme before the layout is inflated
        super.onCreate(savedInstanceState)
    }

    private fun applySavedTheme() {
        val savedTheme = sharedPreferences.getString(THEME_KEY, THEME_LIGHT) // Default to light theme
        when (savedTheme) {
            THEME_LIGHT -> {
                setTheme(R.style.Base_Theme_AutoClicker_Light)
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            THEME_DARK -> {
                setTheme(R.style.Base_Theme_AutoClicker_Dark)
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    fun setPreviousActivity(prevAct: String?){
        val app = application as MyApp
        app.previousActivity = prevAct
        Log.d("fromPreviousActivity", "fromPreviousActivity: $app.previousActivity")
    }
}