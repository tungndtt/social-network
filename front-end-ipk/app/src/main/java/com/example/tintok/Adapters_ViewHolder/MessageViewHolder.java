package com.example.tintok.Adapters_ViewHolder;


import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.DataLayer.DataRepository_UserSimple;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.Model.MessageEntity;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.R;

public class MessageViewHolder extends BaseViewHolder<MessageEntity> implements DataRepository_UserSimple.OnUserProfileChangeListener {

    CardView view;
    LinearLayout messageLayout;
    LinearLayoutCompat leftSide, rightSide;
    ImageView leftProfilePic, rightProfilePic, leftPic, rightPic;
    TextView leftContent, leftDate, rightContent, rightDate;
    String userID;
    public MessageViewHolder(@NonNull View itemView, BaseAdapter mAdapter) {
        super(itemView, mAdapter);
        messageLayout = itemView.findViewById(R.id.messageLayout);
        view = itemView.findViewById(R.id.view);

        leftSide = itemView.findViewById(R.id.leftSide);
        leftProfilePic = itemView.findViewById(R.id.leftprofilePic);
        leftPic = itemView.findViewById(R.id.leftImg);
        leftContent = itemView.findViewById(R.id.leftcontent);
        leftDate = itemView.findViewById(R.id.leftdate);

        rightSide = itemView.findViewById(R.id.rightSide);
        rightProfilePic = itemView.findViewById(R.id.rightprofilePic);
        rightPic = itemView.findViewById(R.id.rightImg);
        rightContent = itemView.findViewById(R.id.rightcontent);
        rightDate = itemView.findViewById(R.id.rightdate);

    }

    @Override
    public void bindData(MessageEntity itemData) {
        this.userID = itemData.getAuthorID();

        if(!itemData.getAuthorID().equals(DataRepositoryController.getInstance().getUser().getValue().getUserID())){

             view.setBackgroundResource(R.drawable.message_background_other);
            leftSide.setVisibility(View.VISIBLE);
            messageLayout.setGravity(Gravity.LEFT);
            rightSide.setVisibility(View.GONE);

            if(itemData.getMedia() == null){
                leftContent.setVisibility(View.VISIBLE);
                leftPic.setVisibility(View.GONE);
                leftContent.setText(itemData.getBuilder());

            }
            else{
                leftContent.setVisibility(View.GONE);
                leftPic.setVisibility(View.VISIBLE);
                MediaEntity m = itemData.getMedia();
                if(m.uri != null)
                    Glide.with(mAdapter.getContext()).load(m.uri).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(leftPic);
                else if(m.url != null)
                    Glide.with(mAdapter.getContext()).load(m.url).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(leftPic);
                else if(m.bitmap != null)
                    Glide.with(mAdapter.getContext()).load(m.bitmap).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(leftPic);
            }
            leftDate.setText(itemData.getDatePosted().toString());
        }

        else{
            view.setBackgroundResource(R.drawable.message_background_user);
            leftSide.setVisibility(View.GONE);
            messageLayout.setGravity(Gravity.RIGHT);
            rightSide.setVisibility(View.VISIBLE);

            if(itemData.getMedia() == null){
                rightContent.setVisibility(View.VISIBLE);
                rightPic.setVisibility(View.GONE);
                rightContent.setText(itemData.getBuilder());
            }
            else{
                rightContent.setVisibility(View.GONE);
                rightPic.setVisibility(View.VISIBLE);
                MediaEntity m = itemData.getMedia();
                if(m.uri != null)
                    Glide.with(mAdapter.getContext()).load(m.uri).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(rightPic);
                else if(m.url != null)
                    Glide.with(mAdapter.getContext()).load(m.url).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(rightPic);
                else if(m.bitmap != null)
                    Glide.with(mAdapter.getContext()).load(m.bitmap).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(rightPic);
            }
            rightDate.setText(itemData.getDatePosted().toString());
        }
        UserSimple user = DataRepositoryController.getInstance().getUserSimpleProfile(itemData.getAuthorID());
        if(user != null) {
            this.onProfileChange(user);
        }
    }

    @Override
    public void onProfileChange(UserSimple user) {
        if(this.userID.compareTo(user.getUserID()) == 0){
            if(!this.userID.equals(DataRepositoryController.getInstance().getUser().getValue().getUserID())){
                Glide.with(mAdapter.getContext()).load(user.getProfilePic().url)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(this.leftProfilePic);
            }
            else{
                Glide.with(mAdapter.getContext()).load(user.getProfilePic().url)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(this.rightProfilePic);
            }
        }
    }

}
