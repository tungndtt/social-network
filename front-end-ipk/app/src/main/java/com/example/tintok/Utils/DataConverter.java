package com.example.tintok.Utils;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.tintok.Communication.RestAPI_model.PostForm;
import com.example.tintok.Communication.RestAPI_model.UserForm;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.Model.Post;
import com.example.tintok.Model.UserProfile;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.R;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DataConverter {
    public static ArrayList<Post> ConvertFromPostForm(ArrayList<PostForm> received){
        ArrayList<Post> result = new ArrayList<>();
        if(received == null) return result;
        for(PostForm postForm: received){
            Post e = new Post(postForm.getId(), postForm.getStatus(),postForm.getAuthor_id(), new MediaEntity(postForm.getImageUrl()),
                    Instant.ofEpochMilli(postForm.getDate()).atZone(ZoneId.systemDefault()).toLocalDateTime());
            e.likers = postForm.getLikes();
            result.add(e);
        }
        return result;
    }

    public static ArrayList<UserSimple> ConvertFromUserFormToSimple(ArrayList<UserForm> received ){
        ArrayList<UserSimple> result = new ArrayList<>();
        if(received == null) return result;
        for(UserForm f : received){
            UserSimple user = new UserSimple();
            user.setUserID(f.getId());
            user.setUserName(f.getUsername());
            user.setDescription(f.getDescription()); //TODO: f.getDescription() ?
            user.setEmail(f.getEmail());
            user.setProfilePic(new MediaEntity(null, f.getImageUrl()));
            user.setBirthday(DateTimeUtil.getDateFromString(f.getBirthday()).toLocalDate());
            result.add(user);
        }
        return result;
    }

    public static UserProfile ConvertToUserProfile(UserForm form){
        UserProfile currUser = new UserProfile();
        currUser.setUserName(form.getUsername());
        currUser.setUserID(form.getId());
        currUser.setEmail(form.getEmail());
        currUser.setBirthday(DateTimeUtil.getDateFromString(form.getBirthday()).toLocalDate());
        currUser.setLocation(form.getLocation());
        currUser.setDescription(form.getDescription());
        currUser.setGender(form.getGender());
        currUser.setProfilePic(new MediaEntity(form.getImageUrl()));
        currUser.userInterests.postValue(form.getInterests());//setUserInterests(form.getInterests()); //LIVEDATA
        ArrayList<Post> photos = currUser.getMyPosts().getValue();
        for(PostForm post : form.getPosts()){
            Post tmp = new Post(post.getId(), post.getStatus(), post.getAuthor_id(), new MediaEntity(post.getImageUrl()),
                    Instant.ofEpochMilli(post.getDate()).atZone(ZoneId.systemDefault()).toLocalDateTime());
            tmp.likers = post.getLikes();
            photos.add(tmp);
        }
        currUser.likedPosts = form.getLiked_posts();
        currUser.myPosts.postValue(photos);
        ArrayList<String> dummy = currUser.getFollowers().getValue();
        dummy.addAll(form.getFollowers());
        Log.e("DataRepo_CurrentU","follower = " + form.getFollowers());
        currUser.postFollowers(dummy);
        dummy = currUser.getFollowing().getValue();
        dummy.addAll(form.getFollowing());
        currUser.postFollowering(dummy);
        dummy = currUser.getFollowingPost().getValue();
        if(form.getFollowing_posts() != null){
            dummy.addAll(form.getFollowing_posts());
            currUser.getFollowingPost().postValue(dummy);
        }


        return currUser;
    }
}
