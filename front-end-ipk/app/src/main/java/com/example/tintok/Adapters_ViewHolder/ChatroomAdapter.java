package com.example.tintok.Adapters_ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.ChatRoom;
import com.example.tintok.R;

import java.util.ArrayList;

public class ChatroomAdapter extends BaseAdapter<ChatRoom, ChatroomViewHolder> {

    onChatRoomClickListener mListner;
    public ChatroomAdapter(Context context, ArrayList<ChatRoom> models, onChatRoomClickListener mListner) {
        super(context, models);
        this.mListner=mListner;
    }

    @NonNull
    @Override
    public ChatroomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom, parent, false);
        return new ChatroomViewHolder(view, this, mListner);
    }

    public interface onChatRoomClickListener{
        public void OnClick(int pos);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull  ChatroomViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        DataRepositoryController.getInstance().AddUserProfileChangeListener(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull  ChatroomViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        DataRepositoryController.getInstance().RemoveUserProfileChangeListener(holder);
    }
}
