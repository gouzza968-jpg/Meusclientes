package com.meusclientes.app;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(WaBusinessPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
