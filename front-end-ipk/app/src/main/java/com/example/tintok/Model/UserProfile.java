package com.example.tintok.Model;

import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;

/**
 * This class represents an user that holds basic user information and several LiveData.
 * Those are lists of followers, following persons, users interests, posts user's following and his posts.
 * @see UserSimple
 */
public class UserProfile extends UserSimple {

    public MutableLiveData<ArrayList<String>> followers;
    public MutableLiveData<ArrayList<String>> following;
    public MutableLiveData<ArrayList<Integer>> userInterests;
    public MutableLiveData<ArrayList<String>> followingPost;
    public MutableLiveData<ArrayList<Post>> myPosts;
    public ArrayList<String> likedPosts;

    public UserProfile(){
        this.myPosts = new MutableLiveData<>(new ArrayList<>());
        this.userInterests = new MutableLiveData<>(new ArrayList<>());
        this.followers = new MutableLiveData<>(new ArrayList<>());
        this.following = new MutableLiveData<>(new ArrayList<>());
        this.followingPost = new MutableLiveData<>(new ArrayList<>());
        this.likedPosts = new ArrayList<>();
    }
    public MutableLiveData<ArrayList<String>> getFollowingPost() {
        return followingPost;
    }

    public MutableLiveData<ArrayList<Integer>> getUserInterests(){
        return userInterests;
    }
    public void setUserInterests(ArrayList<Integer> interests){
        this.userInterests.postValue(interests);
    }
    public MutableLiveData<ArrayList<String>> getFollowers() {
        return followers;
    }
    public void postFollowers(ArrayList<String> followers) {
        this.followers.postValue(followers);
    }
    public MutableLiveData<ArrayList<String>> getFollowing() {
        return following;
    }
    public void postFollowering(ArrayList<String> followering) {
        this.following.postValue(followering);
    }
    public MutableLiveData<ArrayList<Post>> getMyPosts() {
        return myPosts;
    }

    public void copyFrom(UserProfile other){
    }
    public void setFollowingPost(MutableLiveData<ArrayList<String>> followingPost) {
        this.followingPost = followingPost;
    }
    public void postMyPosts(ArrayList<Post> myPosts) {
        this.myPosts.postValue(myPosts);
    }
}
