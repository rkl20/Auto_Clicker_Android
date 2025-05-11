package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class OverlayAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        //Log.d("OverlayAccessibility", "onAccessibilityEvent: $event")
    }

    override fun onInterrupt() {
        //Log.d("OverlayAccessibility", "onInterrupt")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        //Log.d("OverlayAccessibility", "onServiceConnected")
        // Configure the type of events you want to receive

        // Notify CreateOverlay of the connection
        OverlayManager.getInstance(this).onServiceConnected(this)
    }
}