package com.example.desktoppet;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.webkit.WebView;
import androidx.core.app.NotificationCompat;
import android.app.Notification;

public class OverlayService extends Service {
    public static OverlayService instance = null;
    private final String CHANNEL_ID = "desktop_pet_channel";
    private final int NOTIFICATION_ID = 1001;
    private final int PET_SIZE = 280;
    private final int CHAT_WIDTH = 900;
    private final int CHAT_HEIGHT = 1600;

    private WindowManager windowManager;
    private WebView webView;
    private WindowManager.LayoutParams params;
    private boolean isExpanded = false;

    private float lastX = 0, lastY = 0, downX = 0, downY = 0;
    private final float TOUCH_SLOP = 12f;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        createNotificationChannel();
        Notification notification = buildNotification();
        
        // 兼容 Android 14 (API 34) 必须指定 type
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        setupWebView();
        addOverlay();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void setupWebView() {
        webView = new WebView(this);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.loadUrl("https://localhost/index.html?mode=pet");
    }

    private void addOverlay() {
        int layoutType = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? 
                         WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : 
                         WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                PET_SIZE, PET_SIZE, layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 80;
        params.y = 300;

        windowManager.addView(webView, params);

        webView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX(); downY = event.getRawY();
                    lastX = event.getRawX(); lastY = event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - lastX;
                    float dy = event.getRawY() - lastY;
                    if (Math.abs(event.getRawX() - downX) > TOUCH_SLOP || Math.abs(event.getRawY() - downY) > TOUCH_SLOP) {
                        params.x = (int) (params.x + dx);
                        params.y = (int) (params.y + dy);
                        windowManager.updateViewLayout(webView, params);
                        lastX = event.getRawX(); lastY = event.getRawY();
                    }
                    return true;
            }
            return false;
        });
    }

    public void expand() {
        if (isExpanded) return;
        isExpanded = true;
        params.width = CHAT_WIDTH;
        params.height = CHAT_HEIGHT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        windowManager.updateViewLayout(webView, params);
        webView.post(() -> webView.evaluateJavascript("window.__desktopPetSetMode('chat')", null));
    }

    public void collapse() {
        if (!isExpanded) return;
        isExpanded = false;
        params.width = PET_SIZE;
        params.height = PET_SIZE;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        windowManager.updateViewLayout(webView, params);
        webView.post(() -> webView.evaluateJavascript("window.__desktopPetSetMode('pet')", null));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            windowManager.removeView(webView);
            webView.destroy();
        }
  
