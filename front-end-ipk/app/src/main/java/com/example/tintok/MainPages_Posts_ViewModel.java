package com.example.tintok;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tintok.Communication.Communication;
import com.example.tintok.Communication.CommunicationEvent;
import com.example.tintok.CustomView.AfterRefreshCallBack;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.Post;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainPages_Posts_ViewModel extends AndroidViewModel {
    public MainPages_Posts_ViewModel(@NonNull Application application) {
        super(application);
    }


    public MutableLiveData<ArrayList<Post>> getPosts(){

        return DataRepositoryController.getInstance().getNewfeedPosts();
    }

    public String getCurrentUserID() {
        return DataRepositoryController.getInstance().getUser().getValue().getUserID();
    }

    private void submitLike(String id) {
        Communication.getInstance().get_socket().emit(CommunicationEvent.LIKE_POST, getCurrentUserID(), id);
    }


    private void unsubmitLike(String id) {
        Communication.getInstance().get_socket().emit(CommunicationEvent.UNLIKE_POST, getCurrentUserID(), id);
    }

    private void subscribePost(String id) {
        Communication.getInstance().get_socket().emit(CommunicationEvent.FOLLOW_POST, getCurrentUserID(), id);
    }

    private void unsubscribePost(String id) {
        Communication.getInstance().get_socket().emit(CommunicationEvent.UNFOLLOW_POST, getCurrentUserID(), id);
    }

    public void UserPressLike(Post post){
        if(DataRepositoryController.getInstance().isThisUserLikedPost(post)){
            DataRepositoryController.getInstance().getUser().getValue().likedPosts.remove(post.getId());
            unsubmitLike(post.getId());
        }
        else {
            DataRepositoryController.getInstance().getUser().getValue().likedPosts.add(post.getId());
            submitLike(post.getId());
        }

    }

    public void UserPressSubscribe(Post post){
        DataRepositoryController.getInstance().UpdateFollowingPost(post);
        if(DataRepositoryController.getInstance().isThisUserSubscribedPost(post)){
            subscribePost(post.getId());
        }
        else{
            unsubscribePost(post.getId());
        }
    }

    public void refreshData(AfterRefreshCallBack e) {
        DataRepositoryController.getInstance().refreshPost(e);
    }
}