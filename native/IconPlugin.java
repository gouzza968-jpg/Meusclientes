package com.meusclientes.app;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Troca o ícone do app ligando/desligando os atalhos alternativos (activity-alias). */
@CapacitorPlugin(name = "AppIcon")
public class IconPlugin extends Plugin {
    private static final String[] ID = {"a0", "a1", "a2", "a3", "a4", "a5"};
    private static final String[] CLS = {"IconA0", "IconA1", "IconA2", "IconA3", "IconA4", "IconA5"};

    private ComponentName cn(int i) {
        return new ComponentName(getContext().getPackageName(), "com.meusclientes.app." + CLS[i]);
    }

    private int current() {
        PackageManager pm = getContext().getPackageManager();
        for (int i = 0; i < ID.length; i++) {
            int st = pm.getComponentEnabledSetting(cn(i));
            if (st == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) return i;
        }
        return 0; // padrão (A0 vem habilitado no manifesto)
    }

    @PluginMethod
    public void get(PluginCall call) {
        JSObject o = new JSObject();
        o.put("id", ID[current()]);
        call.resolve(o);
    }

    @PluginMethod
    public void set(PluginCall call) {
        String id = call.getString("id", "a0");
        int want = -1;
        for (int i = 0; i < ID.length; i++) if (ID[i].equals(id)) want = i;
        if (want < 0) { call.reject("ícone inválido"); return; }
        try {
            PackageManager pm = getContext().getPackageManager();
            // liga o novo primeiro, para nunca ficar sem ícone
            pm.setComponentEnabledSetting(cn(want), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            for (int i = 0; i < ID.length; i++) {
                if (i != want) pm.setComponentEnabledSetting(cn(i), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("erro");
        }
    }
}
