package com.example.tintok.Communication;

import android.os.Build;

import androidx.annotation.RequiresApi;



import org.json.JSONException;
import org.json.JSONObject;

///DOnt use this class for REST API CALL

public class PacketFactory {
    public static PacketFactory instance;
    String mToken = null;


    public static PacketFactory getInstance(){
        if(instance == null)
            instance = new PacketFactory();
        return instance;
    }
    private PacketFactory(){
        mToken = "";
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public String createPacket(JSONObject data){
        JSONObject jo = data;
        String result="";
        try {
            if(mToken!=null)
                jo.put("author", mToken);
            if(SecureConnection.getInstance().serverpubKey!=null)
                result = SecureConnection.getInstance().EncodeDataToSend(jo.toString());
            else
                result = jo.toString();
        } catch (JSONException e) {
            throw new RuntimeException("data not in json format");
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public JSONObject getDataFromPacket(String data){
        JSONObject jo = null;
        try {
            jo = new JSONObject(data);
            if(mToken != null){
                String currentToken = jo.optString("accessToken");
                if(currentToken.compareTo(mToken) == 0 )
                    jo.remove("accessToken");
                else
                    jo = new JSONObject();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

}
