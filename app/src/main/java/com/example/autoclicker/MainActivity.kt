package com.example.autoclicker

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge //for screen
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat //for screen
import android.content.Intent //for getting pos
import android.provider.Settings
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher //perm
import androidx.activity.result.contract.ActivityResultContracts//perm

//currently in the code while checking permission it will log it, delete when done
class MainActivity : BaseActivity() {
    private lateinit var overlayManager: CreateOverlay //instance of the new class
    private lateinit var showOverlayLauncher: ActivityResultLauncher<Intent> //Create the activity result launcher
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent> // Initialize the launcher for the permission
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayManager = CreateOverlay.getInstance(this) // Pass 'this' (MainActivity) as the context
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        showOverlayLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //show the overlay again
                overlayManager.createOverlay()
            }
        }
        // Initialize the launcher
        overlayPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (overlayManager.checkOverlayPermission()) {
                Log.d("MainActivity", "Overlay permission granted after request")
                overlayManager.createOverlay() //we can now access it because it is no longer in a local function
            } else {
                Log.d("MainActivity", "Overlay permission still denied after request")
                // Handle the case where the user denied the permission.
                // You can show a message or disable overlay functionality
            }
        }
        //logging
        overlayManager.onButtonOverlayClosed = {
            Log.d("OverlayManager", "Overlay was closed")
        }
        val savefilebutton: Button = findViewById(R.id.SaveFileButton)
        savefilebutton.setOnClickListener {
            saveFileButtonFun()
        }
        val appsettingsbutton: Button = findViewById(R.id.AppSettingsButton)
        appsettingsbutton.setOnClickListener {
            appSettingsButtonFun()
        }
        // Set up the toggle button to show/hide the overlay
        //change from toggle to just show
        val openoverlaystartButton: Button = findViewById(R.id.OpenOverlayStartButton)
        openoverlaystartButton.setOnClickListener {
            openOverlay()
        }
        val debugButton: Button = findViewById(R.id.debugButton)
        debugButton.setOnClickListener {
            debugButtonFun()
        }

    }

    fun saveFileButtonFun(){
        setPreviousActivity("MainActivity")
        startActivity(Intent(this, SaveFile_Screen::class.java))
        finish()
    }
    fun appSettingsButtonFun(){
        startActivity(Intent(this, AppSettings_Screen::class.java))
        finish()
    }
    fun openOverlay() {
        //check for overlay permission
        if (!overlayManager.checkOverlayPermission()) {
            overlayManager.requestOverlayPermission()
        } else {
            //check for accessibility permission
            if(overlayManager.checkAccessibilityServiceEnabled()){
                //start overlay
                setPreviousActivity(null)
                overlayManager.createOverlay()
                finish()
            }else{
                overlayManager.requestAccessibilityPermission()
            }
        }
    }
    private fun debugButtonFun(){
        //check for overlay permission
        if (!overlayManager.checkOverlayPermission()) {
            Log.d("MainActivity", "Overlay permission not granted")
            overlayManager.requestOverlayPermission()
        } else {
            //check for accessibility permission
            Log.d("MainActivity", "Overlay permission granted")
            if(overlayManager.checkAccessibilityServiceEnabled()){
                //start overlay
                Log.d("MainActivity", "Accessibility permission granted")
                setPreviousActivity(null)
                overlayManager.createOverlay()
                startActivity(Intent(this, Debug_Screen::class.java))
                finish()
            }else{
                Log.d("MainActivity", "Accessibility permission not granted")
                overlayManager.requestAccessibilityPermission()
            }
        }
    }
}