package com.example.tintok;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tintok.Adapters_ViewHolder.PostAdapter;
import com.example.tintok.CustomView.AfterRefreshCallBack;
import com.example.tintok.CustomView.PostUploadFragment;
import com.example.tintok.CustomView.Refreshable;
import com.example.tintok.Model.Post;

import java.util.ArrayList;


public class MainPages_Posts_Fragment extends Fragment implements PostAdapter.onPostListener, Refreshable {

    private MainPages_Posts_ViewModel mViewModel = null;

    boolean allowViewCmt, allowViewAuthorProfile;

    public MainPages_Posts_Fragment(boolean allowViewCmt, boolean allowViewAuthorProfile){
        this.allowViewCmt = allowViewCmt;
        this.allowViewAuthorProfile = allowViewAuthorProfile;
    }
    public MainPages_Posts_Fragment(){

    }


    public void setViewModel(MainPages_Posts_ViewModel mViewModel){
        this.mViewModel = mViewModel;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_pages__posts__fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mViewModel == null)
            mViewModel = new ViewModelProvider(this.getActivity()).get(MainPages_Posts_ViewModel.class);
        // TODO: Use the ViewMode
        this.postAdapter = new PostAdapter(getContext(), new ArrayList<>());
        this.postAdapter.setListener(this);
        this.recyclerView = getView().findViewById(R.id.post_list);
        recyclerView.setAdapter(postAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mViewModel.getPosts().observe(this.getViewLifecycleOwner(), posts -> {
            Log.i("PostFrag", "Posts changed");
            postAdapter.setItems(new ArrayList<>());
            postAdapter.setItems(posts);
        });
        Log.i("INFO", "OnActivityCreated fragment for post fragment ...");
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<Post> posts = postAdapter.getItems();
        postAdapter.setItems(new ArrayList<>());
        postAdapter.setItems(posts);
    }

    private RecyclerView recyclerView;

    private PostAdapter postAdapter;

    @Override
    public void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        Log.i("INFO", "Destroy fragment for post fragment ...");
    }

    //private ViewModel viewModel;



    public void onClickAvatar(View v, int position) {
        if(!allowViewAuthorProfile)
            return;
        String userID = postAdapter.getItems().get(position).getAuthor_id();
        App.startActivityViewProfile(this.requireContext(), userID);
    }

    @Override
    public void onClickComment(View v, int position) {
        if(!allowViewCmt )
            return;
        Post post =  postAdapter.getItems().get(position);
        App.startActivityComment(this.requireContext(), post.getId());
    }

    @Override
    public void onClickLike(View v, int position) {
        Post post =  postAdapter.getItems().get(position);
        mViewModel.UserPressLike(post);
    }

    @Override
    public void onNotificationChange(int position) {
        Post post =  postAdapter.getItems().get(position);
        mViewModel.UserPressSubscribe(post);
    }

    @Override
    public void onRefresh(AfterRefreshCallBack e) {
        mViewModel.refreshData(e);

    }
}