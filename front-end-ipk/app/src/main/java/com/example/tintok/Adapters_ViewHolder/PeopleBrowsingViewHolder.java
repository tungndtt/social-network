package com.example.tintok.Adapters_ViewHolder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.R;
import com.yuyakaido.android.cardstackview.Direction;


public class PeopleBrowsingViewHolder extends BaseViewHolder<UserSimple> {
    TextView name, description;
    ImageView imageView, likeImg, dislikeImg;
    ScaleGestureDetector gestureDetector;
    Matrix mMatrix = new Matrix();
    float scale = 1f;
    public PeopleBrowsingViewHolder(@NonNull View itemView, BaseAdapter mAdapter) {
        super(itemView, mAdapter);
        name = itemView.findViewById(R.id.profilename);
        description = itemView.findViewById(R.id.profileDes);
        imageView = itemView.findViewById(R.id.profileimage);
        likeImg = itemView.findViewById(R.id.likeImg);
        dislikeImg = itemView.findViewById(R.id.dislikeImg);

        gestureDetector = new ScaleGestureDetector(itemView.getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scale = scale*detector.getScaleFactor();
                scale = Math.max(0.5f, Math.min(scale, 2.0f));
                mMatrix.setScale(scale,scale, detector.getFocusX(), detector.getFocusY());
                imageView.setImageMatrix(mMatrix);
                return true;
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;

            }
        });


    }


    //like = true;
    //dislike = false;
    @Override
    public void bindData(UserSimple itemData) {
        dislikeImg.setVisibility(View.INVISIBLE);
        likeImg.setVisibility(View.INVISIBLE);
        name.setText(itemData.getUserName().toUpperCase()+", "+itemData.getAge());
        description.setText(itemData.getDescription());
        Glide.with(mAdapter.getContext()).load(itemData.getProfilePic().url).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(imageView);
    }
}
