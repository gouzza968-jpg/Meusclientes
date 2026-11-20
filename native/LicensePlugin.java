package com.meusclientes.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;

/**
 * Guarda o estado da licença/teste fora dos dados do app (pasta Android/media) e em SharedPreferences,
 * assinado com o ID do aparelho. Limpar os dados do app não apaga a pasta de mídia.
 */
@CapacitorPlugin(name = "License")
public class LicensePlugin extends Plugin {
    private static final String SECRET = "mc#Lic!2026$tp-9f3a";
    private static final String PREF = "mc_lic", KEY = "v";

    private String did() {
        String id = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        return id == null ? "" : id;
    }

    private String sig(String payload) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest((SECRET + "|" + did() + "|" + payload).getBytes("UTF-8"));
            StringBuilder s = new StringBuilder();
            for (byte x : b) s.append(String.format("%02x", x));
            return s.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private File file() {
        try {
            File[] d = getContext().getExternalMediaDirs();
            if (d != null && d.length > 0 && d[0] != null) {
                d[0].mkdirs();
                return new File(d[0], ".mclic");
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String readFile() {
        try {
            File f = file();
            if (f == null || !f.exists()) return null;
            FileInputStream in = new FileInputStream(f);
            byte[] buf = new byte[(int) f.length()];
            int n = in.read(buf);
            in.close();
            return new String(buf, 0, Math.max(n, 0), "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    /** devolve o payload se a assinatura confere; "" se adulterado; null se não existe */
    private String check(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        int i = raw.lastIndexOf('|');
        if (i < 0) return "";
        String p = raw.substring(0, i), s = raw.substring(i + 1).trim();
        return sig(p).equals(s) ? p : "";
    }

    @PluginMethod
    public void load(PluginCall call) {
        JSArray states = new JSArray();
        boolean tamper = false;
        SharedPreferences sp = getContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String[] raws = {readFile(), sp.getString(KEY, null)};
        for (String r : raws) {
            String p = check(r);
            if (p == null) continue;
            if (p.isEmpty()) tamper = true; else states.put(p);
        }
        JSObject o = new JSObject();
        o.put("states", states);
        o.put("tamper", tamper);
        call.resolve(o);
    }

    @PluginMethod
    public void save(PluginCall call) {
        String p = call.getString("state", "");
        if (p.isEmpty()) { call.reject("vazio"); return; }
        String raw = p + "|" + sig(p);
        getContext().getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, raw).apply();
        try {
            File f = file();
            if (f != null) {
                FileOutputStream os = new FileOutputStream(f);
                os.write(raw.getBytes("UTF-8"));
                os.close();
            }
        } catch (Exception ignored) {}
        call.resolve();
    }

    /** código do aparelho (MC-XXXX-XXXX), sempre o mesmo para o mesmo aparelho */
    @PluginMethod
    public void code(PluginCall call) {
        try {
            String id = did();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(("mc-dev|" + id).getBytes("UTF-8"));
            String a = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
            StringBuilder s = new StringBuilder("MC-");
            for (int i = 0; i < 8; i++) {
                if (i == 4) s.append('-');
                s.append(a.charAt((b[i] & 0xff) % 32));
            }
            JSObject o = new JSObject();
            o.put("code", id.isEmpty() ? "" : s.toString());
            call.resolve(o);
        } catch (Exception e) {
            call.reject("erro");
        }
    }
}
