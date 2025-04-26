package com.example.autoclicker

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import java.io.File

class SaveFile_Screen: BaseActivity() {
    private val dataManager = DataManager(this)
    private val overlayManager = CreateOverlay.getInstance(this)
    //Keep track of the selected view
    private var selectedTextView: TextView? = null
    private lateinit var saveFileSelected: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.savefile_screen)
        //inflate the save file view
        createSaveView()

        //set var
        //set up the button
        val backButton: Button = findViewById(R.id.backButton)
        val selectButton: Button = findViewById(R.id.selectButton)
        val renameButton: Button = findViewById(R.id.renameButton)
        val deleteButton: Button = findViewById(R.id.deleteButton)

        //click listener for the button
        backButton.setOnClickListener {
            backButtonFun()
        }
        selectButton.setOnClickListener {
            selectButtonFun()
        }
        renameButton.setOnClickListener {
            renameButtonFun()
        }
        deleteButton.setOnClickListener {
            deleteButtonFun()
        }

    }

    private fun createSaveView(){
        val dataM = DataManager(this)
        val fileList = dataM.getSavedFilesList()
        if (fileList.isNotEmpty()) {
            val saveFileLayout: LinearLayout = findViewById(R.id.saveFileLayout)
            val textView: TextView = findViewById(R.id.noSaveFileTextView)
            textView.visibility = View.GONE
            for (file in fileList) {
                val fileName = file.nameWithoutExtension // Remove the file extension
                val fileNameVIew = TextView(this).apply {
                    text = fileName
                    textSize = 18f
                    isClickable = true
                    isFocusable = true
                    background = getDrawable(R.drawable.border)
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(10)
                    }
                    setOnClickListener{
                        // Clear the background of the previously selected TextView
                        selectedTextView?.let {
                            it.setTextColor(ContextCompat.getColor(context, R.color.black))
                            it.background = getDrawable(R.drawable.border)
                        }
                        // Update the selected TextView
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                        background = getDrawable(R.drawable.border_selected)
                        // Update the saveFileSelected variable
                        selectedTextView = this
                        saveFileSelected = fileName
                        Log.d("saveFileSelected", "saveFileSelected: $saveFileSelected")
                    }
                }
                saveFileLayout.addView(fileNameVIew)
            }
        }else{
            val textView: TextView = findViewById(R.id.noSaveFileTextView)
            textView.visibility = View.VISIBLE
        }
    }

    private fun backButtonFun(){
        val app = application as MyApp
        if(app.previousActivity=="MainActivity"){
            startActivity(Intent(this, MainActivity::class.java))
            Log.d("app.previousActivity", "app.previousActivity: $app.previousActivity")
        }else{
            overlayManager.showOverlay()
            Log.d("app.previousActivity", "app.previousActivity: $app.previousActivity")
        }
        finish()
    }

    private fun selectButtonFun(){
        if (::saveFileSelected.isInitialized) {
            if (!overlayManager.checkOverlayPermission()) {
                overlayManager.requestOverlayPermission()
            } else {
                //check for accessibility permission
                if(overlayManager.checkAccessibilityServiceEnabled()){
                    val app = application as MyApp
                    if(app.previousActivity=="MainActivity"){
                        overlayManager.createOverlay()
                        Log.d("app.previousActivity", "app.previousActivity: $app.previousActivity")
                    }else{
                        overlayManager.showOverlay()
                        Log.d("app.previousActivity", "app.previousActivity: $app.previousActivity")
                    }
                    setPreviousActivity("SaveFile_Screen")
                    val saveFileNames = saveFileSelected
                    val saveFileDataManager = DataManager(this)
                    val loadedSaveFile = saveFileDataManager.loadAppData(saveFileNames)
                    Log.d("loadedSaveFile", "loadedSaveFile: $loadedSaveFile")
                    if (loadedSaveFile != null) {
                        // clear existing data
                        overlayManager.graphicOverlaysList.clear()
                        overlayManager.graphicOverlayView.removeAllViews()
                        overlayManager.graphicCounter=1
                        overlayManager.settings2Dlist.clear()
                        // Populate graphicOverlaysList and settings2Dlist from loadedData
                        for ((index, graphicData) in loadedSaveFile.graphicPositions.withIndex()) {
                            // add graphic overlay
                            overlayManager.addbuttonfun( (100f * this.resources.displayMetrics.density).toInt(), (100f * this.resources.displayMetrics.density).toInt(), graphicData.x, graphicData.y, loadedSaveFile.timingList[index][0], loadedSaveFile.timingList[index][1])
                        }
                    }
                    finish()
                }else{
                    overlayManager.requestAccessibilityPermission()
                }
            }
        }else{
            Toast.makeText(this, "No save file selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renameButtonFun(){//not done
        if (::saveFileSelected.isInitialized) {
            // Create an AlertDialog for the user to input the new file name
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Rename Save File")

            // Add an EditText for the user to input the new name
            val input = EditText(this)
            builder.setView(input)

            // Set up the OK button
            builder.setPositiveButton("OK") { _, _ ->
                val newFileName = input.text.toString()
                if (newFileName.isEmpty()) {
                    Toast.makeText(this, "File name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newFileName == saveFileSelected){
                    Toast.makeText(this, "New file name cannot be the same as the old file name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                //rename file
                val dataM = DataManager(this)
                dataM.renameAppData(saveFileSelected, newFileName)
                //refresh the screen
                deleteSaveViewExcept(R.id.noSaveFileTextView)
                createSaveView()
            }

            // Set up the Cancel button
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            // Show the AlertDialog
            builder.show()
        }else{
            Toast.makeText(this, "No save file selected", Toast.LENGTH_SHORT).show()
        }
    }
    private fun deleteButtonFun(){
        if(::saveFileSelected.isInitialized) {
            //create alert dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Save File")
            builder.setMessage("Are you sure you want to delete $saveFileSelected?")
            builder.setPositiveButton("DELETE") { _, _ ->
                //delete file
                val saveFileDataManager = DataManager(this)
                saveFileDataManager.deleteAppData(saveFileSelected)
                Toast.makeText(this, "File deleted successfully", Toast.LENGTH_SHORT).show()
                //refresh the screen
                deleteSaveViewExcept(R.id.noSaveFileTextView)
                createSaveView()
            }
            builder.setNegativeButton("CANCEL") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }else{
            Toast.makeText(this, "No save file selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteSaveViewExcept(viewID: Int){
        val saveLayout = findViewById<LinearLayout>(R.id.saveFileLayout)
        for (i in saveLayout.childCount - 1 downTo 0) {
            val childView = saveLayout.getChildAt(i)
            if (childView.id != viewID) {
                saveLayout.removeViewAt(i)
            }
        }
    }


}