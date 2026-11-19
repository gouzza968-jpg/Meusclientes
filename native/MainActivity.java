package com.meusclientes.app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.webkit.CookieManager;

import com.getcapacitor.BridgeActivity;

import java.io.InputStream;
import java.security.MessageDigest;

public class MainActivity extends BridgeActivity {
    // preenchidos pelo workflow na hora de gerar o APK
    private static final String CERT_SHA256 = "__CERT_SHA256__";
    private static final String HTML_SHA256 = "__HTML_SHA256__";

    private static String hex(byte[] b) {
        StringBuilder s = new StringBuilder();
        for (byte x : b) s.append(String.format("%02x", x));
        return s.toString();
    }

    /** Confere se o APK foi reassinado ou se o código do app foi alterado. */
    private boolean integro() {
        try {
            if (CERT_SHA256.startsWith("__") || HTML_SHA256.startsWith("__")) return true; // build de desenvolvimento
            PackageManager pm = getPackageManager();
            Signature[] sigs;
            if (Build.VERSION.SDK_INT >= 28) {
                PackageInfo pi = pm.getPackageInfo(getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                sigs = pi.signingInfo.hasMultipleSigners() ? pi.signingInfo.getApkContentsSigners() : pi.signingInfo.getSigningCertificateHistory();
            } else {
                @SuppressWarnings("deprecation")
                PackageInfo pi = pm.getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
                sigs = pi.signatures;
            }
            boolean certOk = false;
            for (Signature sg : sigs) {
                if (hex(MessageDigest.getInstance("SHA-256").digest(sg.toByteArray())).equals(CERT_SHA256)) certOk = true;
            }
            if (!certOk) return false;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            InputStream in = getAssets().open("public/index.html");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
            in.close();
            return hex(md.digest()).equals(HTML_SHA256);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(WaBusinessPlugin.class);
        registerPlugin(AppUpdatePlugin.class);
        registerPlugin(LicensePlugin.class);
        super.onCreate(savedInstanceState);
        if (!integro()) {
            finishAffinity();
            System.exit(0);
            return;
        }
        try {
            // permite login em painéis abertos dentro do app (cookies de terceiros no iframe)
            CookieManager.getInstance().setAcceptThirdPartyCookies(getBridge().getWebView(), true);
        } catch (Exception ignored) {}
    }
}
