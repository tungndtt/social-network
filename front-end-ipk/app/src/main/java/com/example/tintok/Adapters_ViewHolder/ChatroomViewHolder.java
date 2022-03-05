package com.example.tintok.Adapters_ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.DataLayer.DataRepository_UserSimple;
import com.example.tintok.Model.ChatRoom;
import com.example.tintok.Model.MessageEntity;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.R;

public class ChatroomViewHolder extends BaseViewHolder<ChatRoom> implements View.OnClickListener, DataRepository_UserSimple.OnUserProfileChangeListener{
    ImageView profilepic;
    TextView name, date, lastmsg;
    ChatroomAdapter.onChatRoomClickListener mListener;
    String userID;
    public ChatroomViewHolder(@NonNull View itemView, BaseAdapter mAdapter, ChatroomAdapter.onChatRoomClickListener mListener) {
        super(itemView, mAdapter);
        this.mListener = mListener;
        profilepic = itemView.findViewById(R.id.profilePic);
        name = itemView.findViewById(R.id.profilename);
        date = itemView.findViewById(R.id.lastModifiedDate);
        lastmsg = itemView.findViewById(R.id.newestMsg);

        itemView.setOnClickListener(this);



    }

    @Override
    public void bindData(ChatRoom itemData) {
        DataRepositoryController data = DataRepositoryController.getInstance();
        String thisUserID = data.getUser().getValue().getUserID();
        UserSimple another = null;
        for(String id : itemData.getMemberIDs()){
            if(id.compareTo(thisUserID)!= 0){
                this.userID = id;
                another = data.getUserSimpleProfile(id);
                break;
            }
        }
        if(another!=null){
           this.onProfileChange(another);
        }
        if(itemData.getMessageEntities().getValue() == null || itemData.getMessageEntities().getValue().isEmpty())
            return;
        MessageEntity mess = itemData.getMessageEntities().getValue().get(itemData.getMessageEntities().getValue().size()-1);
        this.date.setText(mess.getDatePosted().toString());
        if(mess.getBuilder() == null)
            lastmsg.setText("IMG");
        else
            lastmsg.setText(mess.getBuilder());
    }

    void onLiveDataChange(){

    }

    @Override
    public void onClick(View v) {
        mListener.OnClick(getAdapterPosition());
    }

    public void onProfileChange(UserSimple user) {
        if(this.userID.compareTo(user.getUserID()) == 0){
            Glide.with(mAdapter.getContext()).load(user.getProfilePic().url).diskCacheStrategy(DiskCacheStrategy.DATA).into(profilepic);
            name.setText(user.getUserName());

        }
    }
}
