package com.meusclientes.app;

import android.content.Intent;
import android.net.Uri;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Abre a conversa direto no WhatsApp Business (ou no WhatsApp comum, se não houver o Business). */
@CapacitorPlugin(name = "WaBusiness")
public class WaBusinessPlugin extends Plugin {

    @PluginMethod
    public void open(PluginCall call) {
        String phone = call.getString("phone", "");
        String text = call.getString("text", "");
        String[] pkgs = {"com.whatsapp.w4b", "com.whatsapp"};
        String https = "https://wa.me/" + phone + (text.isEmpty() ? "" : "?text=" + Uri.encode(text));
        String scheme = "whatsapp://send?phone=" + phone + (text.isEmpty() ? "" : "&text=" + Uri.encode(text));

        for (String pkg : pkgs) {
            for (String url : new String[]{https, scheme}) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    i.setPackage(pkg);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(i);
                    JSObject r = new JSObject();
                    r.put("package", pkg);
                    call.resolve(r);
                    return;
                } catch (Exception ignored) {
                    // tenta a próxima combinação
                }
            }
        }
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(https));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
            call.resolve();
        } catch (Exception e) {
            call.reject("WhatsApp não encontrado");
        }
    }
}
