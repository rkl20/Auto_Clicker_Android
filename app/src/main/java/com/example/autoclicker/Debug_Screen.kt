package com.example.autoclicker

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView

class Debug_Screen : BaseActivity() {
    private var posList = mutableListOf<Pair<Float, Float>>()
    private var timeList = mutableListOf<Long>()
    private lateinit var touchesText: TextView
    private lateinit var timeText: TextView
    private lateinit var blankText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_screen)
        touchesText = findViewById(R.id.touchesText)
        timeText = findViewById(R.id.timeText)
        blankText = findViewById(R.id.blankText)

        //when print button is clicked
        val printPosButton = findViewById<Button>(R.id.printPosButton)
        val printTimeButton = findViewById<Button>(R.id.printTimeButton)
        val clearButton = findViewById<Button>(R.id.clearButton)
        printPosButton.setOnClickListener {
            printPos()
        }
        printTimeButton.setOnClickListener {
            printTime()
        }
        clearButton.setOnClickListener {
            clear()
        }

    }

    //when touch event happens
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("Debug_Screen", "Touch down event")
                Log.d("x", "x: ${event.x}")
                Log.d("y", "y: ${event.y}")
                Log.d("time", "time: ${event.eventTime}")
                addPosAndTime(event.x, event.y, event.eventTime)
                getPosAndTime(posList, timeList)
            }
            /*MotionEvent.ACTION_UP -> {
                Log.d("Debug_Screen", "Touch up event")
                Log.d("x", "x: ${event.x}")
                Log.d("y", "y: ${event.y}")
                Log.d("time", "time: ${event.eventTime}")
            }*/
        }
        return true
    }

    //add pos and time to list
    private fun addPosAndTime(x: Float, y: Float, time: Long) {
        posList.add(Pair(x, y))
        timeList.add(time)
    }

    //get pos and time from list
    @SuppressLint("SetTextI18n")
    private fun getPosAndTime(
        posList: MutableList<Pair<Float, Float>>,
        timeList: MutableList<Long>
    ) {
        //check if list is not empty
        if (posList.isNotEmpty() and timeList.isNotEmpty()) {
            var x = posList.last().first
            var y = posList.last().second
            var time = timeList.last()
            touchesText.text = "Last Touches: x: $x, y: $y"
            timeText.text = "Last Time: $time"
            //check if list size is greater than 1 (if there is more than one touch)
            if (timeList.size > 1) {
                //get the time difference between the last two touches
                var listSize = timeList.size-1
                var timeDiff = timeList.last() - timeList[listSize - 1]
                timeText.append(", Time Diff: $timeDiff")

            }
        }
    }

    //print data to logcat
    private fun printPos() {
        blankText.text = "$posList"
    }
    private fun printTime() {
        blankText.text=""
        if (timeList.isNotEmpty()){
            if (timeList.size > 1) {
                for (i in 0 until timeList.size-1) {
                    var timeDiff = timeList[i + 1] - timeList[i]
                    blankText.append("${i+1} to ${i+2}: $timeDiff \n")
                }
            }
        }
    }
    private fun clear() {
        posList.clear()
        timeList.clear()
        touchesText.text = ""
        timeText.text = ""
        blankText.text = ""
    }
}

