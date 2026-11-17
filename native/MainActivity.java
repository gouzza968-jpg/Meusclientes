package com.meusclientes.app;

import android.os.Bundle;
import android.webkit.CookieManager;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(WaBusinessPlugin.class);
        super.onCreate(savedInstanceState);
        try {
            // permite login em painéis abertos dentro do app (cookies de terceiros no iframe)
            CookieManager.getInstance().setAcceptThirdPartyCookies(getBridge().getWebView(), true);
        } catch (Exception ignored) {}
    }
}
