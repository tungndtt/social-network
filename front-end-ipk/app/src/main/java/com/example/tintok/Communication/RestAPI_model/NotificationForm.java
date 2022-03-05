package com.example.tintok.Communication.RestAPI_model;

import com.google.gson.annotations.SerializedName;


import java.time.LocalDateTime;

public class NotificationForm {

    public static String NEW_COMMENT = "new_comment";
    public static String NEW_REACTION = "new_reaction";
    public static String NEW_FRIEND = "new_friend";


    public String getType() {
        return type;
    }

    public long getDate() {
        return date;
    }


    public String getUrl() {
        return url;
    }

    public String getPostID() {
        return postID;
    }

    public String getStatus() {
        return status;
    }

    public String getPost_author_id() {
        return post_author_id;
    }

    public String getAuthor_id() {
        return author_id;
    }

    @SerializedName("type")
    String type;
    @SerializedName("time")
    long date;
    @SerializedName("author_id")
    String author_id;
    @SerializedName("post_img")
    String url;
    @SerializedName("post_id")
    String postID;
    @SerializedName("post_status")
    String status;
    @SerializedName("post_author_id")
    String post_author_id;


}
