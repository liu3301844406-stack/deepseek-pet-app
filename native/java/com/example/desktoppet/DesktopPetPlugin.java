package com.example.desktoppet;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.ActivityCallback;
import androidx.activity.result.ActivityResult;

@CapacitorPlugin(name = "DesktopPet")
public class DesktopPetPlugin extends Plugin {

    @PluginMethod
    public void enter(PluginCall call) {
        if (!Settings.canDrawOverlays(getContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(call, intent, "overlayPermissionResult");
            return;
        }
        startOverlayService();
        call.resolve();
    }

    @ActivityCallback
    private void overlayPermissionResult(PluginCall call, ActivityResult result) {
        if (Settings.canDrawOverlays(getContext())) {
            startOverlayService();
            call.resolve();
        } else {
            call.reject("未授予悬浮窗权限");
        }
    }

    private void startOverlayService() {
        Intent intent = new Intent(getContext(), OverlayService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            getContext().startForegroundService(intent);
        } else {
            getContext().startService(intent);
        }
    }

    @PluginMethod
    public void exit(PluginCall call) {
        getContext().stopService(new Intent(getContext(), OverlayService.class));
        call.resolve();
    }

    @PluginMethod
    public void expand(PluginCall call) {
        if (OverlayService.instance != null) OverlayService.instance.expand();
        call.resolve();
    }

    @PluginMethod
    public void collapse(PluginCall call) {
        if (OverlayService.instance != null) OverlayService.instance.collapse();
        call.resolve();
    }
}
