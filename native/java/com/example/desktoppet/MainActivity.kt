package com.example.desktoppet

import android.os.Bundle
import com.getcapacitor.BridgeActivity

class MainActivity : BridgeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在 super.onCreate 之前注册插件！
        registerPlugin(DesktopPetPlugin::class.java)
        super.onCreate(savedInstanceState)
    }
}
