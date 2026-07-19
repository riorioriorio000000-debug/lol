package com.yourname.autoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class AutoClickerService : AccessibilityService() {

    var isRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private var clickX = 500f
    private var clickY = 1000f
    private val clickInterval = 1000L

    companion object {
        var instance: AutoClickerService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        isRunning = false
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        isRunning = false
    }

    fun toggleClicking() {
        if (isRunning) {
            stopClicking()
        } else {
            startClicking()
        }
    }

    fun startClicking() {
        isRunning = true
        clickLoop()
    }

    fun stopClicking() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    fun setClickTarget(x: Float, y: Float) {
        clickX = x
        clickY = y
    }

    private fun clickLoop() {
        if (!isRunning) return
        performClick(clickX, clickY)
        handler.postDelayed({ clickLoop() }, clickInterval)
    }

    private fun performClick(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val strokeDescription = GestureDescription.StrokeDescription(path, 0, 50)
        val gestureDescription = GestureDescription.Builder()
            .addStroke(strokeDescription)
            .build()
        dispatchGesture(gestureDescription, null, null)
    }
}
