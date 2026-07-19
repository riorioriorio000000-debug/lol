package com.yourname.autoclicker

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.tvStatus)
        val btnToggle = findViewById<Button>(R.id.btnToggle)
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)

        // Check and request SYSTEM_ALERT_WINDOW permission
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))
            startActivity(intent)
        }

        btnAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        btnToggle.setOnClickListener {
            if (isAccessibilityServiceEnabled()) {
                AutoClickerService.instance?.toggleClicking()
                statusText.text = if (AutoClickerService.instance?.isRunning == true)
                    "حالة النقر: يعمل ✅"
                else
                    "حالة النقر: متوقف ⏸️"
            } else {
                Toast.makeText(this, "فعّل خدمة إمكانية الوصول أولاً", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
        }

        btnOverlay.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                val intent = Intent(this, OverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            } else {
                Toast.makeText(this, "امنح إذن الظهور فوق التطبيقات أولاً", Toast.LENGTH_LONG).show()
                val overlayIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
                startActivity(overlayIntent)
            }
        }

        updateStatus(statusText)
    }

    override fun onResume() {
        super.onResume()
        val statusText = findViewById<TextView>(R.id.tvStatus)
        updateStatus(statusText)
    }

    private fun updateStatus(statusText: TextView) {
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        val overlayEnabled = Settings.canDrawOverlays(this)
        statusText.text = buildString {
            append("إمكانية الوصول: ${if (accessibilityEnabled) "✅ مفعّلة" else "❌ غير مفعّلة"}\n")
            append("الظهور فوق التطبيقات: ${if (overlayEnabled) "✅ مسموح" else "❌ غير مسموح"}\n")
            append("حالة النقر: ${if (AutoClickerService.instance?.isRunning == true) "يعمل ✅" else "متوقف ⏸️"}")
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { it.id.contains(packageName) }
    }
}
