package com.example.desktoppet;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(DesktopPetPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
