package com.example.tintok.Communication.RestAPI_model;

import com.google.gson.annotations.SerializedName;

public class LoginResponseForm {
    @SerializedName("token")
    private String token;
    @SerializedName("message")
    private String msg;

    public String getToken() {
        return token;
    }

    public String getMsg() {
        return msg;
    }
}
