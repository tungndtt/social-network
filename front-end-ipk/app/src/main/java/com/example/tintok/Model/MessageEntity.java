package com.example.tintok.Model;

import android.text.SpannableStringBuilder;
import com.example.tintok.Utils.DateTimeUtil;
import java.time.LocalDateTime;

/**
 * This class represents a MessageEntity that include an authorID, a MediaEntity, SpannableStringBuilder to create a message and a date of message creation.
 * Represents a message in chat.
 */
public class MessageEntity {

    private String authorID;
    MediaEntity media;
    private SpannableStringBuilder builder;
    public LocalDateTime datePosted;


    public MessageEntity(String authorID, MediaEntity media, LocalDateTime date){
        this.authorID = authorID;
        this.media = media;
        this.datePosted = date;
    }
    public MessageEntity(String authorID, SpannableStringBuilder builder, LocalDateTime date){
        this.authorID = authorID;
        this.media = null;
        this.builder = builder;
        this.datePosted = date;
    }
    public String getAuthorID() {
        return authorID;
    }
    public SpannableStringBuilder getBuilder() {
        return builder;
    }
    public String getDatePosted() {
        return DateTimeUtil.ConvertTimeToString(this.datePosted);
    }
    public MediaEntity getMedia() {
        return media;
    }
}
