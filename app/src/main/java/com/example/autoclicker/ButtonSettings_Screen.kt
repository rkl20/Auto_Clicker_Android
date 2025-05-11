package com.example.autoclicker

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
//color picker
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class ButtonSettings_Screen: BaseActivity() {
    private lateinit var overlayManager: OverlayManager //instance of the new class
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var linearLayout: LinearLayout
    //init layout and view
    private lateinit var buttonColorLayout: LinearLayout
    private lateinit var bgColorLayout: LinearLayout
    private lateinit var textColorLayout: LinearLayout
    private lateinit var buttonColorView: View
    private lateinit var bgColorView: View
    private lateinit var textColorView: View
    //lateinit button
    private lateinit var backButton: Button
    //lateinit graphic
    private lateinit var imageGraphic: ImageView
    //lateinit saveguard
    private var startTimeLowerLimit = 1L
    private var startTimeHigherLimit = 60000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayManager = OverlayManager.getInstance(this)
        enableEdgeToEdge()
        setContentView(R.layout.buttonsettings_screen)

        // Find the layout where you'll add the dropdowns
        linearLayout = findViewById(R.id.buttonSettingsLayout)
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
        backButton = findViewById(R.id.backButton)

        // Get the list
        val graphicsList = overlayManager.graphicOverlaysList
        val settingsList = overlayManager.settings2Dlist
        //Log.d("graphicList", "graphicList : $graphicsList")

        // Call the function to create the dropdowns
        createSettings(graphicsList, settingsList)

        //call fun to change view color
        colorChanged()

        //logging
        overlayManager.onButtonOverlayClosed = {
            Log.d("OverlayManager", "Overlay was closed")
        }
        //set listener when clicked
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
            backbuttonfun()
        }
    }

    private fun createSettings(graphicsList: List<View>, settingsList: MutableList<MutableList<Long>>) {
        if(graphicsList.isNotEmpty()){
            val textView: TextView = findViewById(R.id.NoGraphicTextView)
            textView.visibility = View.GONE
        }else{
            val textView: TextView = findViewById(R.id.NoGraphicTextView)
            textView.visibility = View.VISIBLE
        }
        // Create a settings for each graphic overlay
        for (i in graphicsList.indices) {
            // Create a TextView to label each dropdown
            val textView = TextView(this).apply {
                text = "Button ${i + 1}"
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {

                    setMargins(0, 0, 0, 8)
                }
            }
            // Create the EditText
            val editTextStartTime = EditText(this).apply {
                hint = "Enter time here"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                inputType = InputType.TYPE_CLASS_NUMBER
                // Set an initial value if needed
                setText("${settingsList[i][0]}")
            }
            val editTextDuration = EditText(this).apply {
                hint = "Enter time here"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                inputType = InputType.TYPE_CLASS_NUMBER
                // Set an initial value if needed
                setText("${settingsList[i][1]}")
            }

            // Store the EditText's text in the dropdownData map when it changes
            editTextStartTime.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && editTextStartTime.text.isNotEmpty()) {
                    val enteredText = editTextStartTime.text.toString().trim()
                    val textLong = enteredText.toLong()
                    if(textLong >= startTimeLowerLimit){
                        if(textLong < startTimeHigherLimit){
                            settingsList[i][0] = enteredText.toLong()
                        }else{
                            Toast.makeText(this, "Time is too Long", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this, "Time is too Short", Toast.LENGTH_SHORT).show()
                    }

                    //Log.d("DropDownData", "Dropdown data updated for graphic ${graphic.id}: $enteredText")
                }
            }
            editTextDuration.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && editTextDuration.text.isNotEmpty()) {
                    val enteredText = editTextDuration.text.toString().trim()
                    val textLong = enteredText.toLong()
                    if(textLong >= startTimeLowerLimit){
                        if(textLong < startTimeHigherLimit){
                            settingsList[i][0] = enteredText.toLong()
                        }else{
                            Toast.makeText(this, "Time is too Long", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this, "Time is too Short", Toast.LENGTH_SHORT).show()
                    }
                    //Log.d("DropDownData", "Dropdown data updated for graphic ${graphic.id}: $enteredText")
                }
            }

            // Add the label and dropdown to the layout
            linearLayout.addView(textView)
            linearLayout.addView(editTextStartTime)
            linearLayout.addView(editTextDuration)
        }
    }

    private fun backbuttonfun(){
        // Check for permission before showing
        if (overlayManager.checkOverlayPermission()) {
            Log.d("backbutton", "Overlay permission already granted")
            overlayManager.showOverlay()
        } else {
            Log.d("backbutton", "Overlay permission not granted, requesting...")
            //we call the permission here with the launcher
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            overlayPermissionLauncher.launch(intent)
        }
        finish()
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
            overlayManager.changeLayerDrawableColors(imageGraphic, "line", overlayManager.lineColor)
            overlayManager.changeLayerDrawableColors(imageGraphic, "inner", overlayManager.innerColor)
            overlayManager.changeLayerDrawableColors(imageGraphic, "text", overlayManager.textColor)
        }
    }


}