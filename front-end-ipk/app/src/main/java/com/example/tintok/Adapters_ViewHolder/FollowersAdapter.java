package com.example.tintok.Adapters_ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.DataLayer.DataRepository_UserSimple;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.R;

import java.util.ArrayList;

public class FollowersAdapter extends BaseAdapter<String, FollowersAdapter.ViewHolder> {


    public FollowersAdapter(Context context, ArrayList<String> models) {
        super(context, models);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_followers, parent, false);
        return new ViewHolder(view, this);

    }

    @Override
    public void onViewAttachedToWindow(@NonNull  FollowersAdapter.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        DataRepositoryController.getInstance().AddUserProfileChangeListener(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull  FollowersAdapter.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        DataRepositoryController.getInstance().RemoveUserProfileChangeListener(holder);
    }

    public onClickUserListener onClickUserListener;
    public static interface onClickUserListener{
        public void onClickUser(int position);
    }

    public class ViewHolder extends BaseViewHolder<String> implements DataRepository_UserSimple.OnUserProfileChangeListener {

        TextView mFollowerName;
        AppCompatImageView mFollowerProfilePic;

        String userID;
        public ViewHolder(@NonNull View itemView, BaseAdapter mAdapter) {
            super(itemView, mAdapter);
            mFollowerName = itemView.findViewById(R.id.item_follower_username);
            mFollowerProfilePic = itemView.findViewById(R.id.item_follower_profilePic);
            mFollowerProfilePic.setOnClickListener(v -> {
                if(onClickUserListener != null)
                    onClickUserListener.onClickUser(getAdapterPosition());
            });
        }

        @Override
        public void bindData(String itemData) {
            this.userID = itemData;
            onProfileChange(DataRepositoryController.getInstance().getUserSimpleProfile(itemData));

        }

        @Override
        public void onProfileChange(UserSimple user) {
            if(user == null)
                return;
            if(this.userID.compareTo(user.getUserID()) == 0){
                Glide.with(mAdapter.getContext()).load(user.getProfilePic().url).diskCacheStrategy(DiskCacheStrategy.DATA).into(mFollowerProfilePic);
                mFollowerName.setText(user.getUserName());

            }
        }
    }
}
