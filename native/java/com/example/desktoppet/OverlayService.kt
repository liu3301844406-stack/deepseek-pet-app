package com.example.desktoppet

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.webkit.WebView
import androidx.core.app.NotificationCompat
import kotlin.math.abs

class OverlayService : Service() {

    companion object {
        var instance: OverlayService? = null
        const val CHANNEL_ID = "desktop_pet_channel"
        const val NOTIFICATION_ID = 1001
        const val PET_SIZE = 280
        const val CHAT_WIDTH = 900
        const val CHAT_HEIGHT = 1600
    }

    private lateinit var windowManager: WindowManager
    private lateinit var webView: WebView
    private lateinit var params: WindowManager.LayoutParams
    private var isExpanded = false

    private var lastX = 0f
    private var lastY = 0f
    private var downX = 0f
    private var downY = 0f
    private val TOUCH_SLOP = 12f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        setupWebView()
        addOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun setupWebView() {
        webView = WebView(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            loadUrl("https://localhost/index.html?mode=pet")
        }
    }

    private fun addOverlay() {
        params = WindowManager.LayoutParams(
            PET_SIZE, PET_SIZE,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 300
        }

        windowManager.addView(webView, params)

        webView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    lastX = event.rawX
                    lastY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - lastX
                    val dy = event.rawY - lastY
                    if (abs(event.rawX - downX) > TOUCH_SLOP || abs(event.rawY - downY) > TOUCH_SLOP) {
                        params.x = (params.x + dx).toInt()
                        params.y = (params.y + dy).toInt()
                        windowManager.updateViewLayout(webView, params)
                        lastX = event.rawX
                        lastY = event.rawY
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun expand() {
        if (isExpanded) return
        isExpanded = true
        params.width = CHAT_WIDTH
        params.height = CHAT_HEIGHT
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        windowManager.updateViewLayout(webView, params)
        webView.post { webView.evaluateJavascript("window.__desktopPetSetMode('chat')", null) }
    }

    fun collapse() {
        if (!isExpanded) return
        isExpanded = false
        params.width = PET_SIZE
        params.height = PET_SIZE
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        windowManager.updateViewLayout(webView, params)
        webView.post { webView.evaluateJavascript("window.__desktopPetSetMode('pet')", null) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::webView.isInitialized) {
            windowManager.removeView(webView)
            webView.destroy()
        }
        instance = null
    }

    private fun buildNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DeepSeek大肥鱼")
            .setContentText("桌宠正在你的桌面上")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "桌面宠物",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持桌宠悬浮窗运行"
                setSound(null, null)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
