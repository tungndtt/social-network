package com.example.tintok.Communication;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

public abstract class CustomEmitterListener implements Emitter.Listener {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void call(Object... args) {
        String data = (String)args[0];
        JSONObject rawData = PacketFactory.getInstance().getDataFromPacket(data);
        callbackFunc(rawData);

    }

    public abstract void callbackFunc(JSONObject data);
}
