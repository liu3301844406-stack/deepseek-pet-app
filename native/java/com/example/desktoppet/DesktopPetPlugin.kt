package com.example.desktoppet

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.ActivityCallback
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "DesktopPet")
class DesktopPetPlugin : Plugin() {
    @PluginMethod
    fun enter(call: PluginCall) {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            startActivityForResult(call, intent, "overlayPermissionResult")
            return
        }
        startOverlayService()
        call.resolve()
    }

    @ActivityCallback
    private fun overlayPermissionResult(call: PluginCall?, result: androidx.activity.result.ActivityResult) {
        if (Settings.canDrawOverlays(context)) {
            startOverlayService()
            call?.resolve()
        } else {
            call?.reject("未授予悬浮窗权限")
        }
    }

    private fun startOverlayService() {
        val intent = Intent(context, OverlayService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) context.startForegroundService(intent)
        else context.startService(intent)
    }

    @PluginMethod
    fun exit(call: PluginCall) {
        context.stopService(Intent(context, OverlayService::class.java))
        call.resolve()
    }

    @PluginMethod
    fun expand(call: PluginCall) {
        OverlayService.instance?.expand()
        call.resolve()
    }

    @PluginMethod
    fun collapse(call: PluginCall) {
        OverlayService.instance?.collapse()
        call.resolve()
    }
}
