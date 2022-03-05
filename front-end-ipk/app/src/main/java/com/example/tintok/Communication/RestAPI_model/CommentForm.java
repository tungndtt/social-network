package com.example.tintok.Communication.RestAPI_model;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class CommentForm {
    @SerializedName("id")
    private String id;
    @SerializedName("author_id")
    private String author_id;
    @SerializedName("author_name")
    private String author_name;
    @SerializedName("post_id")
    private String post_id;
    @SerializedName("comment")
    private String comment;
    @SerializedName("image_path")
    private String image_path;
    @SerializedName("date")
    private long date;
    public String getAuthor_id() {
        return author_id;
    }

    public String getId() {
        return id;
    }
    public String getAuthor_name() {
        return author_name;
    }

    public String getPost_id() {
        return post_id;
    }

    public String getComment() {
        return comment;
    }

    public String getImage_path() {
        return image_path;
    }

    public long getDate() {
        return date;
    }
}
