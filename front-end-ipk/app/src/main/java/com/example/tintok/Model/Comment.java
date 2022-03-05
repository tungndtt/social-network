package com.example.tintok.Model;

import android.text.SpannableStringBuilder;
import androidx.annotation.Nullable;
import java.time.LocalDateTime;

/**
 * This class represents a Comment of a user on a posts.
 * Each Comment has an unique ID and can contain text and MessageEntities
 * @see MessageEntity
 */
public class Comment extends MessageEntity {

    private String id;

    /**
     * Constructor
     * @param id unique ID of a Comment
     * @param authorID unique ID of the author
     * @param builder comment of author / typed text of the author
     * @param media a chosen media entity by author (not required)
     * @param date current local time
     */
    public Comment(String id, String authorID, @Nullable  SpannableStringBuilder builder, @Nullable MediaEntity media, LocalDateTime date) {
        super(authorID, builder, date);
        this.id = id;
        this.media = media;
    }
    public String getId() {
        return id;
    }
}
