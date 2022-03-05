package com.example.tintok.Communication.RestAPI_model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ChatForm {
    @SerializedName("roomId")
    private String room_id;
    @SerializedName("user_1")
    private String user_1;
    @SerializedName("user_2")
    private String user_2;
    @SerializedName("messages")
    private ArrayList<MessageForm> messages;

    public String getUser_1() {
        return user_1;
    }

    public String getUser_2() {
        return user_2;
    }

    public ArrayList<MessageForm> getMessages() {
        return messages;
    }

    public String getRoom_id() {
        return room_id;
    }
}
