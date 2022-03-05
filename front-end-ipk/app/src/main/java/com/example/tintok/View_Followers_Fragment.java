package com.example.tintok;

import android.app.Application;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.tintok.Adapters_ViewHolder.FollowersAdapter;
import com.example.tintok.CustomView.MyDialogFragment;
import com.example.tintok.CustomView.NoSpaceRecyclerViewDecoration;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.UserSimple;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

/**
 *  Class to view either user's followers or persons that the user is following.
 *  Decision based on passed arguments at creation of this fragment.
 */
public class View_Followers_Fragment extends MyDialogFragment implements FollowersAdapter.onClickUserListener {

    private MainPages_MyProfile_ViewModel mViewModel;
    private RecyclerView mFollowersRV;
    View view;
    private MaterialToolbar toolbar;
    private FollowersAdapter followersAdapter;
    ArrayList<String> ids;

    public View_Followers_Fragment(ArrayList<String> ids) {
        // Required empty public constructor
        this.ids = ids;
    }

    public static View_Followers_Fragment newInstance(ArrayList<String> ids) {
        View_Followers_Fragment fragment = new View_Followers_Fragment( ids);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mViewModel == null)
            mViewModel = new ViewModelProvider(this).get(MainPages_MyProfile_ViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.view_followers_fragment, container, false);
        mFollowersRV = view.findViewById(R.id.view_followers_recyclerview);
        toolbar = view.findViewById(R.id.view_followers_toolbar);
        toolbar.setNavigationOnClickListener(v -> {
           dismiss();
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
            If key-value FOLLOW is 0 then an ArrayList is filled with all user's followers.
            If the key-value is 1 then the ArrayList is filled with all persons the user follows.
         */




        followersAdapter = new FollowersAdapter(this.getContext(), ids);
        followersAdapter.onClickUserListener = this;
        mFollowersRV.setAdapter(followersAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView.ItemDecoration decoration= new NoSpaceRecyclerViewDecoration();
        mFollowersRV.setLayoutManager(manager);
        mFollowersRV.addItemDecoration(decoration);

    }

    @Override
    public void onClickUser(int position) {
        App.startActivityViewProfile(requireContext(), ids.get(position));
    }

    public void onResume() {
        super.onResume();
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(android.content.DialogInterface dialog,
                                 int keyCode, android.view.KeyEvent event) {
                if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                    // To dismiss the fragment when the back-button is pressed.
                    dismiss();
                    return true;
                }
                // Otherwise, do nothing else
                else return false;
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        try{
            MainPages_MyProfile_Fragment parent = (MainPages_MyProfile_Fragment) getParentFragment();
            parent.onCloseViewUserList();
        }catch (Exception e){
            Log.e("View_UserListFrag", "No appropriate parent");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFollowersRV.setAdapter(null);
    }
}