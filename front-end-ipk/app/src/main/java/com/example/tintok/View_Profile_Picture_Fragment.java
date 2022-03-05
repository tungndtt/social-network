package com.example.tintok;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.CustomView.MyDialogFragment;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.Model.Post;
import com.example.tintok.Model.UserProfile;
import com.google.android.material.appbar.MaterialToolbar;

/**
 *  Shows the picture, username and status of the post based on the arguments that are supplied when the fragment was instantiated
 */
public class View_Profile_Picture_Fragment extends MyDialogFragment {

    private TextView mName, mStatus;
    private ImageView mImage;
    private View view;
    private MaterialToolbar toolbar;

    public View_Profile_Picture_Fragment() {
        // Required empty public constructor
    }


    public static View_Profile_Picture_Fragment newInstance() {
        View_Profile_Picture_Fragment fragment = new View_Profile_Picture_Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * Inflates the layout for this fragment.
     * Initialization of views and toolbar.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile_picture_view_fragment, container, false);
        mName = view.findViewById(R.id.picture_view_nameTV);
        mStatus = view.findViewById(R.id.picture_view_status);
        mImage = view.findViewById(R.id.picture_view_image);
        toolbar = view.findViewById(R.id.picture_view_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_backspace);
        toolbar.setNavigationOnClickListener(v -> {
            getDialog().dismiss();
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    /**
     * if arguments are not null then username, status and image are set accordingly
     * else nothing is shown.
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(this.getArguments() != null){
            Bundle bundle = this.getArguments();
            mName.setText(bundle.getString("name"));
            mStatus.setText(bundle.getString("status"));
            Glide.with(this.getContext()).load(bundle.getString("url"))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(mImage);
        }
    }
}