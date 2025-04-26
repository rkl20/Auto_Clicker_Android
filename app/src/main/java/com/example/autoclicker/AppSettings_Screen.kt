package com.example.autoclicker

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class AppSettings_Screen: BaseActivity() {
    private lateinit var overlayManager: CreateOverlay //instance of the new class
    //init layout and view
    private lateinit var buttonColorLayout: LinearLayout
    private lateinit var bgColorLayout: LinearLayout
    private lateinit var textColorLayout: LinearLayout
    private lateinit var buttonColorView: View
    private lateinit var bgColorView: View
    private lateinit var textColorView: View
    //lateinit graphic
    private lateinit var imageGraphic: ImageView

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "MyPrefs" // Name for SharedPreferences
    private val THEME_KEY = "theme" // Key to store the selected theme
    private val THEME_LIGHT = "light"
    private val THEME_DARK = "dark"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        enableEdgeToEdge()
        overlayManager = CreateOverlay.getInstance(this)
        //set view
        setContentView(R.layout.appsettings_screen)

        //get the graphic
        imageGraphic = findViewById(R.id.imageView)
        //get layout and view for color picker
        buttonColorLayout = findViewById(R.id.buttonColorLayout)
        bgColorLayout = findViewById(R.id.bgColorLayout)
        textColorLayout = findViewById(R.id.textColorLayout)
        buttonColorView = findViewById(R.id.buttonColorView)
        bgColorView = findViewById(R.id.bgColorView)
        textColorView = findViewById(R.id.textColorView)
        //get button
        val applyButton: Button = findViewById(R.id.applyButton)
        val permissionButton: Button = findViewById(R.id.permissionButton)
        val backButton: Button = findViewById(R.id.backButton)
        //get radio group
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)

        //call fun to change view color
        colorChanged()

        //set on click listener
        permissionButton.setOnClickListener {
            permissionButtonfun()
        }

        radioGroup.setOnCheckedChangeListener{group, checkedId ->
            when(checkedId){
                R.id.selectLightTheme -> {
                    Log.d("Theme","Theme will change to light")
                    saveTheme(THEME_LIGHT)
                }
                R.id.selectDarkTheme -> {
                    Log.d("Theme","Theme will change to dark")
                    saveTheme(THEME_DARK)
                }
            }
        }

        applyButton.setOnClickListener{
            recreate()
        }
        buttonColorLayout.setOnClickListener{
            colorPicker(buttonColorView) {color ->
                // Update the inner circle color permanently
                overlayManager.changeLayerDrawableColors(imageGraphic, "line", color)
            }
        }
        bgColorLayout.setOnClickListener{
            colorPicker(bgColorView) { color ->
                // Update the inner circle color permanently
                overlayManager.changeLayerDrawableColors(imageGraphic, "inner", color)
            }
        }
        textColorLayout.setOnClickListener{
            colorPicker(textColorView) { color ->
                // Update the inner circle color permanently
                overlayManager.changeLayerDrawableColors(imageGraphic, "text", color)
                val getTV = findViewById<TextView>(R.id.graphicText)
                getTV.setTextColor(color)
            }
        }
        backButton.setOnClickListener {
            backButtonfun()
        }
    }
    private fun colorPicker(colorView: View, onColorSelected: ((Int) -> Unit)? = null){
        val builder = ColorPickerDialog.Builder(this)
        builder.setTitle("Choose custom color!")
        builder.setPreferenceName("MyColorPickerDialog")
        builder.setPositiveButton(getString(R.string.confirm), ColorEnvelopeListener { envelope, fromUser ->
            val colorInt: Int = envelope.color
            val colorHex = envelope.hexCode
            Log.d("colopicker","Selected color: Int=$colorInt, Hex=$colorHex")
            colorView.setBackgroundColor(colorInt)
            onColorSelected?.invoke(colorInt)

        })
        builder.setNegativeButton(getString(R.string.cancel), DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss()
        })
        builder.show()
    }

    private fun colorChanged(){
        if(overlayManager.colorChange == 1){
            buttonColorView.setBackgroundColor(overlayManager.lineColor)
            bgColorView.setBackgroundColor(overlayManager.innerColor)
            textColorView.setBackgroundColor(overlayManager.textColor)
            val getTV = findViewById<TextView>(R.id.graphicText)
            getTV.setTextColor(overlayManager.textColor)
        }
    }

    //save the theme
    private fun saveTheme(theme: String) {
        val editor = sharedPreferences.edit()
        editor.putString(THEME_KEY, theme)
        editor.apply()
    }

    //button function
    private fun permissionButtonfun() {
        overlayManager.requestAccessibilityPermission()
    }


    private fun backButtonfun() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
