package com.example.autoclicker

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter

//store location of graphic
data class GraphicData(val x: Float, val y: Float)

//store graphicData which store location of graphic and timing of touches
data class AppData(
    val graphicPositions: List<GraphicData>,
    val timingList: MutableList<MutableList<Long>>,
)

//initiate this to get all the function
class DataManager(private val context: Context) {
    private val gson = Gson()
    fun saveAppData(appData: AppData, dataFileName: String) {
        // Use getExternalFilesDir(null) for app-specific external storage
        val directory = context.getExternalFilesDir(null)
        val file = File(directory, "$dataFileName.json")

        // Ensure the directory exists
        directory?.mkdirs()

        val jsonString = gson.toJson(appData)
        try {
            FileWriter(file).use { writer ->
                writer.write(jsonString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAppData(dataFileName: String): AppData? {
        // Use getExternalFilesDir(null) for app-specific external storage
        val directory = context.getExternalFilesDir(null)
        val file = File(directory, "$dataFileName.json")

        return if (file.exists()) {
            try {
                FileReader(file).use { reader ->
                    gson.fromJson(reader, AppData::class.java)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    fun renameAppData(oldDataFileName: String, newDataFileName: String) {
        // Use getExternalFilesDir(null) for app-specific external storage
        val directory = context.getExternalFilesDir(null)
        val oldFile = File(directory, "$oldDataFileName.json")
        val newFile = File(directory, "$newDataFileName.json")
        if (oldFile.exists()) {
            oldFile.renameTo(newFile)
            Log.d("exist","renamed")
        }else{
            Log.d("not exist","not renamed")
        }
    }


    fun deleteAppData(dataFileName: String) {
        // Use getExternalFilesDir(null) for app-specific external storage
        val directory = context.getExternalFilesDir(null)
        val file = File(directory, "$dataFileName.json")
        if (file.exists()) {
            file.delete()
            Log.d("exist","deleted")
        }else{
            Log.d("not exist","not deleted")
        }

    }
    fun getSavedFilesList(): List<File> {
        val filesDir = context.getExternalFilesDir(null)
        if (filesDir != null && filesDir.exists() && filesDir.isDirectory) {
            val fileList = filesDir.listFiles()
            if (fileList != null) {
                // Filter out non-json files, so only json file are shown
                return fileList.filter { it.isFile && it.name.endsWith(".json") }
            }
        }
        // Return an empty list if there are no files or if the directory doesn't exist
        return emptyList()
    }

    fun renameFile(newFileName: String, dataFileName: String) {
        // Use getExternalFilesDir(null) for app-specific external storage
        val directory = context.getExternalFilesDir(null)
        val oldFile = File(directory, "$dataFileName.json")
        val newFile = File(directory, "$newFileName.json")

    }

    fun debugFileContent() {
        val filesDir = context.getExternalFilesDir(null) // Get the app's app-specific external files directory
        if (filesDir != null && filesDir.exists() && filesDir.isDirectory) { // Check if the directory exists
            val fileList = filesDir.listFiles() // List all files in the directory
            if (fileList != null && fileList.isNotEmpty()) {
                Log.d("FileDebug", "Files in app's external storage:")
                for (file in fileList) {
                    if (file.isFile) {
                        Log.d("FileDebug", "  - ${file.name}:")
                        try {
                            val fileContent = file.readText() // Read the content of each file
                            Log.d("FileDebug", "    Content:\n$fileContent")
                        } catch (e: Exception) {
                            Log.e("FileDebug", "Error reading file ${file.name}: ${e.message}")
                        }
                    }
                }
            } else {
                Log.d("FileDebug", "No files found in app's external storage.")
            }
        } else {
            Log.e("FileDebug", "App's external storage directory does not exist or is not a directory.")
        }
    }
}