package com.example.tintok.Communication.RestAPI_model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PostRequest {
    @SerializedName("date")
    long timestamp;
    @SerializedName("seenPosts")
    ArrayList<String> seenPosts;

    public PostRequest(long timestamp, ArrayList<String> seenPosts) {
        this.timestamp = timestamp;
        this.seenPosts = seenPosts;
    }
}
