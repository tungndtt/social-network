package com.example.tintok.Adapters_ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.DataLayer.DataRepository_UserSimple;
import com.example.tintok.Model.Notification;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.R;

import java.util.ArrayList;

public class NotificationsAdapter extends BaseAdapter<Notification, NotificationsAdapter.NotificationViewHolder> {
    public NotificationsAdapter(Context context, ArrayList<Notification> models) {
        super(context, models);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view, this);
    }

    public void onViewAttachedToWindow(@NonNull  NotificationViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        DataRepositoryController.getInstance().AddUserProfileChangeListener(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull  NotificationViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        DataRepositoryController.getInstance().RemoveUserProfileChangeListener(holder);
    }

    public void setNotificationClickListener(onNotificationClickListener mListener) {
        this.mListener = mListener;
    }

    onNotificationClickListener mListener;
    public interface onNotificationClickListener{
        void onPostClick(int position);
        void onProfileClick(int position);

    }
    public class NotificationViewHolder extends BaseViewHolder<Notification> implements DataRepository_UserSimple.OnUserProfileChangeListener {

        ImageView profilePic, contentPic;
        TextView content, date;
        public NotificationViewHolder(@NonNull View itemView, BaseAdapter mAdapter) {
            super(itemView, mAdapter);
            profilePic = itemView.findViewById(R.id.profilePic);
            contentPic = itemView.findViewById(R.id.contentPic);
            content = itemView.findViewById(R.id.content);
            date = itemView.findViewById(R.id.date);
            profilePic.setOnClickListener(v -> {
                if(mListener!= null)
                    mListener.onProfileClick(getAdapterPosition());
            });
            contentPic.setOnClickListener(v -> {
                if(mListener!= null)
                    mListener.onPostClick(getAdapterPosition());
            });

        }

        Notification current;
        @Override
        public void bindData(Notification itemData) {
            current = itemData;
            this.date.setText(itemData.getDate());
            if(itemData.getType() == Notification.NotificationType.NEW_FRIEND){
                contentPic.setVisibility(View.GONE);
            }
            else{
                contentPic.setVisibility(View.VISIBLE);
                Glide.with(mAdapter.getContext()).load(itemData.getUrl()).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(contentPic);
            }
            UserSimple user = DataRepositoryController.getInstance().getUserSimpleProfile(itemData.getAuthor_id());
            if (user != null) {
                this.onProfileChange(user);
            }

        }

        @Override
        public void onProfileChange(UserSimple user) {
            if(current.getAuthor_id().compareTo(user.getUserID()) == 0){
                this.content.setText(current.toTextViewString());
                if(profilePic.getVisibility() == View.VISIBLE)
                    Glide.with(mAdapter.getContext()).load(user.getProfilePic().url).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(profilePic);
            }
        }
    }
}
