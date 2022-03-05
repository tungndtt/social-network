package com.example.tintok.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

/**
 * This class represents a Post of a user.
 * A post is identified by ID, contains a status/text, the author of the post as ID, a MediaEntity,
 * date of creation, users who liked this post and comments related to this post.
 */
public class Post {
    private String id;
    private String status;
    private String author_id;
    private MediaEntity image;
    private LocalDateTime dateTime;
    public ArrayList<String> likers;
    public MutableLiveData<ArrayList<Comment>> comments;

    /**
     * Post-Constructor
     * @param id of the post for unique identification
     * @param status text/message to this post from author.
     * @param author_id unique id to identify author
     * @param image MediaEntity that the user took from gallery or from camera
     * @param dateTime date of creation
     */
    public Post(String id, String status, String author_id, MediaEntity image, LocalDateTime dateTime) {
        this.id = id;
        this.status = status;
        this.author_id = author_id;
        this.image = image;
        this.dateTime = dateTime;
        comments = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }


    public MediaEntity getImage() {
        return image;
    }

    public void setImage(MediaEntity image) {
        this.image = image;
    }

    public boolean equals(@Nullable Object obj) {
        try{
            return ((Post)obj).getId().compareTo(this.getId()) == 0;
        }catch (Exception e){
            return super.equals(obj);
        }
    }
    public LocalDateTime getDateTime() {
        return this.dateTime;
    }
}
