package com.example.tintok.DataLayer;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.example.tintok.Communication.Communication;
import com.example.tintok.Communication.RestAPI;
import com.example.tintok.Communication.RestAPI_model.PostForm;
import com.example.tintok.Communication.RestAPI_model.PostRequest;
import com.example.tintok.CustomView.AfterRefreshCallBack;
import com.example.tintok.Model.Post;
import com.example.tintok.Utils.DataConverter;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  This class is responsible to load and refresh all posts on the NewsFeed page
 */
public class DataRepository_Posts extends AbstractDataRepository {
    MutableLiveData<ArrayList<Post>> newfeedPosts;
    DataRepositoryController controller;
    RestAPI api;

    public DataRepository_Posts(DataRepositoryController controller){
        newfeedPosts = new MutableLiveData<>(new ArrayList<>());
        this.controller = controller;
        this.api = Communication.getInstance().getApi();
    }

    public MutableLiveData<ArrayList<Post>> getNewfeedPosts(){
        return newfeedPosts;
    }

    public void setNewfeedPosts(Post... newPosts){
        ArrayList<Post> mPosts = newfeedPosts.getValue();
        for(Post p : newPosts){
            mPosts.add(p);
        }
        newfeedPosts.setValue(mPosts);

    }
    public void postNewfeedPosts(Post... newPosts){
        ArrayList<Post> mPosts = newfeedPosts.getValue();
        for(Post p : newPosts){
            mPosts.add(p);
        }
        newfeedPosts.postValue(mPosts);

    }

    /**
     * requests all posts for the newsfeed page after login
     */
    long timeStamp = 0;
    public void initData(){

        this.api.getPosts(new PostRequest(Instant.now().toEpochMilli(), new ArrayList<>())).enqueue(new Callback<ArrayList<PostForm>>() {
            @Override
            public void onResponse(Call<ArrayList<PostForm>> call, Response<ArrayList<PostForm>> response) {
                if(response.isSuccessful()){
                    ArrayList<PostForm> postForms = response.body();
                   submitNewData(postForms);
                   try{
                       timeStamp = postForms.get(0).getDate();
                   }catch (Exception e){

                   }
                    setReady();
                } else {
                    Log.d("DataRepoPost", "Response fails");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<PostForm>> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                Log.e("Error", "Connection error");
            }
        });
    }

    /**
     * requests all posts for the newsfeed page after user refresh
     * @param e
     */
    public void refreshPost(AfterRefreshCallBack e) {
        if(this.getNewfeedPosts().getValue() == null || this.newfeedPosts.getValue().isEmpty()){
            this.initData();
            e.onRefreshingDone();
            return;
        }
        ArrayList<String> seenPosts = new ArrayList<>();
        for(Post p : this.getNewfeedPosts().getValue()){
            seenPosts.add(p.getId());
        }
        this.api.getPosts(new PostRequest(this.getNewfeedPosts().getValue().get(seenPosts.size()-1).getDateTime().atZone(ZoneId.systemDefault()).toEpochSecond()
                , seenPosts)).enqueue(new Callback<ArrayList<PostForm>>() {
            @Override
            public void onResponse(Call<ArrayList<PostForm>> call, Response<ArrayList<PostForm>> response) {
                if(response.isSuccessful()){
                    ArrayList<PostForm> postForms = response.body();
                    submitNewData(postForms);
                } else {
                    Log.d("Info", "Response fails");
                }
                e.onRefreshingDone();
            }

            @Override
            public void onFailure(Call<ArrayList<PostForm>> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                Log.e("Error", "Connection error");
            }
        });
    }

    /**
     * update LiveData that contain the newsfeed posts
     * @param posts fetched posts from backend
     */
    protected void submitNewData(ArrayList<PostForm> posts) {
        ArrayList<Post> current = this.newfeedPosts.getValue();
        if(current == null)
            current = new ArrayList<>();
        ArrayList<Post> newPosts = DataConverter.ConvertFromPostForm(posts);
        for(Post p : newPosts){
            if(current.contains(p)){
                current.remove(p);
            }
        }
        current.addAll(newPosts);
        this.newfeedPosts.postValue(current);
    }
    //Server part

}
