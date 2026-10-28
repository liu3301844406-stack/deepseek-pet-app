package com.example.desktoppet

import android.os.Bundle
import com.getcapacitor.BridgeActivity

class MainActivity : BridgeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        registerPlugin(DesktopPetPlugin::class.java)
        super.onCreate(savedInstanceState)
    }
}
