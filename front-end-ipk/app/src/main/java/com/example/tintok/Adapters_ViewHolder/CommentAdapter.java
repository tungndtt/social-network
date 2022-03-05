package com.example.tintok.Adapters_ViewHolder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.DataLayer.DataRepository_UserSimple;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.R;
import com.example.tintok.Model.Comment;

import java.util.ArrayList;


public class CommentAdapter extends BaseAdapter<Comment, CommentAdapter.ViewHolder>{
    public CommentAdapter(Context mContext, ArrayList<Comment> items) {
        super(mContext, items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void addItem(Comment item) {
        this.items.add(0,item);
        notifyItemInserted(0);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        DataRepositoryController.getInstance().AddUserProfileChangeListener(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        DataRepositoryController.getInstance().RemoveUserProfileChangeListener(holder);
    }

    public static class ViewHolder extends BaseViewHolder<Comment> implements DataRepository_UserSimple.OnUserProfileChangeListener {

        private TextView name, status, date;
        private ImageView profile_pic, content_pic;
        private CardView view;
        String id;
        public ViewHolder(@NonNull View itemView, BaseAdapter mAdapter) {
            super(itemView, mAdapter);
            this.name = itemView.findViewById(R.id.name);
            this.status = itemView.findViewById(R.id.leftcontent);
            this.date = itemView.findViewById(R.id.leftdate);
            this.profile_pic = itemView.findViewById(R.id.leftprofilePic);
            this.content_pic = itemView.findViewById(R.id.leftImg);
            this.view = itemView.findViewById(R.id.view);
        }

        @Override
        public void bindData(Comment itemData) {
            id = itemData.getAuthorID();
            this.status.setText(itemData.getBuilder());
            this.date.setText(itemData.getDatePosted().toString());
            view.setBackgroundResource(R.drawable.comment_background);
            UserSimple user = DataRepositoryController.getInstance().getUserSimpleProfile(itemData.getAuthorID());
            if(user != null) {
                this.onProfileChange(user);
            }
            if(itemData.getMedia() != null){
                content_pic.setVisibility(View.VISIBLE);
                Glide.with(mAdapter.getContext()).load(itemData.getMedia().url)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(content_pic);
            }
            else{
                content_pic.setVisibility(View.GONE);
            }
        }

        @Override
        public void onProfileChange(UserSimple user) {
            if(this.id.compareTo(user.getUserID()) == 0){
                Glide.with(mAdapter.getContext()).load(user.getProfilePic().url)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(profile_pic);
                this.name.setText(user.getUserName());
            }
        }
    }

}
