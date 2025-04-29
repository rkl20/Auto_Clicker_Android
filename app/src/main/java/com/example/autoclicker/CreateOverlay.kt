package com.example.autoclicker

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
//access for touch
import android.accessibilityservice.AccessibilityService
import android.graphics.Path
import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.graphics.drawable.LayerDrawable
import androidx.core.net.toUri
//timing
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import java.util.concurrent.CountDownLatch

//
//should change the name to overlay manager, because it is managing the overlay
class CreateOverlay(private val context: Context): BaseActivity() {

    // companion object to implement the singleton pattern, to give access to other activity
    companion object {
        @Volatile
        private var INSTANCE: CreateOverlay? = null

        fun getInstance(context: Context): CreateOverlay {
            return INSTANCE ?: synchronized(this) {
                val instance = CreateOverlay(context)
                INSTANCE = instance
                instance
            }
        }
    }
    //button and graphic overlay variable
    private lateinit var buttonOverlayView: FrameLayout
    lateinit var graphicOverlayView: FrameLayout
    //list of graphic overlays
    val graphicOverlaysList = mutableListOf<View>() // Changed type to View to accommodate TextView

    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private lateinit var fullScreenParams: WindowManager.LayoutParams

    private var buttonOverlayVisible = false
    private var graphicOverlayVisible = false

    var onButtonOverlayClosed: (() -> Unit)? = null
    private var onGraphicOverlayClosed: (() -> Unit)? = null
    // Counter to keep track of graphic order
    var graphicCounter = 1
    //graphic variable
    private var isGraphicTouchable = true
    //touch variable
    private var globalActions : AccessibilityService? = null
    //array for button settings
    val settings2Dlist: MutableList<MutableList<Long>> = mutableListOf()
    /* list[row][column]
    2d array for button settings row for amount,
    column1 for start time / delay,
    column2 for duration
     */
    //init shared layer drawable
    private val sharedLayerDrawable: LayerDrawable by lazy {
        ContextCompat.getDrawable(context, R.drawable.press_graphic) as LayerDrawable
    }
    //color change variable, 1 is changed, 0 is not changed
    var colorChange = 0
    var lineColor = ContextCompat.getColor(this.context, R.color.black)
    var innerColor = ContextCompat.getColor(this.context, R.color.blackAlpha_13)
    var textColor = ContextCompat.getColor(this.context, R.color.black)

    //var for default
    var defaultGraphicPosX: Float = 10f
    var defaultGraphicPosY: Float = 10f
    var defaultStartTime: Long = 0
    var defaultDuration: Long = 100



    //create the button overlay
    fun createOverlay() {
        if (!checkOverlayPermission()) {
            Log.e("OverlayManager", "Attempted to create overlay without permission!")
            return
        }

        // Inflate the XML layout
        val inflater = LayoutInflater.from(context)
        buttonOverlayView = inflater.inflate(R.layout.layout_overlay, null) as FrameLayout
        //Log.d("createOverlay", "buttonOverlayView: $buttonOverlayView")

        //create graphic overlay
        createGraphicOverlay()

        //size of graphic
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val desiredWidth = (100f * displayMetrics.density).toInt()//the original width will be multiplied by this number
        val desiredHeight = (100f * displayMetrics.density).toInt()//the original height will be multiplied by this number

        //find the button
        val addButton: Button = buttonOverlayView.findViewById(R.id.AddButton)
        val subButton: Button = buttonOverlayView.findViewById(R.id.SubButton)
        val startButton: Button = buttonOverlayView.findViewById(R.id.StartButton)
        val buttonSetting: Button = buttonOverlayView.findViewById(R.id.ButtonSettingsButton)
        val saveButton: Button = buttonOverlayView.findViewById(R.id.SaveButton)
        val loadButton: Button = buttonOverlayView.findViewById(R.id.loadButton)
        val closeButton: Button = buttonOverlayView.findViewById(R.id.closeButton)
        val minimizeButton: Button = buttonOverlayView.findViewById(R.id.minimizeButton)
        // Add a listener
        addButton.setOnClickListener {
            addbuttonfun(desiredWidth, desiredHeight, defaultGraphicPosX, defaultGraphicPosY, defaultStartTime, defaultDuration)
        }
        subButton.setOnClickListener {
            subbuttonfun()
        }
        startButton.setOnClickListener {
            startbuttonfun()
        }
        buttonSetting.setOnClickListener {
            buttonsettingsfun()
        }
        saveButton.setOnClickListener {
            savebuttonfun()
        }
        loadButton.setOnClickListener {
            loadbuttonfun()
        }
        closeButton.setOnClickListener {
            closebuttonfun()
        }
        minimizeButton.setOnClickListener {
            if(minimizeButton.text == context.getString(R.string.MinimizeButton)){
                minimizeButton.text = context.getString(R.string.ShowButton)
                minimizebuttonfun()
            }else if(minimizeButton.text == context.getString(R.string.ShowButton)){
                minimizeButton.text = context.getString(R.string.MinimizeButton)
                showButtonFun()
            }

        }

        //add graphic overlay view (the graphic that show pos of preses)
        // Make sure the graphicOverlayView is the first view to be added
        fullScreenParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            getOverlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE /*or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE*/,
            PixelFormat.TRANSLUCENT
        )
        fullScreenParams.gravity = Gravity.TOP or Gravity.START
        try {
            windowManager.addView(graphicOverlayView, fullScreenParams)
            graphicOverlayVisible = true
            Log.d("OverlayDebug", "graphicOverlayView added")
        } catch (e: WindowManager.BadTokenException) {
            Log.e("MainActivity", "Error adding overlay view: ${e.message}")
        }

        //add button overlay
        try {
            val layoutParameter = layoutParam(110f, 400f)//currently height doesn't matter, always wrap content
            windowManager.addView(buttonOverlayView, layoutParameter)
            buttonOverlayVisible = true
            Log.d("OverlayDebug", "buttonOverlayView added")
        } catch (e: WindowManager.BadTokenException) {
            Log.e("MainActivity", "Error adding overlay view: ${e.message}")
        }
        Log.d("OverlayDebug", "createOverlay() finished")
    }

    //hide overlay
    fun hideOverlay() {
        if(::buttonOverlayView.isInitialized && ::graphicOverlayView.isInitialized) {
            buttonOverlayView.visibility = View.GONE
            graphicOverlayView.visibility = View.GONE
            buttonOverlayVisible = false
            graphicOverlayVisible = false
            Log.d("OverlayDebug", "overlay hidden")
        }else{
            Log.d("OverlayDebug", "Cannot hide overlay, it has not been initialized")
        }
        Log.d("OverlayDebug", "hideOverlay() finished")
    }

    //show overlay after hidden
    fun showOverlay(){
        Log.d("showOverlay()", "Showing overlay")
        if(::buttonOverlayView.isInitialized && ::graphicOverlayView.isInitialized) {
            buttonOverlayView.visibility = View.VISIBLE
            graphicOverlayView.visibility = View.VISIBLE
            buttonOverlayVisible = true
            graphicOverlayVisible = true
            Log.d("OverlayDebug", "overlay shown")
        }else{
            Log.d("OverlayDebug", "Cannot show overlay, it has not been initialized")
        }
        Log.d("OverlayDebug", "showOverlay() finished")
    }

    //create graphic overlay
    private fun createGraphicOverlay(){
        // Initialize graphicOverlayView
        graphicOverlayView = FrameLayout(context)
        graphicOverlayView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        graphicOverlayView.setBackgroundColor(ContextCompat.getColor(context,R.color.overlayBG))//set background color for graphic overlay, transparent if want not seen
    }
    //layout param for button overlay
    private fun layoutParam(desiredWidthDp:Float,desiredHeightDp:Float): WindowManager.LayoutParams {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val desiredWidth = (desiredWidthDp * displayMetrics.density).toInt()
        val desiredHeight = (desiredHeightDp * displayMetrics.density).toInt()

        // Get screen dimensions
        //val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Calculate x and y coordinates for right placement
        //val x = screenWidth - desiredWidth
        val y = (screenHeight - desiredHeight) / 2
        val layoutParams = WindowManager.LayoutParams(
            desiredWidth, // Set initial width here
            //desiredHeight, // Set initial height here
            WindowManager.LayoutParams.WRAP_CONTENT,
            getOverlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        layoutParams.x = 0 //set to 0 to match with the gravity
        layoutParams.y = y
        return layoutParams
    }

    //get overlay type of phone for creating button and graphic overlay
    fun getOverlayType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    // Function to set the touch-ability of the overlay
    fun setOverlayTouchable(isTouchable: Boolean, onComplete: () -> Unit) {
        Log.d("OverlayTouchable", "Setting overlay touchable to: $isTouchable")
        //create parameters for the overlay
        if (::fullScreenParams.isInitialized) {
            fullScreenParams.flags = if (isTouchable) {
                fullScreenParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()//we want to remove the flag
            } else {
                fullScreenParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE//we want to add the flag
            }
            try {
                windowManager.updateViewLayout(graphicOverlayView, fullScreenParams)
                // Wait for the UI to update (short delay)
                Handler(Looper.getMainLooper()).postDelayed({
                    isGraphicTouchable = isTouchable
                    Log.d("OverlayTouchable", "Overlay touchable state set to: $isGraphicTouchable")
                    onComplete()
                }, 50) // Adjust delay as needed (50ms is a good starting point)
            } catch (e: IllegalArgumentException) {
                Log.e("setOverlayTouchable", "Error updating graphic overlay view: ${e.message}")
                onComplete()//call onComplete even if there is an error
            }

        }else{
            onComplete()//call onComplete even if fullScreenParams is not initialize
        }

    }

    //simulate screen touches
    private fun dispatchTouchEvent(x: Float, y: Float, ST: Long, DUR: Long, latch: CountDownLatch) {
        Log.d("dispatchTouchEvent", "dispatchTouchEvent called: x=$x, y=$y")
        //check if globalAction variable is null or not
        if (globalActions == null) {
            Log.e("dispatchTouchEvent", "Accessibility service is not connected!")
            return
        }

        // Test here
        //Log.d("dispatchTouchEvent", "globalAction: $globalActions")
        val path = Path()
        path.moveTo(x, y)
        //path.lineTo(x+0f,y+100f)//delete if done

        // New: Log the path information
        //Log.d("dispatchTouchEvent", "Path is empty: ${path.isEmpty}")

        val gesture_builder = GestureDescription.Builder()
        val touch_description = GestureDescription.StrokeDescription(path, ST, DUR)
        // Test here
        //Log.d("dispatchTouchEvent", "strokeDescription: $touch_description")
        val gesture_builder_withtouch = gesture_builder.addStroke(touch_description).build()

        // Test here
        //Log.d("dispatchTouchEvent", "gestureDescription: $gesture_builder_withtouch")
        val callback = object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
                Log.d("dispatchTouchEvent", "Gesture completed.")
                latch.countDown() // Decrement the latch when gesture is done
            }
            override fun onCancelled(gestureDescription: GestureDescription) {
                super.onCancelled(gestureDescription)
                Log.d("dispatchTouchEvent", "Gesture cancelled.")
                latch.countDown() // Decrement the latch when gesture is done
            }
        }
        globalActions?.dispatchGesture(gesture_builder_withtouch, callback, null)
    }
    /*fun performGlobalAction(action: Int) : Boolean{
        return try {
            val result = globalActions.performGlobalAction(action)
            result
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }*/

    fun onServiceConnected(service: AccessibilityService){
        Log.d("OverlayDebug","onServiceConnected in CreateOverlay called")
        globalActions = service
    }
    //funtion if permission of screen touches true and not true
    fun requestAccessibilityPermission(): Boolean {
        // Check if Accessibility permission is granted
        if (!checkAccessibilityServiceEnabled()) {
            // If not, open the accessibility settings to request it
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Log.d("AccessibilityPermission", "Accessibility permission not granted, opening settings")
            return false // Permission is not granted
        }
        Log.d("AccessibilityPermission", "Accessibility permission granted")
        return true // Permission is granted
    }
    //the function that checks if accessibility permission is granted
    fun checkAccessibilityServiceEnabled(): Boolean {
        val service = context.packageName + "/" + OverlayAccessibilityService::class.java.canonicalName
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )

        return enabledServicesSetting?.contains(service) == true
    }
    //Check android version to see if overlay permission is granted by default or not, if new version check the settings to see its granted or not, then get request permission
    fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // On older versions, it's granted by default
        }
    }

    //request the permission if not already granted
    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri()
            )
        }
    }
    //start new activity in this case buttonsettings_screen
    fun startBSSActivity(){
        val intent = Intent(context, ButtonSettings_Screen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag!
        context.startActivity(intent)
        //Log.d("startNewActivity", "startNewActivity() finished")
    }

    fun changeLayerDrawableColors(imageView: ImageView, whichLayer: String, color: Int) {
        val layerDrawable = sharedLayerDrawable
        if(whichLayer == "line"){
            val layerOuter = layerDrawable.findDrawableByLayerId(R.id.outerCircle)?.mutate()
            val layerLine = layerDrawable.findDrawableByLayerId(R.id.line)?.mutate()
            val layerRec = layerDrawable.findDrawableByLayerId(R.id.square)?.mutate()
            if (layerOuter != null && layerLine != null && layerRec != null) {
                DrawableCompat.setTint(layerOuter,color)
                DrawableCompat.setTint(layerLine,color)
                DrawableCompat.setTint(layerRec,color)
            }
            lineColor = color
        }else if(whichLayer == "inner"){
            val layerInner = layerDrawable.findDrawableByLayerId(R.id.innerCircle)?.mutate()
            if (layerInner != null) {
                DrawableCompat.setTint(layerInner,color)
            }
            innerColor = color
        }else if(whichLayer == "text"){
            // Find the TextView within the overlayContainer
            graphicOverlaysList.forEach { overlayContainer ->
                if (overlayContainer is ViewGroup) { // Check if overlayContainer is a ViewGroup
                    val textView = overlayContainer.getChildAt(1) as? TextView // Assuming TextView is the second child
                    if (textView != null) {
                        textView.setTextColor(color)
                    }
                }
            }
            textColor = color
        }

        imageView.setImageDrawable(layerDrawable)
        colorChange = 1
    }

    //button fun
    fun addbuttonfun(desiredWidth: Int, desiredHeight: Int, GraphicPosX: Float, GraphicPosY: Float, ST: Long, DUR: Long){
        // create and customize graphic overlay
        val overlayContainer = FrameLayout(context) // Create a container for image and text
        // Create the ImageView
        val overlay = ImageView(context).apply {
            setImageDrawable(sharedLayerDrawable)
            layoutParams = RelativeLayout.LayoutParams(
                desiredWidth,
                desiredHeight
            )
            contentDescription = "Graphic Overlay"
            id = View.generateViewId() // Generate an ID for the ImageView
        }

        // Create the TextView
        val orderText = TextView(context).apply {
            text = graphicCounter.toString()
            setTextColor(textColor)
            textSize = 35f

            // Layout parameters to position the text relative to the image
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_TOP, overlay.id) // Align top with the image
                addRule(RelativeLayout.ALIGN_LEFT, overlay.id) // Align left with the image
                //val margin = (10 * context.resources.displayMetrics.density).toInt()
                setMargins(30, 5, 0, 0) // Set margins: left, top, right, bottom
            }
        }

        overlayContainer.addView(overlay) // Add the image to the container
        overlayContainer.addView(orderText) // Add the text to the container

        // set position
        Log.d("addbutton", "GraphicPosX: $GraphicPosX, GraphicPosY: $GraphicPosY")
        overlayContainer.x = GraphicPosX//(0..500).random().toFloat()
        overlayContainer.y = GraphicPosY//(0..500).random().toFloat()
        /*overlayContainer.x = 450f
        overlayContainer.y = 1000f*/

        overlayContainer.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        // Touch and drag logic for the container
        overlayContainer.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val layoutParams = view.layoutParams as FrameLayout.LayoutParams
                    val initialX = layoutParams.leftMargin.toFloat()
                    val initialY = layoutParams.topMargin.toFloat()
                    view.tag = Pair(initialX - event.rawX, initialY - event.rawY)
                }

                MotionEvent.ACTION_MOVE -> {
                    val tag = view.tag as? Pair<Float, Float>
                    if (tag != null) {
                        val (offsetX, offsetY) = tag
                        val x = event.rawX + offsetX
                        val y = event.rawY + offsetY
                        val layoutParams = view.layoutParams as FrameLayout.LayoutParams
                        layoutParams.leftMargin = x.toInt()
                        layoutParams.topMargin = y.toInt()
                        view.layoutParams = layoutParams
                    }
                }

                MotionEvent.ACTION_UP -> {
                    view.tag = null
                }
            }
            true
        }
        graphicOverlayView.addView(overlayContainer)
        graphicOverlaysList.add(overlayContainer)
        graphicCounter++
        //add row to list
        settings2Dlist.add(mutableListOf(ST, DUR))//should change to var outside of fun so changing it is easier
        Log.d("addrow", "settings2Dlist: $settings2Dlist")
        /*Log.d("graphicOverlaysList x", "${graphicOverlaysList[0].x}")
        val location = IntArray(2)
        graphicOverlaysList[0].getLocationOnScreen(location)
        Log.d("x on screen", "${location[0].toFloat()}")
        Log.d("graphicOverlaysList y", "${graphicOverlaysList[0].y}")
        Log.d("y on screen", "${location[1].toFloat()}")*/
    }
    private fun subbuttonfun(){
        if (graphicOverlaysList.isNotEmpty()) {
            //remove from list
            val lastOverlay = graphicOverlaysList.removeAt(graphicOverlaysList.lastIndex)
            //remove from view
            graphicOverlayView.removeView(lastOverlay)
            //if graphic counter 1 dont reduce, because when remove no graphic exist so when re added it will still be one
            if(graphicCounter>=2){
                graphicCounter--
            }
        }
        //remove row from list
        if(settings2Dlist.isNotEmpty()) {
            settings2Dlist.removeAt(settings2Dlist.lastIndex)
            //Log.d("removerow", "settings2Dlist: ${settings2Dlist}")
        }
    }
    private fun startbuttonfun(){
        if (graphicOverlaysList.isNotEmpty()) {
            //fun(){inside this is code that we want to run after oncomplete()}
            graphicOverlayView.setBackgroundColor(ContextCompat.getColor(context,R.color.invisible))
            setOverlayTouchable(false){ // we set the overlay non touchable, and when it's done, we continue
                Thread {
                    graphicOverlaysList.forEachIndexed { index, graphic ->
                        val latch = CountDownLatch(1) // Initialize latch for each gesture
                        //make it non touchable before we get the position

                        val location = IntArray(2)
                        graphic.getLocationOnScreen(location)
                        val x = location[0].toFloat() + (graphic.width / 2)
                        val y = location[1].toFloat() + (graphic.height / 2)
                        // Dispatch on Main Thread
                        Handler(Looper.getMainLooper()).post{
                            dispatchTouchEvent(x, y,settings2Dlist[index][0],settings2Dlist[index][1],latch)
                            Log.d("Start Button", "touch")
                            Log.d("Pos and Time", "x: $x, y: $y, ST: ${settings2Dlist[index][0]}, DUR: ${settings2Dlist[index][1]}")
                        }
                        //wait on a separate thread
                        try {
                            latch.await() // Wait here until latch.countDown() is called
                        } catch (e: InterruptedException) {
                            Log.e("dispatchTouchEvent", "Interrupted while waiting for gesture to complete: ${e.message}")
                        }
                    }
                    // Back to the main thread to unlock
                    Handler(Looper.getMainLooper()).post {
                        Log.d("Start Button", "touches done")
                        graphicOverlayView.setBackgroundColor(ContextCompat.getColor(context,R.color.overlayBG))
                        setOverlayTouchable(true){}
                    }
                }.start()
            }

        } else {
            Log.d("Start Button","no graphic to simulate touch")
        }
    }
    private fun buttonsettingsfun(){
        hideOverlay()
        startBSSActivity()
    }
    private fun savebuttonfun(){
        //check if empty dont execute code
        if(graphicOverlaysList.isNotEmpty()){
            Log.d("savebutton","not empty")
            //set dataM to access function
            val dataM = DataManager(context)
            val fileList = dataM.getSavedFilesList()
            var numberofFiles = fileList.size
            var newFileName = "profile$numberofFiles"
            Log.d("savebutton","numberofFiles: $numberofFiles")
            //check the name, if exist -> change
            var fileNameSame = true
            while(fileNameSame) {
                fileNameSame = false
                Log.d("fileNameSame", "fileNameSame: $fileNameSame")
                for (File in dataM.getSavedFilesList()) {
                    if (File.nameWithoutExtension == newFileName) {
                        Log.d("FileNameWithout", "FileNameWithout: ${File.nameWithoutExtension}")
                        Log.d("newFileName", "newFileName: $newFileName")
                        numberofFiles++
                        newFileName = "profile$numberofFiles"
                        Log.d("fileNameSame", "add 1 newFileName: $newFileName")
                        fileNameSame = true
                        break
                    }
                }
            }
            //get location of graphic based on graphicOverlayList (the view not the screen)
            val pos: List<GraphicData> = graphicOverlaysList.map { graphic ->
                GraphicData(graphic.x, graphic.y)
            }
            //save it
            val saveAppData = AppData(pos, settings2Dlist)
            dataM.saveAppData(saveAppData, newFileName)
            Log.d("savebutton","saveAppData: $saveAppData")
            //Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
        }else{
            //Toast.makeText(context, "No graphic overlays to save", Toast.LENGTH_SHORT).show()
            Log.d("savebutton","no graphic overlays to save")
        }
    }
    private fun loadbuttonfun(){
        //start save file screen
        val intent = Intent(context, SaveFile_Screen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag!
        context.startActivity(intent)
        //hide overlay
        hideOverlay()
    }

    private fun closebuttonfun(){
        hideOverlay()
        graphicCounter=1
        onButtonOverlayClosed?.invoke()
        onGraphicOverlayClosed?.invoke()
        graphicOverlaysList.clear()
    }
    private fun minimizebuttonfun(){
        //make view invisible except minimize button (should i change it to show show button? rather than change the text?)
        val buttonLayout = buttonOverlayView.findViewById<ConstraintLayout>(R.id.ButtonLayout)
        for (i in 0 until buttonLayout.childCount) {
            val child = buttonLayout.getChildAt(i)
            if(child.id != R.id.minimizeButton){
                child.visibility = View.GONE
            }else{
                child.visibility = View.VISIBLE
            }
        }
        graphicOverlayView.visibility = View.GONE
        graphicOverlayVisible = false
    }

    private fun showButtonFun(){
        //show button
        val buttonLayout = buttonOverlayView.findViewById<ConstraintLayout>(R.id.ButtonLayout)
        for (i in 0 until buttonLayout.childCount) {
            val child = buttonLayout.getChildAt(i)
            child.visibility = View.VISIBLE
            }
        graphicOverlayView.visibility = View.VISIBLE
        graphicOverlayVisible = true
    }
}