package com.example.tintok.Model;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Utils.DateTimeUtil;
import java.time.LocalDateTime;

/**
 * This class represents a notification that a user receives
 * if another user likes one of his posts, make a comment on one of his posts or both of them follower each other.
 */
public class Notification {

    String postID;
    String post_author_id;
    String post_status;
    NotificationType type;
    String author_id;
    String url;
    public  LocalDateTime date;
    public enum NotificationType{
        NEW_FRIEND, LIKE_POST, COMMENT_POST
    }

    /**
     * Constructor
     * @param type NEW_FRIEND if two users follow each other. LIKE_POST if another user likes user's post. COMMENT_POST if another user comment on his post.
     * @param date time of notification creation
     * @param author_id id of the user who likes/comment a post
     * @param url of the post
     * @param postID id of the post
     * @param post_author_id id of the author of the post
     * @param post_status message of the post
     */
    public Notification(NotificationType type, LocalDateTime date, String author_id, String url, String postID,  String post_author_id, String post_status) {
        this.type = type;
        this.date = date;
        this.author_id = author_id;
        this.url = url;
        this.postID = postID;
        this.post_author_id = post_author_id;
        this.post_status = post_status;
    }

    public String getAuthor_id() {
        return author_id;
    }
    public NotificationType getType() {
        return type;
    }
    public String getDate() {
        return DateTimeUtil.ConvertTimeToString(this.date);
    }
    public String getUrl() {
        return url;
    }
    public String getPostID() {
        return postID;
    }


    /**
     * Create a String based on the NotificationType for displaying this string to user
     * @return String with author name who triggered the notification plus notification message
     */
    public SpannableStringBuilder toTextViewString() {
        UserSimple user = (DataRepositoryController.getInstance().getUserSimpleProfile(this.author_id));
        String author_username = (user == null)?"":user.getUserName();
        SpannableStringBuilder str = new SpannableStringBuilder(author_username);
        StyleSpan bold_italicText = new StyleSpan(Typeface.BOLD_ITALIC);
        StyleSpan italicText = new StyleSpan(Typeface.ITALIC);
        str.setSpan(bold_italicText, 0, str.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        if(type == NotificationType.NEW_FRIEND){
            str.append(" and you have been following each other. You can now chat with each others");
        }

        else if(type == NotificationType.LIKE_POST){
            str.append(" has put a like on your post");
        }
        else if(type == NotificationType.COMMENT_POST){
            str.append(" has put a new comment on your post");
        }
        str.setSpan(bold_italicText, author_username.length(), str.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        for (UnderlineSpan span : str.getSpans(0, str.length(), UnderlineSpan.class))
            str.removeSpan(span);
        return str;
    }



}
